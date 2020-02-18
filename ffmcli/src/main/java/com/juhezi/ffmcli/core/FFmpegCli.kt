package com.juhezi.ffmcli.core

import android.content.Context
import com.juhezi.ffmcli.handler.FFmpegLoadLibraryResponseHandler
import com.juhezi.ffmcli.task.FFmpegLoadLibraryAsyncTask
import com.juhezi.ffmcli.util.Utils
import me.juhezi.eternal.other.EShell
import me.juhezi.eternal.other.EShellCallback

object FFmpegCli {

    var ffmpegLoadLibraryAsyncTask: FFmpegLoadLibraryAsyncTask? = null

    fun loadLibrary(
        context: Context,
        ffmpegLoadLibraryResponseHandler: FFmpegLoadLibraryResponseHandler
    ) {
        ffmpegLoadLibraryAsyncTask =
            FFmpegLoadLibraryAsyncTask(context, ffmpegLoadLibraryResponseHandler)
        ffmpegLoadLibraryAsyncTask!!.execute()
    }


    /**
     * 异步执行
     */
    fun execute(
        context: Context,
        command: String,
        shellCallback: EShellCallback,
        timeoutMs: Long = -1,
        environmentVars: Map<String, String>? = null
    ) = if (command.isNotEmpty()) {
        EShell().setCallback(shellCallback).runAsync(
            command.replace("ffmpeg", Utils.getFFmpeg(context, environmentVars)),
            timeoutMs
        )
    } else null

}