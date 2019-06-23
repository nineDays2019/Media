package me.juhezi.eternal.media.tools

import android.graphics.ImageFormat
import android.media.Image
import android.util.Log
import me.juhezi.eternal.global.loge
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.lang.Exception

fun getEternalFormatById(id: Int) = EternalFormat.values().find { it.id == id }

fun saveImageToFile(image: Image, file: File, clear: Boolean = true) {

    when (image.format) {
        ImageFormat.JPEG -> saveJPEGImageToFile(image, file, clear)
    }
    throw Exception("Undefined format")
}

fun saveJPEGImageToFile(image: Image, file: File, clear: Boolean = true) {
    val buffer = image.planes[0].buffer
    val bytes = ByteArray(buffer.remaining())
    buffer.get(bytes)
    var output: FileOutputStream? = null
    try {
        output = FileOutputStream(file).apply {
            write(bytes)
        }
    } catch (e: IOException) {
        loge(e.message)
    } finally {
        if (clear) {
            image.close()
        }
        output?.let {
            try {
                it.close()
            } catch (e: IOException) {
                loge(e.message)
            }
        }
    }
}