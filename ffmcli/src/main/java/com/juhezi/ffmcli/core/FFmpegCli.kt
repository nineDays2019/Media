package com.juhezi.ffmcli.core

import android.content.Context
import com.juhezi.ffmcli.exception.FFmpegCommandAlreadyRunningException
import com.juhezi.ffmcli.handler.FFmpegExecuteResponseHandler
import com.juhezi.ffmcli.handler.FFmpegLoadLibraryResponseHandler
import com.juhezi.ffmcli.task.FFmpegExecuteAsyncTask
import com.juhezi.ffmcli.task.FFmpegLoadLibraryAsyncTask

object FFmpegCli {

    var ffmpegLoadLibraryAsyncTask: FFmpegLoadLibraryAsyncTask? = null
    var ffmpegExecuteAsyncTask: FFmpegExecuteAsyncTask? = null

    fun loadLibrary(context: Context, ffmpegLoadLibraryResponseHandler: FFmpegLoadLibraryResponseHandler) {
        ffmpegLoadLibraryAsyncTask =
            FFmpegLoadLibraryAsyncTask(context, ffmpegLoadLibraryResponseHandler)
        ffmpegLoadLibraryAsyncTask!!.execute()
    }

    fun execute(
        environmentVars: Map<String, String>?,
        cmd: Array<String>,
        ffmpegExecuteResponseHandler: FFmpegExecuteResponseHandler
    ) {
        if (ffmpegExecuteAsyncTask != null && !ffmpegExecuteAsyncTask!!.isProcessCompleted()) {
            throw FFmpegCommandAlreadyRunningException(
                "FFmpeg command is already running, you are only allowed to run single command."
            )
        }

    }

    fun execute(cmd: Array<String>, ffmpegExecuteResponseHandler: FFmpegExecuteResponseHandler) {
        FFmpegCli.execute(null, cmd, ffmpegExecuteResponseHandler)
    }

    fun isFFmpegCommandRunning(): Boolean {
        return true
    }

    fun killRunningProcesses(): Boolean {
        return false
    }
}