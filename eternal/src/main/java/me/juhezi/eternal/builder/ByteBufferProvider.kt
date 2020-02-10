package me.juhezi.eternal.builder

import java.io.FileInputStream
import java.io.IOException

class ByteBufferProvider(var path: String) {

    var fis: FileInputStream = FileInputStream(path)

    fun provide(buffer: ByteArray) = fis.read(buffer)

    fun close() {
        try {
            fis.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

}