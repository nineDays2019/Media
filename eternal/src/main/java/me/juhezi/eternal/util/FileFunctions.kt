package me.juhezi.eternal.util

import android.content.Context
import me.juhezi.eternal.global.loge
import java.io.*
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*

private const val DEFAULT_BUFFER_SIZE = 1024 * 4
private const val EOF = -1

fun close(closeable: Closeable?) {
    try {
        closeable?.close()
    } catch (e: IOException) {
        //do nothing
    }
}


fun copyBinaryFromAssetsToData(context: Context, inputName: String, outputName: String):
        Boolean {
    // dir under /data/data/package name
    val fileDir = context.filesDir
    var inputStream: InputStream
    try {
        inputStream = context.assets.open(inputName)
        val outputStream = FileOutputStream(File(fileDir, outputName))
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        var read = inputStream.read(buffer)
        while (EOF != read) {
            outputStream.write(buffer, 0, read)
            read = inputStream.read(buffer)
        }
        close(inputStream)
        close(outputStream)
        return true
    } catch (e: IOException) {
        loge(e.message)
    }
    return false
}

fun sha1(path: String): String? {
    var inputStream: InputStream? = null
    try {
        inputStream = BufferedInputStream(FileInputStream(path))
        return sha1(inputStream)
    } catch (e: IOException) {
        loge(e.message)
    } finally {
        close(inputStream)
    }
    return null
}

fun sha1(inputStream: InputStream): String? {
    try {
        val messageDigest = MessageDigest.getInstance("SHA1")
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        var read = inputStream.read(buffer)
        while (read != EOF) {
            messageDigest.update(buffer, 0, read)
            read = inputStream.read(buffer)
        }
        val formatter = Formatter()
        // convert byte to format
        for (b in messageDigest.digest()) {
            formatter.format("%02x", b)
        }
        return formatter.toString()
    } catch (e: NoSuchAlgorithmException) {
        loge(e.message)
    } catch (e: IOException) {
        loge(e.message)
    } finally {
        close(inputStream)
    }
    return null
}