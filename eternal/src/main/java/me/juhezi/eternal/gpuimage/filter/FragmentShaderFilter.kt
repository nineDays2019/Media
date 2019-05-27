package me.juhezi.eternal.gpuimage.filter

import android.opengl.GLES20.*
import me.juhezi.eternal.gpuimage.objects.VertexArray

class FragmentShaderFilter(
    override var vertexShader: String = NO_FILTER_VERTEX_SHADER,
    override var fragmentShader: String = COLOR_FRAGMENT_SHADER
) :
    EternalGPUImageFilter(vertexShader, fragmentShader) {

    var index = 0f

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
    }

    override fun onDraw() {
        super.onDraw()
        if (uTimeLocation >= 0) {
            val time = System.currentTimeMillis() % 1000
            glUniform1f(uTimeLocation, time / 100f)
        }
        if (uResolutionLocation >= 0) {
            resolutionVertexArray.updateBuffer(floatArrayOf(outputWidth.toFloat(), outputHeight.toFloat()))
            glUniform2fv(uResolutionLocation, 1, resolutionVertexArray.getFloatBuffer())    // count 可以用来赋值数组
        }

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