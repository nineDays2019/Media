package me.juhezi.eternal.gpuimage

import android.opengl.GLES20
import android.opengl.GLES20.*
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import me.juhezi.eternal.gpuimage.helper.ShaderHelper
import java.nio.FloatBuffer

open class EternalGPUImageFilter(
    protected open var vertexShader: String = NO_FILTER_VERTEX_SHADER,
    protected open var fragmentShader: String = NO_FILTER_FRAGMENT_SHADER
) {

    companion object {
        // 没有滤镜的顶点着色器
        val NO_FILTER_VERTEX_SHADER = """
            attribute vec4 position;
            attribute vec2 inputTextureCoordinate;
            uniform mat4 uMatrix;
            varying vec2 textureCoordinate;
            void main() {
                gl_Position = uMatrix * position;
                textureCoordinate = inputTextureCoordinate;
            }
        """.trimIndent()
        val NO_FILTER_FRAGMENT_SHADER = """
            varying highp vec2 textureCoordinate;
            uniform sampler2D inputImageTexture;
            void main() {
                gl_FragColor = texture2D(inputImageTexture, textureCoordinate);
            }
        """.trimIndent()
    }

    protected var program = 0
    protected var aPositionLocation = 0
    protected var aInputTextureCoordinateLocation = 0

    protected var uMatrixPosition = 0

    protected var uInputImageTextureLocation = 0

    protected var width: Int = 0
    protected var height: Int = 0

    private var isInitialized = false

    var gpuImage: EternalGPUImage? = null

    fun init() {
        onInit()
        isInitialized = true
        onInitialized()
    }

    fun destroy() {
        isInitialized = false
        glDeleteProgram(program)
        onDestroy()
    }

    open fun getRenderMode() = GLSurfaceView.RENDERMODE_WHEN_DIRTY

    open fun onInit() {
        if (isInitialized) return
        program = ShaderHelper.buildProgram(vertexShader, fragmentShader)
        aPositionLocation = GLES20.glGetAttribLocation(
            program,
            "position"
        )
        aInputTextureCoordinateLocation = GLES20.glGetAttribLocation(
            program,
            "inputTextureCoordinate"
        )

        uMatrixPosition = GLES20.glGetUniformLocation(program, "uMatrix")
        uInputImageTextureLocation = GLES20.glGetUniformLocation(
            program,
            "inputImageTexture"
        )
    }

    open fun onInitialized() {}

    open fun onDestroy() {}

    open fun onDraw(
        textureId: Int, cubeBuffer: FloatBuffer,
        textureBuffer: FloatBuffer, inputMatrix: FloatArray? = null
    ) {
        glUseProgram(program)
        if (!isInitialized) {
            return
        }
        cubeBuffer.position(0)
        glVertexAttribPointer(
            aPositionLocation, POSITION_COMPONENT_COUNT,
            GL_FLOAT, false,
            0, cubeBuffer
        )
        glEnableVertexAttribArray(aPositionLocation)

        textureBuffer.position(0)
        glVertexAttribPointer(  // 这里有问题
            aInputTextureCoordinateLocation, POSITION_COMPONENT_COUNT,
            GL_FLOAT, false,
            0, textureBuffer
        )
        glEnableVertexAttribArray(aInputTextureCoordinateLocation)  // 这里也有问题

        if (textureId != NO_TEXTURE) {
            glActiveTexture(GL_TEXTURE0)
            glBindTexture(GL_TEXTURE_2D, textureId)
            glUniform1i(uInputImageTextureLocation, 0)
        }
        val matrix: FloatArray
        if (inputMatrix == null) {
            matrix = FloatArray(16)
            Matrix.setIdentityM(matrix, 0)
        } else {
            matrix = inputMatrix
        }
        glUniformMatrix4fv(uMatrixPosition, 1, false, matrix, 0)
        glDrawArrays(GL_TRIANGLE_STRIP, 0, 4)
        glDisableVertexAttribArray(aPositionLocation)
        glDisableVertexAttribArray(aInputTextureCoordinateLocation)
        glBindTexture(GL_TEXTURE_2D, 0)
    }

    open fun setOutputSize(size: Pair<Int, Int>) {
        width = size.first
        height = size.second
    }

    fun resetFragmentShader(fragmentShader: String) {
        this.fragmentShader = fragmentShader
        // 重新初始化
        init()
        gpuImage?.requestRender()
    }

}
