package me.juhezi.eternal.gpuimage

import android.opengl.GLES20
import android.opengl.GLES20.*
import android.opengl.GLSurfaceView
import me.juhezi.eternal.gpuimage.filter.EternalBaseFilter
import java.util.*
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class EternalGPUImageRenderer(private var currentFilter: EternalBaseFilter) : GLSurfaceView.Renderer {

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

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        outputWidth = width
        outputHeight = height
        glViewport(0, 0, width, height)
        currentFilter.setOutputSize(outputWidth to outputHeight)
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        glClearColor(0f, 0f, 0f, 1f)
        glDisable(GL_DEPTH_TEST)
        currentFilter.init()
    }

    override fun onDrawFrame(gl: GL10?) {
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