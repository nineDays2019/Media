package com.juhezi.ffmcli.task

import android.os.AsyncTask
import com.juhezi.ffmcli.handler.FFmpegExecuteResponseHandler
import com.juhezi.ffmcli.model.CommandResult
import com.juhezi.ffmcli.model.ShellCommand
import com.juhezi.ffmcli.util.Utils
import me.juhezi.eternal.global.logd
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

class FFmpegExecuteAsyncTask(
    val cmd: Array<String>,
    val ffmpefExecuteResponseHandler: FFmpegExecuteResponseHandler?
) : AsyncTask<Void, String, CommandResult>() {

    var shellCommand = ShellCommand()
    var output = ""
    var process: Process? = null

    override fun onPreExecute() {
        ffmpefExecuteResponseHandler?.onStart()
    }

    override fun doInBackground(vararg params: Void?): CommandResult {
        return try {
            process = shellCommand.run(cmd)
            if (process == null) {
                return CommandResult.getDummyFailureResponse()
            }
            logd("Running publishing updates method ")
            checkAndUpdateProcess()
            CommandResult.getOutputFromProcess(process!!)
        } catch (e: Exception) {
            CommandResult.getDummyFailureResponse()
        } finally {
            Utils.destroyProcess(process)
        }
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

    @Throws(InterruptedException::class)
    private fun checkAndUpdateProcess() {
        while (!Utils.isProcessCompleted(process)) {
            // checking if process is completed
            if (Utils.isProcessCompleted(process)) {
                return
            }
            // 进程还没有完成
            try {
                val reader = BufferedReader(InputStreamReader(process?.errorStream))
                var line = reader.readLine()
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

    fun isProcessCompleted(): Boolean {
        return Utils.isProcessCompleted(process)
    }

}