package com.juhezi.ffmcli.handler

interface FFmpegLoadLibraryResponseHandler : ResponseHandler {

    fun onFailure()

    fun onSuccess()

}
