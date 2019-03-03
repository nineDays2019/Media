package com.juhezi.ffmcli.core

import android.content.Context
import com.juhezi.ffmcli.exception.FFmpegCommandAlreadyRunningException
import com.juhezi.ffmcli.handler.FFmpegExecuteResponseHandler
import com.juhezi.ffmcli.handler.FFmpegLoadLibraryResponseHandler
import com.juhezi.ffmcli.task.FFmpegExecuteAsyncTask
import com.juhezi.ffmcli.task.FFmpegLoadLibraryAsyncTask
import com.juhezi.ffmcli.util.Utils
import me.juhezi.eternal.global.concatenate
import me.juhezi.eternal.global.logi

object FFmpegCli {

    var ffmpegLoadLibraryAsyncTask: FFmpegLoadLibraryAsyncTask? = null
    var ffmpegExecuteAsyncTask: FFmpegExecuteAsyncTask? = null

    fun loadLibrary(context: Context, ffmpegLoadLibraryResponseHandler: FFmpegLoadLibraryResponseHandler) {
        ffmpegLoadLibraryAsyncTask =
                FFmpegLoadLibraryAsyncTask(context, ffmpegLoadLibraryResponseHandler)
        ffmpegLoadLibraryAsyncTask!!.execute()
    }

    fun execute(
        context: Context,
        environmentVars: Map<String, String>?,
        cmd: Array<String>,
        ffmpegExecuteResponseHandler: FFmpegExecuteResponseHandler
    ) {
        if (ffmpegExecuteAsyncTask != null && !ffmpegExecuteAsyncTask!!.isProcessCompleted()) {
            throw FFmpegCommandAlreadyRunningException(
                "FFmpeg command is already running, you are only allowed to run single command."
            )
        }
        if (!cmd.isEmpty()) {
            val ffmpegBinary = arrayOf(Utils.getFFmpeg(context, environmentVars))
            val command = concatenate(ffmpegBinary, cmd)
            logi("command is : ${buildString {
                command.forEach { append(it).append(" ") }
            }}")
            ffmpegExecuteAsyncTask = FFmpegExecuteAsyncTask(command, ffmpegExecuteResponseHandler)
            ffmpegExecuteAsyncTask!!.execute()
        }

    }

    fun execute(context: Context, cmd: Array<String>, ffmpegExecuteResponseHandler: FFmpegExecuteResponseHandler) {
        FFmpegCli.execute(context, null, cmd, ffmpegExecuteResponseHandler)
    }

    fun isFFmpegCommandRunning(): Boolean {
        return ffmpegExecuteAsyncTask != null && ffmpegExecuteAsyncTask!!.isProcessCompleted()
    }

    fun killRunningProcesses() = Utils.killAsync(ffmpegExecuteAsyncTask) || Utils.killAsync(ffmpegLoadLibraryAsyncTask)

}