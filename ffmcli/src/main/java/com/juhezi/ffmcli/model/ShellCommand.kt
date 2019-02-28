package com.juhezi.ffmcli.model

import com.juhezi.ffmcli.util.Utils
import me.juhezi.eternal.global.loge
import java.io.IOException

class ShellCommand {

    fun run(command: Array<String>): Process? {
        var process: Process? = null
        try {
            process = Runtime.getRuntime().exec(command)
        } catch (e: IOException) {
            loge("Exception while trying to run: $command,\n${e.message}")
        }
        return process
    }

    fun runWaitFor(command: Array<String>): CommandResult {
        val process = run(command)
        var exitValue: Int? = null
        var output: String? = null
        try {
            if (process != null) {
                exitValue = process.waitFor()
                output = if (CommandResult.success(exitValue)) {
                    Utils.convertInputStreamToString(process.inputStream)
                } else {
                    Utils.convertInputStreamToString(process.errorStream)
                }
            }
        } catch (e: InterruptedException) {
            loge("Interrupt exception, ${e.message}")
        } finally {
            Utils.destroyProcess(process)
        }
        return CommandResult(CommandResult.success(exitValue), output ?: "")
    }

}