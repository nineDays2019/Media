package me.juhezi.eternal.gpuimage.filter

import android.graphics.Bitmap
import android.opengl.GLES20
import android.opengl.GLES20.*
import android.opengl.Matrix
import me.juhezi.eternal.gpuimage.BYTES_PER_FLOAT
import me.juhezi.eternal.gpuimage.NO_TEXTURE
import me.juhezi.eternal.gpuimage.POSITION_COMPONENT_COUNT
import me.juhezi.eternal.gpuimage.ST
import me.juhezi.eternal.gpuimage.helper.TextureHelper
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * 用来显示图片的 Filter
 */
open class EternalGPUImageFilter(
    override var vertexShader: String = NO_FILTER_VERTEX_SHADER,
    override var fragmentShader: String = NO_FILTER_FRAGMENT_SHADER
) : EternalBaseFilter(vertexShader, fragmentShader) {

    companion object {
        // 没有滤镜的顶点着色器
        val NO_FILTER_VERTEX_SHADER = """
            attribute vec4 aPosition;
            attribute vec2 aInputTextureCoordinate;
            uniform mat4 uMatrix;
            varying vec2 textureCoordinate;
            void main() {
                gl_Position = uMatrix * aPosition;
                textureCoordinate = aInputTextureCoordinate;
            }
        """.trimIndent()
        val NO_FILTER_FRAGMENT_SHADER = """
            varying highp vec2 textureCoordinate;
            uniform sampler2D uInputImageTexture;
            void main() {
                gl_FragColor = texture2D(uInputImageTexture, textureCoordinate);
            }
        """.trimIndent()
    }

    private val textureBuffer = ByteBuffer.allocateDirect(
        ST.size * BYTES_PER_FLOAT
    )
        .order(ByteOrder.nativeOrder())
        .asFloatBuffer()

    private var textureId: Int = NO_TEXTURE
    protected var bitmapWidth: Int = 0
    protected var bitmapHeight: Int = 0
    protected var bitmapRotation: Float = 0f

    protected var aInputTextureCoordinateLocation = 0

    protected var uInputImageTextureLocation = 0

    // 这个变量其实没什么卵用
    private var currentBitmap: Bitmap? = null

    init {
        textureBuffer.put(ST).position(0)
    }

    override fun onInit() {
        super.onInit()
        aInputTextureCoordinateLocation = GLES20.glGetAttribLocation(
            program,
            "aInputTextureCoordinate"
        )
        uInputImageTextureLocation = GLES20.glGetUniformLocation(
            program,
            "uInputImageTexture"
        )
    }

    override fun onDraw() {
        if (!isInitialized) {
            return
        }
//        i("Start: glError ${glGetError()}")
        glUseProgram(program)

        if (aPositionLocation >= 0) {
            cubeBuffer.position(0)
            glVertexAttribPointer(
                aPositionLocation, POSITION_COMPONENT_COUNT,
                GL_FLOAT, false,
                0, cubeBuffer
            )
            glEnableVertexAttribArray(aPositionLocation)
        }

        if (aInputTextureCoordinateLocation >= 0) {
            textureBuffer.position(0)
            glVertexAttribPointer(
                aInputTextureCoordinateLocation, POSITION_COMPONENT_COUNT,
                GL_FLOAT, false,
                0, textureBuffer
            )
            glEnableVertexAttribArray(aInputTextureCoordinateLocation)
        }

        if (textureId != NO_TEXTURE) {
            glActiveTexture(GL_TEXTURE0)
            glBindTexture(GL_TEXTURE_2D, textureId)
            glUniform1i(uInputImageTextureLocation, 0)
        }

        if (uMatrixPosition >= 0) {
            val matrix = FloatArray(16)
            // 先旋转，再缩放
            Matrix.multiplyMM(matrix, 0, getScaleMatrix(), 0, getRotateMatrix(), 0)
            glUniformMatrix4fv(
                uMatrixPosition, 1, false,
                matrix, 0
            )
        }
        glDrawArrays(GL_TRIANGLE_STRIP, 0, 4)
        if (aPositionLocation >= 0) {
            glDisableVertexAttribArray(aPositionLocation)
        }
        if (aInputTextureCoordinateLocation >= 0) {
            glDisableVertexAttribArray(aInputTextureCoordinateLocation)
        }
        glBindTexture(GL_TEXTURE_2D, 0)
    }

    fun setBitmap(bitmap: Bitmap, rotation: Float = 0f, recycle: Boolean = true) {
        currentBitmap = bitmap
        bitmapRotation = rotation
        val pair = with(bitmap) {
            if (bitmapRotation == 90f || bitmapRotation == 270f) {
                height to width
            } else {
                width to height
            }
        }
        bitmapWidth = pair.first
        bitmapHeight = pair.second
        runOnGLThread {
            textureId = TextureHelper.loadTexture(bitmap, recycle)
            gpuImage?.requestRender()
        }
    }

    fun removeBitmap() {
        GLES20.glDeleteTextures(1, intArrayOf(textureId), 0)
        textureId = NO_TEXTURE
        bitmapHeight = 0
        bitmapWidth = 0
    }

    fun getCurrentImage() = currentBitmap

    private fun getScaleMatrix(): FloatArray {
        val source = FloatArray(16)
        Matrix.setIdentityM(source, 0)
        if (bitmapHeight == 0 || bitmapWidth == 0 ||
            outputHeight == 0 || outputWidth == 0
        ) {
            return source
        } else {
            // 计算宽高的缩放比例
            val bitmapRatio = bitmapWidth / bitmapHeight.toFloat()  // 16 : 9
            val outputRatio = outputWidth / outputHeight.toFloat()  // 9 : 16
            // 图片宽高和归一化坐标的比例
            val rW = outputWidth / bitmapWidth.toFloat()
            val rH = outputHeight / bitmapHeight.toFloat()
            val widthScale: Float
            val heightScale: Float
            if (bitmapRatio >= outputRatio) {   // 宽度不变
                widthScale = 1f
                heightScale = rW / rH
            } else {    // 高度不变
                heightScale = 1f
                widthScale = rH / rW
            }
            Matrix.scaleM(
                source, 0, widthScale,
                heightScale,
                1f
            )
            return source
        }
    }

    private fun getRotateMatrix(): FloatArray {
        val source = FloatArray(16)
        Matrix.setIdentityM(source, 0)
        Matrix.rotateM(source, 0, -bitmapRotation, 0f, 0f, 1f)
        return source
    }


}
