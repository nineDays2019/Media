package me.juhezi.eternal.gpuimage

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PixelFormat
import android.graphics.SurfaceTexture
import android.opengl.GLSurfaceView
import android.view.MotionEvent.*
import android.view.TextureView
import me.juhezi.eternal.gpuimage.filter.EternalBaseFilter
import me.juhezi.eternal.gpuimage.renderer.EternalGPUImageRenderer

class EternalGPUImage(val context: Context) {

    private var filter: EternalBaseFilter =
        EternalBaseFilter()
    private var renderer: EternalGPUImageRenderer
    private var touchPoint: Pair<Float, Float> = 0f to 0f
    private var lastTime = 0L
    private var running = true
    var continuous = false  // 是否连续渲染

    var fps = 30
    private var glThread: EternalGLThread? = null

    // GLSurfaceView 和 Texture 只能二选一
    var glSurfaceView: GLSurfaceView? = null
        @SuppressLint("ClickableViewAccessibility")
        set(value) {
            if (value == null) return
            field = value
            textureView?.surfaceTextureListener = null
            textureView = null
            glThread?.stopRender()
            glThread?.interrupt()
            glThread = null
            field?.setEGLContextClientVersion(2)
            field?.setEGLConfigChooser(
                8, 8,
                8, 8,
                16, 0
            )
            field?.holder?.setFormat(PixelFormat.RGBA_8888)
            field?.setRenderer(renderer)
            field?.renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
            field?.requestRender()
            field?.setOnTouchListener { _, event ->
                touchPoint = when (event.action) {
                    ACTION_DOWN, ACTION_MOVE, ACTION_UP -> event.x to event.y
                    else -> 0f to 0f
                }
                true
            }
        }

    var textureView: TextureView? = null
        set(value) {
            if (value == null) return
            field = value
            glSurfaceView = null
            field?.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture?, width: Int, height: Int) {
                    renderer.onSurfaceChanged(width, height)
                }

                override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) {
                }

                override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?): Boolean {
                    glThread?.stopRender()
                    glThread?.interrupt()
                    glThread = null
                    return true
                }

                override fun onSurfaceTextureAvailable(surface: SurfaceTexture?, width: Int, height: Int) {
                    glThread = EternalGLThread(surface!!, renderer, fps)
                    glThread!!.start()
                    renderer.onSurfaceChanged(width, height)
                }
            }
        }

    init {
        renderer = EternalGPUImageRenderer(filter)
    }

    fun requestRender(force: Boolean = false) {
        if (running and (force or !continuous)) {
            glSurfaceView?.requestRender()
        }
    }

    fun setFilter(filter: EternalBaseFilter): EternalGPUImage {
        this.filter = filter
        this.filter.gpuImage = this
        renderer.setFilter(this.filter)
        requestRender()
        return this
    }

    fun runOnGLThread(runnable: () -> Unit) {
        renderer.runOnDraw(runnable)
        requestRender()
    }

    fun destroy() {
        running = false
        renderer.destroy()
    }

}