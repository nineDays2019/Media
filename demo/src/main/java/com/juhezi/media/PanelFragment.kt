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
import me.juhezi.eternal.other.EShell
import me.juhezi.eternal.other.ShellResult
import me.juhezi.eternal.router.OriginalPicker
import me.juhezi.eternal.util.UriUtils
import me.juhezi.mediademo.KEY_URL
import me.juhezi.mediademo.MainActivity
import me.juhezi.mediademo.VideoPlayerActivity

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
                object : EShell.Callback() {

                    override fun onFinish(
                        shellResult: ShellResult?,
                        returnCode: Int,
                        costTimeMs: Long
                    ) {

                        logi("Message is \n${shellResult?.message}, returnCode is $returnCode, costTimeMs is $costTimeMs")

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
        rootView.audio.setOnClickListener {
            turnTo(AudioActivity::class.java)
        }
        rootView.android_video_play.setOnClickListener {
            checkPermissionWith(Manifest.permission.READ_EXTERNAL_STORAGE) {
                val intent = OriginalPicker.getIntent(OriginalPicker.Type.VIDEO)
                startActivityForResult(intent, 0x124)
            }
        }
        return rootView
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
            } else if (requestCode == 0x124) {
                if (uri != null) {
                    val intent = Intent(context, VideoPlayerActivity::class.java)
                    intent.putExtra(KEY_URL, UriUtils.getPathFromUri(context, uri))
                    startActivity(intent)
                }
            }
        }
    }

}