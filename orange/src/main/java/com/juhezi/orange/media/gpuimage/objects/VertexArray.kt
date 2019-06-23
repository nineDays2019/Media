package com.juhezi.orange.media.gpuimage.objects

import com.juhezi.orange.media.gpuimage.BYTES_PER_FLOAT
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

/**
 * VAO
 */
class VertexArray {

    private var floatBuffer: FloatBuffer

    constructor(vertexData: FloatArray) {
        floatBuffer = ByteBuffer
            .allocateDirect(vertexData.size * BYTES_PER_FLOAT)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .put(vertexData)

    }

    constructor(size: Int) {
        floatBuffer = ByteBuffer
            .allocateDirect(size * BYTES_PER_FLOAT)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
    }

    fun getFloatBuffer() = floatBuffer

    fun updateBuffer(vertexData: FloatArray): FloatBuffer {
        floatBuffer.clear()
        floatBuffer.put(vertexData).position(0)
        return floatBuffer
    }

    override fun toString(): String {
        return floatBuffer.toString()
    }

}