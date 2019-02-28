package com.juhezi.ffmcli.util

import android.content.Context
import android.content.SharedPreferences
import android.os.AsyncTask
import android.text.TextUtils
import com.juhezi.ffmcli.BuildConfig
import me.juhezi.eternal.global.loge
import me.juhezi.eternal.service.SharedPreferencesService
import java.io.*

object Utils {

    val FFMPEG_BIN_NAME = "ffmpeg"
    val FFMPEG_CLI_TAG = "ffmpeg_cli"
    val FFMPEG_VERSION = "ffmpeg_version"

    fun getFFmpeg(context: Context?): String {
        return if (context == null) "" else context.filesDir.absolutePath + File.separator + FFMPEG_BIN_NAME
    }

    fun getFFmpeg(context: Context, environmentVars: Map<String, String>?): String {
        val ffmpegCommand = StringBuilder()
        if (environmentVars != null) {
            for ((key, value) in environmentVars) {
                ffmpegCommand.append(key).append("=").append(value).append(" ")
            }
        }
        ffmpegCommand.append(getFFmpeg(context))
        return ffmpegCommand.toString()
    }

    /**
     * 获取内存中 ffmpeg 的版本号
     * SharedPreferences 中获取
     *
     * @param context
     * @return
     */
    fun getDeviceFFmpegVersion(context: Context): Int {
        var result = -1
        SharedPreferencesService.read(context, FFMPEG_CLI_TAG) {
            result = getInt(FFMPEG_VERSION, result)
        }
        return result
    }

    /**
     * 获取 Assets 中 ffmpeg 的版本号
     * 从 Gradle Config 中获取版本号
     *
     * @return
     */
    fun getAssetsFFmpegVersion() = BuildConfig.FFMPEG_VERSION

    fun isDeviceFFmpegVersionOld(context: Context): Boolean {
        if (getDeviceFFmpegVersion(context) < 0) return true
        if (getDeviceFFmpegVersion(context) < getAssetsFFmpegVersion()) return true
        return false
    }

    fun convertInputStreamToString(inputStream: InputStream): String? {
        try {
            val r = BufferedReader(InputStreamReader(inputStream))
            var read: String
            val sb = StringBuilder()
            read = r.readLine()
            while (read != null) {
                sb.append(read)
                read = r.readLine()
            }
            return read
        } catch (e: IOException) {
            loge("error converting input stream to string ${e.message}")
            return null
        }
    }

    fun destroyProcess(process: Process?) {
        process?.destroy()
    }

    fun killAsync(asyncTask: AsyncTask<Any, Any, Any>?) =
        asyncTask != null &&
                asyncTask.isCancelled &&
                asyncTask.cancel(true)

    fun isProcessCompleted(process: Process?): Boolean {
        try {
            if (process == null) return true
            process.exitValue()
            return true
        } catch (e: IllegalThreadStateException) {
            loge(e.message)
        }
        return false
    }

}
