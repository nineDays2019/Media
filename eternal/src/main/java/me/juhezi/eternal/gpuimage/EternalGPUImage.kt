package me.juhezi.eternal.gpuimage

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.opengl.GLSurfaceView
import android.view.MotionEvent.*
import me.juhezi.eternal.gpuimage.filter.FragmentShaderFilter

class EternalGPUImage(val context: Context) {

    private var filter: EternalGPUImageFilter = EternalGPUImageFilter()
    private var renderer: EternalGPUImageRenderer
    private var currentBitmap: Bitmap? = null
    private var touchPoint: Pair<Float, Float> = 0f to 0f

    var glSurfaceView: GLSurfaceView? = null
        @SuppressLint("ClickableViewAccessibility")
        set(value) {
            field = value
            field?.setEGLContextClientVersion(2)
            field?.setEGLConfigChooser(8, 8,
                    8, 8,
                    16, 0)
            field?.holder?.setFormat(PixelFormat.RGBA_8888)
            field?.setRenderer(renderer)
            field?.renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
            field?.requestRender()
            field?.setOnTouchListener { _, event ->
                touchPoint = when (event.action) {
                    ACTION_DOWN, ACTION_MOVE, ACTION_UP -> event.x to event.y
                    else -> 0f to 0f
                }
//                requestRender()
                true
            }
        }

    init {
        renderer = EternalGPUImageRenderer(filter)
        renderer.drawClosure = {
            // onDraw
            if (filter is FragmentShaderFilter) {
                (filter as FragmentShaderFilter).setTouchPoint(touchPoint)
            }
        }
    }

    fun requestRender() {
        glSurfaceView?.requestRender()
    }

    fun setImage(bitmap: Bitmap?): EternalGPUImage {
        currentBitmap = bitmap
        if (bitmap != null) {
            renderer.setImageBitmap(bitmap)
        }
        return this
    }

    fun setFilter(filter: EternalGPUImageFilter): EternalGPUImage {
        this.filter = filter
        renderer.setFilter(this.filter)
        this.filter.gpuImage = this
        requestRender()
        return this
    }

    fun runOnGLThread(runnable: Runnable) {
        renderer.runOnDraw(runnable)
    }

}