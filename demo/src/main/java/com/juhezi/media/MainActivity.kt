package com.juhezi.media

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.juhezi.ffmcli.core.FFmpegCli
import com.juhezi.ffmcli.handler.ExecuteResponseHandler
import kotlinx.android.synthetic.main.activity_gpu_image.*
import kotlinx.android.synthetic.main.activity_main.*
import me.juhezi.eternal.base.BaseActivity
import me.juhezi.eternal.extension.showToast
import me.juhezi.eternal.global.loge
import me.juhezi.eternal.global.logi
import me.juhezi.eternal.global.logw
import me.juhezi.eternal.router.OriginalPicker
import me.juhezi.eternal.util.UriUtils

class MainActivity : BaseActivity() {

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
        gpu_image.setOnClickListener {
            turnTo(GPUImageActivity::class.java)
        }
        pick_picture.setOnClickListener {
            val intent = OriginalPicker.getIntent(OriginalPicker.Type.IMAGE)
            startActivityForResult(intent, 0x123)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 0x123) {
                val uri = data?.data
                if (uri != null) {
                    val intent = Intent(this, PictureActivity::class.java)
                    intent.putExtra(PICTURE_KEY, UriUtils.getPathFromUri(this, uri))
                    startActivity(intent)
                }
            }
        }
    }

}
