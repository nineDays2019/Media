package me.juhezi.eternal.gpuimage.renderer

import android.opengl.GLSurfaceView
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

abstract class EternalBaseRenderer : GLSurfaceView.Renderer {

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        onSurfaceChanged(width, height)
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        onSurfaceCreated()
    }

    override fun onDrawFrame(gl: GL10?) {
        onDrawFrame()
    }

    abstract fun onSurfaceCreated()
    abstract fun onDrawFrame()
    abstract fun onSurfaceChanged(width: Int, height: Int)

}