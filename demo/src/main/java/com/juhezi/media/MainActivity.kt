package com.juhezi.media

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.juhezi.ffmcli.core.FFmpegCli
import com.juhezi.ffmcli.handler.ExecuteResponseHandler
import com.juhezi.orange.bridge.OrangeBridge
import kotlinx.android.synthetic.main.activity_main.*
import me.juhezi.eternal.extension.i
import me.juhezi.eternal.extension.showToast
import me.juhezi.eternal.global.loge
import me.juhezi.eternal.global.logi
import me.juhezi.eternal.global.logw

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        execute_command.setOnClickListener {
            FFmpegCli.execute(this, BuildConfig.CMD.ifEmpty { "ffmpeg -version" }, object : ExecuteResponseHandler() {
                var i = 0
                override fun onSuccess(message: String) {
                    logi("success: \n$message")
                    showToast("Success")
                }

                override fun onFailure(message: String) {
                    loge("failure: \n$message")
                    showToast("Failure")
                }

                override fun onProgress(message: String) {
                    logw("progress [$i]: $message")
                    i++
                }

            })
        }
        button1.setOnClickListener {
            Thread {
                i("Start")
                OrangeBridge.test()
            }.start()
        }
    }

}
