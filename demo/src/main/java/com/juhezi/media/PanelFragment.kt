package com.juhezi.media

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.juhezi.ffmcli.core.FFmpegCli
import com.juhezi.ffmcli.handler.ExecuteResponseHandler
import com.juhezi.media.capture.CaptureActivity
import com.juhezi.orange.bridge.OrangeBridge
import kotlinx.android.synthetic.main.fragment_panel.view.*
import me.juhezi.eternal.base.BaseFragment
import me.juhezi.eternal.extension.checkPermissionWith
import me.juhezi.eternal.extension.showToast
import me.juhezi.eternal.global.loge
import me.juhezi.eternal.global.logi
import me.juhezi.eternal.global.logw
import me.juhezi.eternal.router.OriginalPicker
import me.juhezi.eternal.util.UriUtils

class PanelFragment : BaseFragment() {

    lateinit var rootView: View

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.fragment_panel, container, false)
        rootView.execute_command.setOnClickListener {
            FFmpegCli.execute(
                context!!,
                BuildConfig.CMD.ifEmpty { "ffmpeg -version" },
                object : ExecuteResponseHandler() {
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
        rootView.findViewById<View>(R.id.gpu_image).setOnClickListener {
            turnTo(GPUImageActivity::class.java)
        }
        rootView.pick_picture.setOnClickListener {
            checkPermissionWith(Manifest.permission.READ_EXTERNAL_STORAGE) {
                val intent = OriginalPicker.getIntent(OriginalPicker.Type.IMAGE)
                startActivityForResult(intent, 0x123)
            }
        }
        rootView.findViewById<View>(R.id.json).setOnClickListener {
            OrangeBridge.testJson()
        }
        rootView.findViewById<View>(R.id.camera).setOnClickListener {
            checkPermissionWith(Manifest.permission.CAMERA) {
                turnTo(CaptureActivity::class.java)
            }
        }
        rootView.findViewById<View>(R.id.web).setOnClickListener {
            turnTo(WebActivity::class.java)
        }
        rootView.load.setOnClickListener { load() }
        rootView.turn.setOnClickListener { turn() }
        return rootView
    }

    fun load() {

    }

    fun turn() {
        val intent = Intent(context, ProxyActivity::class.java)
        intent.putExtra("class_name", "todo")
        startActivity(intent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            val uri = data?.data
            if (requestCode == 0x123) {
                if (uri != null) {
                    val intent = Intent(context, PictureActivity::class.java)
                    intent.putExtra(PICTURE_PATH, UriUtils.getPathFromUri(context, uri))
                    startActivity(intent)
                }
            } /*else if (requestCode == 0x124) {
                SchemeUtils.openScheme(
                    context!!,
                    "eternal://demo/picture?$PICTURE_PATH=${UriUtils.getPathFromUri(context, uri)}"
                )
            }*/
        }
    }

}