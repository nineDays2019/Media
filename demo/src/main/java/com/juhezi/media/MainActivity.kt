package com.juhezi.media

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.juhezi.ffmcli.core.FFmpegCli
import com.juhezi.ffmcli.handler.ExecuteResponseHandler
import com.juhezi.media.capture.CaptureActivity
import com.juhezi.orange.bridge.OrangeBridge
import kotlinx.android.synthetic.main.activity_main.*
import me.juhezi.eternal.base.BaseActivity
import me.juhezi.eternal.builder.buildScheme
import me.juhezi.eternal.extension.checkPermissionWith
import me.juhezi.eternal.extension.showToast
import me.juhezi.eternal.global.loge
import me.juhezi.eternal.global.logi
import me.juhezi.eternal.global.logw
import me.juhezi.eternal.router.OriginalPicker
import me.juhezi.eternal.router.SchemeUtils
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
            checkPermissionWith(Manifest.permission.READ_EXTERNAL_STORAGE) {
                val intent = OriginalPicker.getIntent(OriginalPicker.Type.IMAGE)
                startActivityForResult(intent, 0x123)
            }
        }
        json.setOnClickListener {
            OrangeBridge.testJson()
        }
        camera.setOnClickListener {
            checkPermissionWith(Manifest.permission.CAMERA) {
                turnTo(CaptureActivity::class.java)
            }
        }

        camera_scheme.setOnClickListener {
            SchemeUtils.openScheme(this, buildScheme {
                scheme = this@MainActivity.getString(R.string.scheme)
                host = "demo"
                path = "capture"
                permission = Manifest.permission.CAMERA // 添加权限
                appendOrUpdateParam(CAPTURE_FRONT to "0") // 默认为后置摄像头
            })
        }
        picture_scheme.setOnClickListener {
            checkPermissionWith(Manifest.permission.READ_EXTERNAL_STORAGE) {
                val intent = OriginalPicker.getIntent(OriginalPicker.Type.IMAGE)
                startActivityForResult(intent, 0x124)
            }
        }
        gpu_image_scheme.setOnClickListener {
            SchemeUtils.openScheme(
                this@MainActivity,
                "eternal://demo/gpu_image?$GPU_FRAGMENT=storage/emulated/0/color.glsl"
            )
        }
        web_scheme.setOnClickListener {
            SchemeUtils.openScheme(this, buildScheme {
                scheme = this@MainActivity.getString(R.string.scheme)
                host = "demo"
                path = "web"
            })
        }
        web.setOnClickListener {
            turnTo(WebActivity::class.java)
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            val uri = data?.data
            if (requestCode == 0x123) {
                if (uri != null) {
                    val intent = Intent(this, PictureActivity::class.java)
                    intent.putExtra(PICTURE_PATH, UriUtils.getPathFromUri(this, uri))
                    startActivity(intent)
                }
            } else if (requestCode == 0x124) {
                SchemeUtils.openScheme(
                    this@MainActivity,
                    "eternal://demo/picture?$PICTURE_PATH=${UriUtils.getPathFromUri(this, uri)}"
                )
            }
        }
    }

}
