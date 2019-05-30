package com.juhezi.media

import android.os.Bundle
import android.view.WindowManager
import me.juhezi.eternal.base.BaseActivity

class CaptureActivity : BaseActivity() {

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
    }

}