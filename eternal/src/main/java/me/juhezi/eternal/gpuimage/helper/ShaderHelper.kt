package me.juhezi.eternal.gpuimage.helper

import android.opengl.GLES20.*
import me.juhezi.eternal.extension.value
import me.juhezi.eternal.global.isDebug
import me.juhezi.eternal.global.logw

object ShaderHelper {

    internal fun compileVertexShader(shaderCode: String): Int {
        return compileShader(GL_VERTEX_SHADER, shaderCode)
    }

    internal fun compileFragmentShader(shaderCode: String): Int {
        return compileShader(GL_FRAGMENT_SHADER, shaderCode)
    }

    internal fun compileShader(type: Int, shaderCode: String): Int {
        val shaderObjectId = glCreateShader(type)

        if (shaderObjectId == 0) {
            logw("Could not create new shader.")
            return 0
        }
        // 把着色器源代码上传到着色器对象里
        glShaderSource(shaderObjectId, shaderCode)
        glCompileShader(shaderObjectId)
        val compileStatus = IntArray(1)
        glGetShaderiv(shaderObjectId, GL_COMPILE_STATUS, compileStatus, 0)
        logw("Results of compiling source:\n$shaderCode\n:${glGetShaderInfoLog(shaderObjectId)}")
        if (compileStatus.value() == 0) {    // 编译失败
            glDeleteShader(shaderObjectId)
            logw("Compilation of shader failed.")
            return 0
        }
        return shaderObjectId
    }

    fun linkProgram(vertexShaderId: Int, fragmentShaderId: Int): Int {
        val programObjectId = glCreateProgram()
        if (programObjectId == 0) {
            logw("Could not create new program")
            return 0
        }
        glAttachShader(programObjectId, vertexShaderId)
        glAttachShader(programObjectId, fragmentShaderId)
        glLinkProgram(programObjectId)
        val linkStatus = IntArray(1)
        glGetProgramiv(programObjectId, GL_LINK_STATUS, linkStatus, 0)
        logw("Results of linking program:\n${glGetProgramInfoLog(programObjectId)}")
        if (linkStatus.value() == 0) {
            glDeleteProgram(programObjectId)
            logw("Linking of program failed")
            return 0
        }
        return programObjectId
    }

    internal fun validateProgram(programObjectId: Int): Boolean {
        glValidateProgram(programObjectId)
        val validateStatus = IntArray(1)
        glGetProgramiv(programObjectId, GL_VALIDATE_STATUS, validateStatus, 0)
        logw("Result of validating program: ${validateStatus.value()}\nLog: ${glGetProgramInfoLog(programObjectId)}")
        return validateStatus.value() != 0
    }

    fun buildProgram(vertexShaderSource: String,
                     fragmentShaderSource: String): Int {
        val vertexShader = compileVertexShader(vertexShaderSource)
        val fragmentShader = compileFragmentShader(fragmentShaderSource)

        val program = linkProgram(vertexShader, fragmentShader)
        if (isDebug()) {
            validateProgram(program)
        }
        glDeleteShader(vertexShader)
        glDeleteShader(fragmentShader)
        return program
    }


}