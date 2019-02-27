package me.juhezi.eternal.gpuimage.filter

import android.opengl.GLES20.*
import me.juhezi.eternal.extension.i
import me.juhezi.eternal.gpuimage.EternalGPUImageFilter
import me.juhezi.eternal.gpuimage.objects.VertexArray
import java.nio.FloatBuffer

class FragmentShaderFilter :
        EternalGPUImageFilter(EternalGPUImageFilter.NO_FILTER_VERTEX_SHADER, COLOR_FRAGMENT_SHADER) {

    companion object {
        val COLOR_FRAGMENT_SHADER = """
            precision mediump float;

            void main() {
                gl_FragColor = vec4(1.0, 0.0, 0.0, 1.0);
            }
        """.trimIndent()
    }

    private var uResolutionLocation = 0
    private var uMouseLocation = 0
    private var uTimeLocation = 0

    private val resolutionVertexArray: VertexArray =
            VertexArray(2)
    private val mouseVertexArray: VertexArray =
            VertexArray(2)

    override fun onInit() {
        super.onInit()
        uResolutionLocation = glGetUniformLocation(program, "u_resolution")
        uMouseLocation = glGetUniformLocation(program, "u_mouse")
        uTimeLocation = glGetUniformLocation(program, "u_time")
        i("uResolutionLocation: $uResolutionLocation\tprogram: $program")
    }

    override fun onDraw(textureId: Int, cubeBuffer: FloatBuffer, textureBuffer: FloatBuffer, inputMatrix: FloatArray?) {
        super.onDraw(textureId, cubeBuffer, textureBuffer, inputMatrix)
        glUniform2f(uResolutionLocation, width.toFloat(), height.toFloat())
        i("resolution errorCode: ${glGetError()}")
        glUniform1f(uTimeLocation, System.currentTimeMillis() / 1000.0f)
        i("time errorCode: ${glGetError()}")

    }

    // 传入触摸坐标
    // 要在 GL 线程中执行
    fun setTouchPoint(point: Pair<Float, Float>) {
        glUniform2f(uMouseLocation, point.first, point.second)
        i("mouse errorCode: ${glGetError()}")
    }

}