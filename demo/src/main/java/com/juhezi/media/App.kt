package com.juhezi.media

import com.juhezi.ffmcli.core.FFmpegCli
import com.juhezi.ffmcli.handler.LoadLibraryResponseHandler
import me.juhezi.eternal.base.BaseApplication
import me.juhezi.eternal.global.logi

class App : BaseApplication() {

    override fun onCreate() {
        super.onCreate()
        FFmpegCli.loadLibrary(this, object : LoadLibraryResponseHandler() {

            override fun onStart() {
                logi("start")
            }

            override fun onFinish() {
                logi("finish")
            }

            override fun onSuccess() {
                logi("success")

            }

            override fun onFailure() {
                logi("failure")
            }

        })
    }

}