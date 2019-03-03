package com.juhezi.ffmcli.model

import com.juhezi.ffmcli.util.Utils
import me.juhezi.eternal.global.logi

class CommandResult(val success: Boolean, val output: String) {

    companion object {
        fun getDummyFailureResponse(): CommandResult {
            return CommandResult(false, "")
        }

        fun getOutputFromProcess(process: Process): CommandResult {
            val output = if (success(process.exitValue())) {
                Utils.convertInputStreamToString(process.inputStream)
            } else {
                Utils.convertInputStreamToString(process.errorStream)
            }
            return CommandResult(success(process.exitValue()), output ?: "")
        }

        fun success(exitValue: Int?) = exitValue != null && exitValue == 0

    }

}