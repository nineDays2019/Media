package com.juhezi.media

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.WindowManager
import kotlinx.android.synthetic.main.activity_picture.*
import me.juhezi.eternal.base.BaseActivity
import me.juhezi.eternal.gpuimage.EternalGPUImage
import me.juhezi.eternal.gpuimage.EternalGPUImageFilter

const val PICTURE_KEY = "picture_key"

class PictureActivity : BaseActivity() {

    private lateinit var gpuImage: EternalGPUImage
    private var picturePath = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setContentView(R.layout.activity_picture)
        hideBottomUIMenu()
        showContent()
        toolBarVisibility = false
        handleIntent(intent)
        gpuImage = EternalGPUImage(this)
        gpuImage.glSurfaceView = glsv_picture_show
        gpuImage.setFilter(EternalGPUImageFilter())
        showPicture(picturePath)
    }

    private fun showPicture(path: String) {
        // 加载 Bitmap 应该是异步的，不过是 Demo，所以影响不大
        gpuImage.setImage(BitmapFactory.decodeFile(path))
    }

    private fun handleIntent(intent: Intent) {
        picturePath = intent.getStringExtra(PICTURE_KEY)
    }

}