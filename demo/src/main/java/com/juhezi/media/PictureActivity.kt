package com.juhezi.media

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.text.TextUtils
import android.view.WindowManager
import kotlinx.android.synthetic.main.activity_picture.*
import me.juhezi.eternal.base.BaseActivity
import me.juhezi.eternal.extension.getBitmapDegree
import me.juhezi.eternal.extension.i
import me.juhezi.eternal.extension.isEmpty
import me.juhezi.eternal.extension.showToast
import com.juhezi.orange.media.gpuimage.EternalGPUImage
import com.juhezi.orange.media.gpuimage.filter.EternalGPUImageFilter


class PictureActivity : BaseActivity() {

    private lateinit var gpuImage: EternalGPUImage
    private var picturePath: String? = ""
    private val filter = EternalGPUImageFilter()

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
        gpuImage.setFilter(filter)
//        PermissionService.requestPermission(READ_EXTERNAL_STORAGE, this)
        showPicture(picturePath)
    }

    private fun showPicture(path: String?) {
        if (TextUtils.isEmpty(path)) {
            showToast("路径为空!!!")
            return
        }
        i("旋转角为：${path!!.getBitmapDegree()}")
        // 加载 Bitmap 应该是异步的，不过是 Demo，所以影响不大
        Thread {
            val bitmap = BitmapFactory.decodeFile(path)
            filter.setBitmap(bitmap, rotation = path.getBitmapDegree().toFloat())
        }.start()
    }

    private fun handleIntent(intent: Intent?) {
        picturePath = intent?.getStringExtra(PICTURE_PATH)
        if (isEmpty(picturePath)) {
            val uri = intent?.data
            picturePath = uri?.getQueryParameter(PICTURE_PATH)
        }
    }

}