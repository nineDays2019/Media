package com.juhezi.orange.media.gpuimage.renderer

import android.opengl.GLES20.*
import com.juhezi.orange.media.effectfilter.Renderer.EternalBaseRenderer
import com.juhezi.orange.media.gpuimage.filter.EternalBaseFilter

class EternalGPUImageRenderer(private var currentFilter: EternalBaseFilter) :
    EternalBaseRenderer() {

    var preDrawClosure: (() -> Unit)? = null
    var afterDrawClosure: (() -> Unit)? = null
    // fps
    var fpsClosure: (() -> Unit)? = null

    private var isStop = false

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
            GL_COLOR_BUFFER_BIT or
                    GL_DEPTH_BUFFER_BIT
        )
        preDrawClosure?.invoke()
        currentFilter.onDraw()
        afterDrawClosure?.invoke()
        fpsClosure?.invoke()
    }

    override fun onSurfaceDestroy() {

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