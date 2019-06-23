package com.juhezi.orange.media.capture

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Matrix
import android.graphics.RectF
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraCharacteristics
import android.media.ImageReader
import android.text.TextUtils
import android.util.Size
import android.view.Surface
import android.view.TextureView
import me.juhezi.eternal.extension.i
import com.juhezi.orange.media.tools.ImageHelper.getFullMessage

class CaptureController(
    var context: Context,
    var textureView: TextureView,
    defaultFront: Boolean = true,   // 默认前置摄像头
    var isAutoPreview: Boolean = true,  // 开启相机后，自动预览
    var useImageReaderForPreview: Boolean = false  // 是否使用 ImageReader 接收预览数据
) {
    val cameraController = CameraController(context)

    private val extraTextureViews: MutableList<TextureView> = ArrayList()

    var currentCameraId: String
    var currentPreviewSize: Size
    var currentFormat: Int
    /**
     * Camera sensor 的旋转角度
     */
    private var currentSensorOrientation = 0

    var frontCameraId = ""   // 前置摄像头
    var backCameraId = ""   // 后置摄像头

    private var previewImageReader: ImageReader? = null


    // 非主线程
    private var onPreviewImageAvailableListener: ImageReader.OnImageAvailableListener =
        ImageReader.OnImageAvailableListener {
            // 接受预览数据
            val image = it.acquireNextImage()
//            i("Preview: ${getFullMessage(image)}")
            image.close()
        }

    //    characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION)
    init {
        setupCameraId(cameraController)
        currentCameraId = if (defaultFront) frontCameraId else backCameraId
        cameraController.cameraOpenedCallback = {
            if (isAutoPreview) {
                startPreview()
            }
        }
        currentFormat = getDefaultPreviewFormat()
        currentPreviewSize = getDefaultPreviewSize()
    }

    /**
     * 设置前后摄像头 ID
     */
    private fun setupCameraId(cameraController: CameraController) {
        cameraController.getAvailableCameraIds().map {
            it to cameraController.getCameraParams(it)
        }.forEach {
            if (!TextUtils.isEmpty(frontCameraId) &&
                !TextUtils.isEmpty(backCameraId)
            )
                return
            if (TextUtils.isEmpty(frontCameraId)) {
                if (it.second.get(CameraCharacteristics.LENS_FACING) ==
                    CameraCharacteristics.LENS_FACING_FRONT
                ) {
                    frontCameraId = it.first
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

        // Log
        getPreviewFormats().apply {
            i("AvailableFormats: ")
            forEachIndexed { index, format ->
                i("[$index] 0x${format.toString(16)}")
            }
        }
        getPreviewSizes().apply {
            i("AvailableSizes: ")
            forEachIndexed { index, size ->
                i("[$index] $size")
            }
        }

        fun internalSettleTextureView(textureView: TextureView) {
            // 这里可以不使用 textureView 的 surfaceTexture，而是可以自己创建一个
            // 不可行，无法设置尺寸
            if (textureView.width > 0 && textureView.height > 0 &&
                currentPreviewSize.width > 0 && currentPreviewSize.height > 0
            ) {
                val viewRatio = textureView.width /
                        textureView.height.toFloat()
                val surfacePair = if ((currentSensorOrientation / 90) % 2 == 0) {
                    currentPreviewSize.width to currentPreviewSize.height
                } else {
                    currentPreviewSize.height to currentPreviewSize.width
                }
                val surfaceRatio = surfacePair.first /
                        surfacePair.second.toFloat()
                val viewPair = if (viewRatio > surfaceRatio) {  // 高为基准
                    textureView.height * surfaceRatio to textureView.height.toFloat()
                } else {    // 宽为基准
                    textureView.width.toFloat() to textureView.width / surfaceRatio
                }
                val matrix = Matrix()
                matrix.setRectToRect(
                    RectF(
                        0f, 0f,
                        textureView.width.toFloat(),
                        textureView.height.toFloat()
                    ),
                    RectF(
                        textureView.width / 2 - viewPair.first / 2,
                        textureView.height / 2 - viewPair.second / 2,
                        textureView.width / 2 + viewPair.first / 2,
                        textureView.height / 2 + viewPair.second / 2
                    ),
                    Matrix.ScaleToFit.FILL
                )
                // 有 Bug
//                val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
//                val displayRotation = wm.defaultDisplay.rotation * 90f
//                matrix.postRotate(
//                    -displayRotation,
//                    textureView.width / 2f,
//                    textureView.height / 2f
//                )
                textureView.post {
                    textureView.setTransform(matrix)
                }
                val surfaceTexture = textureView.surfaceTexture
                if (surfaceTexture != null) {
                    // 这个不需要改
                    surfaceTexture.setDefaultBufferSize(
                        currentPreviewSize.width,
                        currentPreviewSize.height
                    )
                    cameraController.addPreviewSurface(Surface(surfaceTexture))
                }
            } else {
                val surfaceTexture = textureView.surfaceTexture
                if (surfaceTexture != null) {
                    surfaceTexture.setDefaultBufferSize(currentPreviewSize.width, currentPreviewSize.height)
                    cameraController.addPreviewSurface(Surface(surfaceTexture))
                }
            }
        }
        if (force) {
            cameraController.stopPreview()
        }
        internalSettleTextureView(textureView)
        extraTextureViews.forEach {
            internalSettleTextureView(it)
        }
        previewImageReader?.close()
        previewImageReader = null

        // Notice：分辨率和图片格式是保持一致的
        if (useImageReaderForPreview) {
            previewImageReader = ImageReader.newInstance(
                currentPreviewSize.width,
                currentPreviewSize.height,
                currentFormat, 1
            ).apply {
                setOnImageAvailableListener(
                    onPreviewImageAvailableListener,
                    cameraController.backgroundHandler
                )
            }
            cameraController.previewImageReaderSurface =
                previewImageReader!!.surface
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
        currentSensorOrientation = cameraController
            .getCameraParams(currentCameraId)[CameraCharacteristics
            .SENSOR_ORIENTATION] ?: 0
        if (textureView.isAvailable) {
            textureView.surfaceTextureListener = null
            if (!TextUtils.isEmpty(currentCameraId)) {
                cameraController.openCamera(currentCameraId)
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
                        cameraController.openCamera(currentCameraId)
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
        previewImageReader?.close()
        previewImageReader = null
    }

    fun switchCamera() {
        currentCameraId = if (currentCameraId == frontCameraId) {
            backCameraId
        } else {
            frontCameraId
        }
        // 切换摄像头之后，format 和 size 要全部切换成默认的
        currentFormat = getDefaultPreviewFormat()
        currentPreviewSize = getDefaultPreviewSize()
        onPause()   // 重新执行这一套流程
        onResume()
    }

    fun switchPreviewSize(size: Size) {
        if (size == currentPreviewSize) return
        this.currentPreviewSize = size
        onPause()
        onResume()
    }

    fun switchPreviewFormat(format: Int) {
        if (format == currentFormat) return
        currentFormat = format
        onPause()
        onResume()
    }

    fun addExtraPreviewTextureView(textureView: TextureView) {
        extraTextureViews.add(textureView)
        handlePreviewAfterChangePreviewTextureView()
    }

    fun addExtraPreviewTextureViews(textureViews: List<TextureView>) {
        extraTextureViews.addAll(textureViews)
        handlePreviewAfterChangePreviewTextureView()
    }

    fun removeExtraPreviewTextureView(textureView: TextureView) {
        extraTextureViews.remove(textureView)
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

    // currentFormat 这个时候是空的，应该如何做
    fun getPreviewSizes() =
        cameraController.getAvailableSizes(currentCameraId, currentFormat)

    fun getPreviewFormats() =
        cameraController.getAvailableFormats(currentCameraId)

    private fun getDefaultPreviewSize() = getPreviewSizes().first()

    private fun getDefaultPreviewFormat() = getPreviewFormats().first()

}