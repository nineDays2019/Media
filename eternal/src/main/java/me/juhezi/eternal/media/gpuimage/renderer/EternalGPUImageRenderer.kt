package me.juhezi.eternal.media.gpuimage.renderer

import android.opengl.GLES20
import android.opengl.GLES20.*
import me.juhezi.eternal.media.gpuimage.filter.EternalBaseFilter
import java.util.*

class EternalGPUImageRenderer(private var currentFilter: EternalBaseFilter) :
    EternalBaseRenderer() {

    private val runOnDrawQueue: Queue<() -> Unit>

    var preDrawClosure: (() -> Unit)? = null
    var afterDrawClosure: (() -> Unit)? = null
    // fps
    var fpsClosure: (() -> Unit)? = null

    private var outputWidth: Int = 0    // 输出宽高
    private var outputHeight: Int = 0

    private var isStop = false

    init {
        runOnDrawQueue = LinkedList()
    }

    // 如果是 TextureView 的话，这个方法不是在 GL 线程
    // glViewport 需要再 GL 线程中执行
    override fun onSurfaceChanged(width: Int, height: Int) {
        outputWidth = width
        outputHeight = height
        currentFilter.setOutputSize(outputWidth to outputHeight)
        runOnDraw {
            glViewport(0, 0, width, height)
        }
    }

    override fun onSurfaceCreated() {
        glClearColor(0f, 0f, 0f, 1f)
        glDisable(GL_DEPTH_TEST)
        currentFilter.init()
    }

    override fun onDrawFrame() {
        glClear(
            GLES20.GL_COLOR_BUFFER_BIT or
                    GLES20.GL_DEPTH_BUFFER_BIT
        )
        runAll(runOnDrawQueue)
        preDrawClosure?.invoke()
        currentFilter.onDraw()
        afterDrawClosure?.invoke()
        fpsClosure?.invoke()
    }

    fun runOnDraw(runnable: () -> Unit) {
        synchronized(runOnDrawQueue) {
            runOnDrawQueue.add(runnable)
        }
    }

    private fun runAll(queue: Queue<() -> Unit>) {
        synchronized(queue) {
            while (!queue.isEmpty()) {
                queue.poll()()
            }
        }
    }

    fun setFilter(filter: EternalBaseFilter) {
        runOnDraw {
            val oldFilter = this.currentFilter
            currentFilter = filter
            oldFilter.destroy()
            currentFilter.init()
            currentFilter.setOutputSize(outputWidth to outputHeight)
        }
    }

    fun destroy() {
        isStop = true
        currentFilter.destroy()
    }

}