package com.juhezi.orange.media.gpuimage.filter

import android.opengl.GLES20.*
import android.opengl.Matrix
import com.juhezi.orange.media.gpuimage.BASE_FRAGMENT_SHADER
import com.juhezi.orange.media.gpuimage.BASE_VERTEX_SHADER
import com.juhezi.orange.media.gpuimage.objects.VertexArray

class FragmentShaderFilter(
    override var vertexShader: String = BASE_VERTEX_SHADER,
    override var fragmentShader: String = BASE_FRAGMENT_SHADER
) :
    EternalBaseFilter(vertexShader, fragmentShader) {

    var index = 0f

    private var uResolutionLocation = 0
    private var uMouseLocation = 0
    private var uTimeLocation = 0

    private var useProjection = false   // 不能使用投影，现阶段的解决办法就是设置 TextureView 的宽高

    private val resolutionVertexArray: VertexArray =
        VertexArray(2)
    private val mouseVertexArray: VertexArray =
        VertexArray(2)

    override fun onInit() {
        super.onInit()
        uResolutionLocation = glGetUniformLocation(program, "u_resolution")
        uMouseLocation = glGetUniformLocation(program, "u_mouse")
        uTimeLocation = glGetUniformLocation(program, "u_time")
    }

    override fun onDraw() {
        super.onDraw()
        if (uTimeLocation >= 0) {
            val time = System.currentTimeMillis() % 1000
            glUniform1f(uTimeLocation, time / 100f)
        }
        if (uResolutionLocation >= 0) {
            resolutionVertexArray.updateBuffer(floatArrayOf(outputWidth.toFloat(), outputHeight.toFloat()))
//            resolutionVertexArray.updateBuffer(getResolutionArray())
            glUniform2fv(uResolutionLocation, 1, resolutionVertexArray.getFloatBuffer())    // count 可以用来赋值数组
        }

    }

    /**
     * 正交投影矩阵
     */
    override fun getDrawMatrix(): FloatArray {
        return if (useProjection) {
            val matrix = FloatArray(16)
            val aspectRatio = if (outputWidth > outputHeight)
                outputWidth / outputHeight.toFloat() else
                outputHeight / outputWidth.toFloat()
            if (outputWidth > outputHeight) {   // Landscape
                Matrix.orthoM(matrix, 0, -aspectRatio, aspectRatio, -1f, 1f, -1f, 1f)
            } else {    // Portrait or square
                Matrix.orthoM(matrix, 0, -1f, 1f, -aspectRatio, aspectRatio, -1f, 1f)
            }
            matrix
        } else {
            super.getDrawMatrix()
        }
    }

    private fun getResolutionArray(): FloatArray {
        val value = Math.min(
            outputWidth.toFloat(),
            outputHeight.toFloat()
        )
        return FloatArray(2) { value }
    }

    // 传入触摸坐标
    // 要在 GL 线程中执行
    fun setTouchPoint(point: Pair<Float, Float>) {
        if (uMouseLocation >= 0) {
            mouseVertexArray.updateBuffer(floatArrayOf(point.first, point.second))
            glUniform2fv(uMouseLocation, 1, mouseVertexArray.getFloatBuffer())
        }
    }

}