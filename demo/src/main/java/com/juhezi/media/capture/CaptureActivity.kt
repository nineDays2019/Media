package com.juhezi.media.capture

import android.os.Bundle
import android.view.WindowManager
import com.juhezi.media.R
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

        savedInstanceState ?: supportFragmentManager.beginTransaction()
            .replace(R.id.container, Camera2BasicFragment.newInstance())
            .commit()

    }

}