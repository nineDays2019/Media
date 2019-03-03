package com.juhezi.ffmcli.task

import android.annotation.SuppressLint
import android.content.Context
import android.os.AsyncTask
import com.juhezi.ffmcli.core.FFmpegOperation
import com.juhezi.ffmcli.handler.FFmpegLoadLibraryResponseHandler

class FFmpegLoadLibraryAsyncTask(
    private val context: Context,
    private val ffmpegLoadLibraryResponseHandler: FFmpegLoadLibraryResponseHandler?
) : AsyncTask<Void, Void, Boolean>() {

    override fun onPreExecute() {
        ffmpegLoadLibraryResponseHandler?.onStart()
    }

    override fun doInBackground(vararg voids: Void): Boolean {
        var result =  FFmpegOperation.loadLibrary(context)
        return result
    }

    override fun onPostExecute(result: Boolean?) {
        if (result != null && result) {
            ffmpegLoadLibraryResponseHandler?.onSuccess()
        } else {
            ffmpegLoadLibraryResponseHandler?.onFailure()
        }
        ffmpegLoadLibraryResponseHandler?.onFinish()
    }

}
