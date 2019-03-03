package com.juhezi.ffmcli.core

import android.content.Context
import android.content.SharedPreferences
import com.juhezi.ffmcli.model.CommandResult
import com.juhezi.ffmcli.model.ShellCommand
import com.juhezi.ffmcli.util.Utils
import me.juhezi.eternal.global.logd
import me.juhezi.eternal.global.logi
import me.juhezi.eternal.service.SharedPreferencesService
import me.juhezi.eternal.util.copyBinaryFromAssetsToData

import java.io.File

object FFmpegOperation {

    private val cpuArchName = "armeabi-v7a"

    fun loadLibrary(context: Context): Boolean {
        val ffmpegFile = File(Utils.getFFmpeg(context))
        if (ffmpegFile.exists() &&
            !Utils.isDeviceFFmpegVersionOld(context)
        ) {
            logi("exist!!")
            // 版本号相同，不需要重新覆盖
            return true
        }
        SharedPreferencesService.write(context, Utils.FFMPEG_CLI_TAG) {
            putInt(Utils.FFMPEG_VERSION, Utils.getAssetsFFmpegVersion())
        }
        val isCopy = copyBinaryFromAssetsToData(
            context,
            cpuArchName + File.separator + Utils.FFMPEG_BIN_NAME,
            Utils.FFMPEG_BIN_NAME
        )
        // make file executable
        return if (isCopy) {
            if (!ffmpegFile.canExecute()) {
                logd("FFmpeg is not executable, trying to make it executable ...")
                ffmpegFile.setExecutable(true)
            } else {
                logd("FFmpeg is executable.")
                true
            }
        } else {
            false
        }
    }

}
