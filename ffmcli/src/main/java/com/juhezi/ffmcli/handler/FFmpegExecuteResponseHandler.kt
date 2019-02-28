package com.juhezi.ffmcli.handler

interface FFmpegExecuteResponseHandler : ResponseHandler {

    fun onSuccess(message: String)

    fun onProgress(message: String)

    fun onFailure(message: String)

}