package com.juhezi.media.capture

import android.os.Bundle
import android.view.WindowManager
import com.juhezi.media.R
import kotlinx.android.synthetic.main.activity_capture.*
import me.juhezi.eternal.base.BaseActivity
import me.juhezi.eternal.capture.CaptureController

class CaptureActivity : BaseActivity() {

    private lateinit var captureController: CaptureController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setContentView(R.layout.activity_capture)
        hideBottomUIMenu()
        showContent()
        toolBarVisibility = false
        captureController = CaptureController(this, tv_capture_00)
        iv_switch.setOnClickListener {
            captureController.switchCamera()
        }
        var flag1 = 0
        var flag2 = 0
        preview.setOnClickListener {
            if (flag1 % 2 == 0) {
                captureController.stopPreview()
            } else {
                captureController.startPreview()
            }
            flag1++
        }
        change.setOnClickListener {
            if (flag2 % 2 == 0) {
                captureController.addExtraPreviewTextureView(tv_capture_01)
            } else {
                captureController.removeExtraPreviewTextureView(tv_capture_01)
            }
            flag2++
        }
    }

    override fun onResume() {
        super.onResume()
        captureController.onResume()
    }

    override fun onPause() {
        super.onPause()
        captureController.onPause()
    }

}