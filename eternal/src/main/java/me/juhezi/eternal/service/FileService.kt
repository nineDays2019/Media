package me.juhezi.eternal.service

import me.juhezi.eternal.extension.isEmpty
import java.io.File
import java.io.FileOutputStream
import java.io.PrintStream

/**
 * 文件服务
 * 所提供的服务有：
 * [x] 向文件中写入字符串
 * [ ] 从文件中读取字符串
 */
class FileService {

    companion object {
        fun getInstance() = Holder.sInstance
    }

    private object Holder {
        val sInstance = FileService()
    }

    fun write(path: String, content: String): Boolean {
        if (isEmpty(path)) return false
        val file = File(path)
        return write(file, content)
    }

    fun write(file: File, content: String): Boolean {
        if (!file.exists()) {
            file.createNewFile()
        }
        if (file.isDirectory) return false
        return try {
            val ps = PrintStream(FileOutputStream(file))
            ps.print(content)
            ps.close()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun append() {

    }

}