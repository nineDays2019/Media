package me.juhezi.eternal.capture

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraCharacteristics
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

    val extratextureViews: MutableList<TextureView> = ArrayList()

    var currentCameraId: String? = null
    var currentPreviewSize: Size? = null

    var fontCameraId = ""   // 前置摄像头
    var backCameraId = ""   // 后置摄像头

    init {
        setupCameraId(cameraController)
        currentCameraId = fontCameraId
        cameraController.getAvailableSizes(currentCameraId!!).forEach {
            i(it.toString())
        }
        currentPreviewSize = cameraController.getAvailableSizes(currentCameraId!!)
            .first()
        cameraController.cameraOpenedCallback = {
            if (isAutoPreview) {
                startPreview()
            }
        }
    }

    /**
     * 设置前后摄像头 ID
     */
    private fun setupCameraId(cameraController: CameraController) {
        cameraController.getCameraIdList().map {
            it to cameraController.getCameraParams(it)
        }.forEach {
            if (!TextUtils.isEmpty(fontCameraId) &&
                !TextUtils.isEmpty(backCameraId)
            )
                return
            if (TextUtils.isEmpty(fontCameraId)) {
                if (it.second.get(CameraCharacteristics.LENS_FACING) ==
                    CameraCharacteristics.LENS_FACING_FRONT
                ) {
                    fontCameraId = it.first
                }
            }

            if (TextUtils.isEmpty(backCameraId)) {

                if (it.second.get(CameraCharacteristics.LENS_FACING) ==
                    CameraCharacteristics.LENS_FACING_BACK
                ) {
                    backCameraId = it.first
                }

            }
        }
    }

    @SuppressLint("Recycle")
    fun startPreview(force: Boolean = false) {
        if (currentPreviewSize == null) return
        fun internalSettleTextureView(textureView: TextureView) {
            // 这里可以不使用 textureView 的 surfaceTexture，而是可以自己创建一个
            // 不可行，无法设置尺寸
            /*if (textureView.width > 0 && textureView.height > 0 &&
                currentPreviewSize!!.width > 0 && currentPreviewSize!!.height > 0
            ) {
                val viewRatio = textureView.width /
                        textureView.height.toFloat()
                val surfaceRatio = currentPreviewSize!!.width /
                        currentPreviewSize!!.height.toFloat()
                var pair = if (viewRatio > surfaceRatio) {  // 高为基准
                    textureView.height * surfaceRatio to textureView.height
                } else {    // 宽为基准
                    textureView.width to textureView.height / surfaceRatio
                }
            } else {*/
            val surfaceTexture = textureView.surfaceTexture
            if (surfaceTexture != null) {
                surfaceTexture.setDefaultBufferSize(currentPreviewSize!!.width, currentPreviewSize!!.height)
                cameraController.addPreviewSurface(Surface(surfaceTexture))
            }
//            }
        }
        if (force) {
            cameraController.stopPreview()
        }
        internalSettleTextureView(textureView)
        extratextureViews.forEach {
            internalSettleTextureView(it)
        }
        cameraController.startPreview()
    }

    fun stopPreview() {
        cameraController.stopPreview()
    }

    /**
     * 生命周期和 Activity 或者 Fragment 绑定即可
     * 打开相机
     */
    fun onResume() {
        if (textureView.isAvailable) {
            textureView.surfaceTextureListener = null
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
//        cameraController.stopPreview()
        cameraController.release()
    }

    fun switchCamera() {
        currentCameraId = if (currentCameraId == fontCameraId) {
            backCameraId
        } else {
            fontCameraId
        }
        onPause()   // 重新执行这一套流程
        onResume()
    }

    fun addExtraPreviewTextureView(textureView: TextureView) {
        extratextureViews.add(textureView)
        handlePreviewAfterChangePreviewTextureView()
    }

    fun addExtraPreviewTextureViews(textureViews: List<TextureView>) {
        extratextureViews.addAll(textureViews)
        handlePreviewAfterChangePreviewTextureView()
    }

    fun removeExtraPreviewTextureView(textureView: TextureView) {
        extratextureViews.remove(textureView)
        handlePreviewAfterChangePreviewTextureView()
    }

    /**
     * 修改预览的 TextureView 之后，处理预览的逻辑
     * 如果现在没有在预览，那么就不应该预览
     * 如果现在在预览，那么就可以预览
     */
    private fun handlePreviewAfterChangePreviewTextureView() {
        if (cameraController.isPreviewing) {    // 当前正在预览，需要重新预览
            startPreview(true)
        }    // 当前不在预览，那么无需其他操作

    }

}