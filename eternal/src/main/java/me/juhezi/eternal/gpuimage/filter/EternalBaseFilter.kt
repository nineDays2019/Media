package me.juhezi.eternal.gpuimage.filter

import android.opengl.GLES20
import android.opengl.GLES20.*
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import me.juhezi.eternal.gpuimage.*
import me.juhezi.eternal.gpuimage.helper.ShaderHelper
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * 什么都没有，显示区域全部显示白色
 */
open class EternalBaseFilter(
    protected open var vertexShader: String = BASE_VERTEX_SHADER,
    protected open var fragmentShader: String = BASE_FRAGMENT_SHADER
) {

    protected val cubeBuffer = ByteBuffer.allocateDirect(
        CUBE.size * BYTES_PER_FLOAT
    )
        .order(ByteOrder.nativeOrder())
        .asFloatBuffer()!!

    protected var program = 0
    protected var aPositionLocation = 0

    protected var uMatrixPosition = 0

    protected var outputWidth: Int = 0
    protected var outputHeight: Int = 0

    protected var isInitialized = false

    var gpuImage: EternalGPUImage? = null

    init {
        // 把顶点数据从 Java 堆复制到本地堆
        cubeBuffer.put(CUBE).position(0)
    }

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

    /**
     * GL Thread
     */
    open fun onInit() {
        if (isInitialized) return

        program = ShaderHelper.buildProgram(vertexShader, fragmentShader)
        aPositionLocation = GLES20.glGetAttribLocation(
            program,
            "aPosition"
        )
        uMatrixPosition = GLES20.glGetUniformLocation(program, "uMatrix")
    }

    open fun onInitialized() {}

    open fun onDestroy() {}

    /**
     * GL Thread
     */
    open fun onDraw() {
        if (!isInitialized) {
            return
        }
        glUseProgram(program)

        //---aPosition---
        if (aPositionLocation >= 0) {
            cubeBuffer.position(0)
            glVertexAttribPointer(
                aPositionLocation, POSITION_COMPONENT_COUNT,
                GL_FLOAT, false,
                0, cubeBuffer
            )
            glEnableVertexAttribArray(aPositionLocation)
        }

        //---uMatrix---
        if (uMatrixPosition >= 0) {
            val matrix = FloatArray(16)
            Matrix.setIdentityM(matrix, 0)
            glUniformMatrix4fv(uMatrixPosition, 1, false, matrix, 0)
        }
        glDrawArrays(GL_TRIANGLE_STRIP, 0, 4)
        glDisableVertexAttribArray(aPositionLocation)
    }

    open fun setOutputSize(size: Pair<Int, Int>) {
        outputWidth = size.first
        outputHeight = size.second
    }

    /**
     * 重置片段着色器
     */
    fun resetFragmentShader(fragmentShader: String) {
        this.fragmentShader = fragmentShader
        // 重新初始化
        init()
        gpuImage?.requestRender()
    }

    protected fun runOnGLThread(runnable: () -> Unit) {
        gpuImage?.runOnGLThread(runnable)
    }

}
