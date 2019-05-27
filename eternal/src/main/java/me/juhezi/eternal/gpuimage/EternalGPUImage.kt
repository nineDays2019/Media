package me.juhezi.eternal.gpuimage

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PixelFormat
import android.opengl.GLSurfaceView
import android.view.MotionEvent.*
import me.juhezi.eternal.gpuimage.filter.EternalBaseFilter
import me.juhezi.eternal.gpuimage.helper.FPSHelper

class EternalGPUImage(val context: Context) {

    private var filter: EternalBaseFilter =
        EternalBaseFilter()
    private var renderer: EternalGPUImageRenderer
    private var touchPoint: Pair<Float, Float> = 0f to 0f
    private var lastTime = 0L
    private var running = true
    var continuous = false  // 是否连续渲染
    var fps = 30

    var glSurfaceView: GLSurfaceView? = null
        @SuppressLint("ClickableViewAccessibility")
        set(value) {
            field = value
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

    init {
        renderer = EternalGPUImageRenderer(filter)
        renderer.fpsClosure = {
            if (continuous && running) {
                Thread.sleep(FPSHelper.getDelayTime(fps).toLong())
                requestRender(true)
            }
            lastTime = System.currentTimeMillis()
        }
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