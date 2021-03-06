package com.juhezi.orange.media.effectfilter.Renderer

import android.opengl.GLSurfaceView
import java.util.*
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

abstract class EternalBaseRenderer : GLSurfaceView.Renderer {

    protected var outputWidth: Int = 0    // 输出宽高
    protected var outputHeight: Int = 0

    protected val runOnDrawQueue: Queue<() -> Unit>

    init {
        runOnDrawQueue = LinkedList()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        internalSurfaceChanged(width, height)
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        internalSurfaceCreated()
    }

    override fun onDrawFrame(gl: GL10?) {
        runAll(runOnDrawQueue)
        internalDrawFrame()
    }

    /**
     * 暂时还不知道在哪里调用
     */
    fun onSurfaceDestroyed() {
        inSurfaceDestroy()
    }

    protected abstract fun internalSurfaceCreated()
    protected abstract fun internalDrawFrame()
    protected abstract fun internalSurfaceChanged(width: Int, height: Int)
    protected abstract fun inSurfaceDestroy()

    private fun runAll(queue: Queue<() -> Unit>) {
        synchronized(queue) {
            while (!queue.isEmpty()) {
                queue.poll()()
            }
        }
    }

    fun runOnDraw(runnable: () -> Unit) {
        synchronized(runOnDrawQueue) {
            runOnDrawQueue.add(runnable)
        }
    }

}