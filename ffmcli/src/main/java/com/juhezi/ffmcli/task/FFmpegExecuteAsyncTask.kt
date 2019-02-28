package com.juhezi.ffmcli.task

import android.os.AsyncTask
import com.juhezi.ffmcli.handler.FFmpegExecuteResponseHandler
import com.juhezi.ffmcli.model.CommandResult
import com.juhezi.ffmcli.model.ShellCommand
import com.juhezi.ffmcli.util.Utils
import me.juhezi.eternal.global.logd
import me.juhezi.eternal.global.loge
import java.util.concurrent.TimeoutException
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader


class FFmpegExecuteAsyncTask(
    val cmd: Array<String>,
    val timeout: Long,
    val ffmpefExecuteResponseHandler: FFmpegExecuteResponseHandler?
) : AsyncTask<Void, String, CommandResult>() {

    var shellCommand = ShellCommand()
    var startTime: Long = 0
    var output = ""
    var process: Process? = null

    override fun onPreExecute() {
        startTime = System.currentTimeMillis()
        ffmpefExecuteResponseHandler?.onStart()
    }

    override fun doInBackground(vararg params: Void?): CommandResult {
        try {
            process = shellCommand.run(cmd)
            if (process == null) {
                return CommandResult.getDummyFailureResponse()
            }
            logd("Running publishing updates method")
            checkAndUpdateProcess()
            return CommandResult.getOutputFromProcess(process!!)
        } catch (e: TimeoutException) {
            loge("FFmpeg time out, ${e.message}")
        } catch (e: Exception) {
            loge("Error running FFmpeg ${e.message}")
        } finally {
            Utils.destroyProcess(process)
        }
        return CommandResult.getDummyFailureResponse()
    }

    override fun onProgressUpdate(vararg values: String?) {
        if (values[0] != null) {
            ffmpefExecuteResponseHandler?.onProgress(values[0]!!)
        }
    }

    override fun onPostExecute(result: CommandResult?) {
        output += result?.output
        if (result != null && result.success) {
            ffmpefExecuteResponseHandler?.onSuccess(output)
        } else {
            ffmpefExecuteResponseHandler?.onFailure(output)
        }
        ffmpefExecuteResponseHandler?.onFinish()
    }

    @Throws(TimeoutException::class, InterruptedException::class)
    private fun checkAndUpdateProcess() {
        while (!Utils.isProcessCompleted(process)) {
            // checking if process is completed
            if (Utils.isProcessCompleted(process)) {
                return
            }

            // Handle timeout
            if (timeout != java.lang.Long.MAX_VALUE && System.currentTimeMillis() > startTime + timeout) {
                throw TimeoutException("FFmpeg timed out")
            }

            try {
                val reader = BufferedReader(InputStreamReader(process?.errorStream))
                var line: String = reader.readLine()
                while (line != null) {
                    if (isCancelled) {
                        return
                    }
                    output += line + "\n"
                    publishProgress(line)
                    line = reader.readLine()
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }
    }

}