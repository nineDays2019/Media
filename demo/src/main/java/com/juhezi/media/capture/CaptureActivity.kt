package com.juhezi.media.capture

import android.os.Bundle
import android.view.TextureView
import android.view.WindowManager
import com.juhezi.media.R
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
        val textureView01 = findViewById<TextureView>(R.id.tv_capture_00)
        captureController = CaptureController(this, textureView01)
    }

    override fun onResume() {
        super.onResume()
        captureController.onResume()
    }

    override fun onPause() {
        super.onPause()
        captureController.onPause()
    }

    override fun onDestroy() {
        captureController.onDestroy()
        super.onDestroy()
    }

}