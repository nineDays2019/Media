package com.juhezi.media

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.juhezi.ffmcli.core.FFmpegCli
import com.juhezi.ffmcli.handler.ExecuteResponseHandler
import kotlinx.android.synthetic.main.activity_main.*
import me.juhezi.eternal.global.logi
import me.juhezi.eternal.global.logw

class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
//        OrangeBridge.test("http://f.us.sinaimg.cn/0008BFPKlx07rQB0dlrO01041200prFo0E010.mp4?label=mp4_hd&template=720x1280.24.0&Expires=1551199278&ssig=5Qm8eOR3hC&KID=unistore,video")
        test.setOnClickListener {
            FFmpegCli.execute(this, "ffmpeg -version", object : ExecuteResponseHandler() {
                var i = 0
                override fun onSuccess(message: String) {
                    logi("\n============\n$message============\n")
                }

                override fun onFailure(message: String) {
                    logi("failure: $message")
                }

                override fun onProgress(message: String) {
                    logw("progress [$i]: $message")
                    i++
                }

            })
        }
    }

}
