package me.juhezi.eternal.capture

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.SurfaceTexture
import android.text.TextUtils
import android.util.Size
import android.view.Surface
import android.view.TextureView
import me.juhezi.eternal.extension.i

class CaptureController(
    var context: Context,
    var textureView: TextureView,
    var isAutoPreview: Boolean = true
) {
    val cameraController = CameraController(context)

    var currentCameraId: String? = null
    var currentPreviewSize: Size? = null

    init {
        // demo 都选择第一个、以后再改
        currentCameraId = cameraController.getCameraIdList().first()
        cameraController.getAvailableSizes(currentCameraId!!).forEach {
            i(it.toString())
        }
        currentPreviewSize = cameraController.getAvailableSizes(currentCameraId!!).first()
        cameraController.cameraOpenedCallback = {
            if (isAutoPreview) {
                startPreview()
            }
        }
    }

    @SuppressLint("Recycle")
    fun startPreview() {
        if (currentPreviewSize == null) return
        // add Surface
        val texture = textureView.surfaceTexture
        // 设置预览尺寸
        texture.setDefaultBufferSize(currentPreviewSize!!.width, currentPreviewSize!!.height)
        val surface = Surface(texture)
        cameraController.addPreviewSurface(surface)
        cameraController.startPreview()
    }

    /**
     * 生命周期和 Activity 或者 Fragment 绑定即可
     * 打开相机
     */
    fun onResume() {
        if (textureView.isAvailable) {
            if (!TextUtils.isEmpty(currentCameraId)) {
                cameraController.openCamera(currentCameraId!!)
            }
        } else {
            textureView.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) {
                    i("update")
                }

                override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?): Boolean {
                    return true
                }

                override fun onSurfaceTextureAvailable(surface: SurfaceTexture?, width: Int, height: Int) {
                    if (!TextUtils.isEmpty(currentCameraId)) {
                        cameraController.openCamera(currentCameraId!!)
                    }
                }

                override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture?, width: Int, height: Int) {
                }
            }

        }
    }

    fun onPause() {
        cameraController.stopPreview()
    }

    fun onDestroy() {
        cameraController.release()
    }

}