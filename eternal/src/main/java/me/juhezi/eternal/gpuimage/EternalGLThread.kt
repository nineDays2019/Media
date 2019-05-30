package me.juhezi.eternal.gpuimage

import android.graphics.SurfaceTexture
import android.opengl.EGL14
import android.opengl.GLUtils
import me.juhezi.eternal.extension.i
import me.juhezi.eternal.global.generateRandomID
import me.juhezi.eternal.gpuimage.helper.FPSHelper
import me.juhezi.eternal.gpuimage.renderer.EternalBaseRenderer
import java.util.concurrent.atomic.AtomicBoolean
import javax.microedition.khronos.egl.EGL10
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.egl.EGLContext
import javax.microedition.khronos.opengles.GL

class EternalGLThread(
    private var surfaceTexture: SurfaceTexture,
    private var renderer: EternalBaseRenderer,
    var fps: Int = 30
) : Thread("GLThread ${generateRandomID()}") {

    companion object {
        private const val EGL_CONTEXT_CLIENT = 0x3098
        private const val EGL_OPENGL_ES2_BIT = 4
    }

    private val isRunning: AtomicBoolean = AtomicBoolean(false)

    private var egl: EGL10? = null
    private var gl: GL? = null
    private var eglDisplay = EGL10.EGL_NO_DISPLAY
    private var eglContext = EGL10.EGL_NO_CONTEXT
    private var eglSurface = EGL10.EGL_NO_SURFACE

    private fun ifThenThrows(value: Boolean, message: String = "Message: ") {
        if (value) {
            throw RuntimeException(message + GLUtils.getEGLErrorString(egl?.eglGetError() ?: 0))
        }
    }

    private fun initGL() {
        egl = EGLContext.getEGL() as EGL10
        eglDisplay = egl!!.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY)

        ifThenThrows(eglDisplay == EGL10.EGL_NO_DISPLAY, "eglGetDisplay failed: ")

        val version = IntArray(2)

        ifThenThrows(!egl!!.eglInitialize(eglDisplay, version), "eglInitialize failed: ")

        val configAttribs = intArrayOf(
            EGL10.EGL_BUFFER_SIZE, 32,
            EGL10.EGL_ALPHA_SIZE, 8,
            EGL10.EGL_BLUE_SIZE, 8,
            EGL10.EGL_GREEN_SIZE, 8,
            EGL10.EGL_RED_SIZE, 8,
            EGL10.EGL_RENDERABLE_TYPE, EGL_OPENGL_ES2_BIT,
            EGL10.EGL_SURFACE_TYPE, EGL10.EGL_WINDOW_BIT,
            EGL10.EGL_NONE
        )
        val numConfigs = IntArray(1)
        val configs = arrayOfNulls<EGLConfig>(1)

        ifThenThrows(
            !egl!!.eglChooseConfig(eglDisplay, configAttribs, configs, 1, numConfigs),
            "eglChooseConfig failed: "
        )

        val contextAttribs = intArrayOf(
            EGL14.EGL_CONTEXT_CLIENT_VERSION, 2,
            EGL10.EGL_NONE
        )
        eglContext = egl!!.eglCreateContext(eglDisplay, configs[0], EGL10.EGL_NO_CONTEXT, contextAttribs)
        eglSurface = egl!!.eglCreateWindowSurface(eglDisplay, configs[0], surfaceTexture, null)
        if (eglSurface == EGL10.EGL_NO_SURFACE || eglContext == EGL10.EGL_NO_CONTEXT) {
            val error = egl!!.eglGetError()
            if (error == EGL10.EGL_BAD_NATIVE_WINDOW) {
                throw RuntimeException("eglCreateWindowSurface returned  EGL_BAD_NATIVE_WINDOW. ")
            }
            throw RuntimeException("eglCreateWindowSurface failed : " + GLUtils.getEGLErrorString(error))
        }

        if (!egl!!.eglMakeCurrent(eglDisplay, eglSurface, eglSurface, eglContext)) {
            throw RuntimeException(
                "eglMakeCurrent failed : " +
                        GLUtils.getEGLErrorString(egl!!.eglGetError())
            )
        }

        gl = eglContext.gl

    }

    private fun destroyGL() {
        with(egl!!) {
            eglDestroyContext(eglDisplay, eglContext)
            eglDestroySurface(eglDisplay, eglSurface)
        }
        eglContext = EGL10.EGL_NO_CONTEXT
        eglSurface = EGL10.EGL_NO_SURFACE
    }

    override fun run() {
        isRunning.set(true)
        initGL()
        renderer.onSurfaceCreated()
        var lastTime = 0L
        while (isRunning.get()) {
            renderer.onDrawFrame()
            egl!!.eglSwapBuffers(eglDisplay, eglSurface)
            val consumeTime = System.currentTimeMillis() - lastTime
            val remainTime = FPSHelper.getDelayTime(fps) - consumeTime
            if (remainTime > 0) {
                try {
                    sleep(remainTime)
                }catch (e:Exception){
                    i("被打断啦！！")
                }
            }
            lastTime = System.currentTimeMillis()
        }
        destroyGL()
    }

    fun stopRender() {
        // 下一帧不再渲染
        isRunning.set(false)
    }

}