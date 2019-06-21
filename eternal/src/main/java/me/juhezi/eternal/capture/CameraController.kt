package me.juhezi.eternal.capture

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.camera2.*
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.util.Size
import android.view.Surface
import me.juhezi.eternal.extension.i
import me.juhezi.eternal.global.loge
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit

@Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class CameraController(private val context: Context) : ICameraController {

    /**
     * 是否正在预览
     */
    var isPreviewing: Boolean = false
        private set(value) {
            field = value
        }

    private var cameraDevice: CameraDevice? = null

    private var backgroundThread: HandlerThread? = null
    var backgroundHandler: Handler? = null
    var mainHandler: Handler

    private var captureSession: CameraCaptureSession? = null
    private var previewRequestBuilder: CaptureRequest.Builder? = null
    private var previewRequest: CaptureRequest? = null

    /**
     * 避免相机关闭前应用退出
     */
    private val cameraOpenCloseLock = Semaphore(1)

    private var cameraManager: CameraManager =
        context.getSystemService(Context.CAMERA_SERVICE) as CameraManager

    private var cameraIdList: List<String>

    private var previewSurfaceList: MutableList<Surface> = mutableListOf()

    /**
     * 预览用 ImageReader
     */
    var previewImageReaderSurface: Surface? = null

    var cameraOpenedCallback: (() -> Unit)? = null

    /**
     * 相机是否可用
     */
    var cameraAvailable = false
        private set(value) {
            field = value
        }

    init {
        cameraIdList = cameraManager.cameraIdList.toList()
        mainHandler = Handler(Looper.getMainLooper())
    }

    override fun getCameraParams(id: String) = cameraManager.getCameraCharacteristics(id)

    override fun addPreviewSurface(surface: Surface) {
        previewSurfaceList.add(surface)
    }

    override fun removePreviewSurfaces(surface: Surface) {
        previewSurfaceList.remove(surface)
    }

    override fun getAvailableSizes(id: String, format: Int): List<Size> {
        val map = getCameraParams(id).get(
            CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP
        )
        map ?: return emptyList()
        return map.getOutputSizes(format).toList()
    }

    override fun getAvailableFormats(id: String) =
        getCameraParams(id)[CameraCharacteristics
            .SCALER_STREAM_CONFIGURATION_MAP]
            .outputFormats
            .toList()

    override fun getAvailableCameraIds(): List<String> = cameraIdList

    @SuppressLint("MissingPermission")
    override fun openCamera(id: String) {
        try {
            startBackgroundThread()
            if (!cameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw RuntimeException("Time out waiting to lock camera opening.")
            }
            cameraManager.openCamera(id, cameraDeviceStateCallback, backgroundHandler)
        } catch (e: CameraAccessException) {
            loge("Message: ${e.message}")
        } catch (e: InterruptedException) {
            throw RuntimeException("Interrupted while trying to lock camera opening.", e)
        }
    }

    override fun startPreview() {
        if (isPreviewing || cameraDevice == null) {
            return
        }
        captureSession?.close()
        captureSession = null
        previewRequestBuilder = cameraDevice!!.createCaptureRequest(
            CameraDevice.TEMPLATE_PREVIEW
        )
        /* The Surface added must be one of the surfaces included in the most
        * recent call to {@link CameraDevice#createCaptureSession}, when the
        * request is given to the camera device.
        **/
        previewSurfaceList.forEach {
            previewRequestBuilder!!.addTarget(it)
        }
        if (previewImageReaderSurface != null &&
            previewImageReaderSurface!!.isValid
        ) {
            previewRequestBuilder!!.addTarget(previewImageReaderSurface!!)
        }
        cameraDevice!!.createCaptureSession(
            previewSurfaceList.apply {
                if (previewImageReaderSurface != null &&
                    previewImageReaderSurface!!.isValid
                ) {
                    add(previewImageReaderSurface!!)
                }
            },
            object : CameraCaptureSession.StateCallback() {
                override fun onConfigureFailed(session: CameraCaptureSession) {
                    i("Config 失败啦")
                }

                override fun onConfigured(session: CameraCaptureSession) {
                    // Maybe the camera is already closed
                    if (cameraDevice == null) return

                    try {
                        captureSession = session
                        // 设置自动对焦
                        previewRequestBuilder!!.set(
                            CaptureRequest.CONTROL_AF_MODE,
                            CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE
                        )
                        previewRequest = previewRequestBuilder!!.build()
                        captureSession?.setRepeatingRequest(
                            previewRequest!!,
                            null,
                            null
                        )
                        isPreviewing = true
                    } catch (e: CameraAccessException) {
                        loge(e.toString())
                    }
                }

            }, null
        )
    }

    override fun stopPreview() {
        captureSession?.close()
        captureSession = null
        isPreviewing = false
        previewSurfaceList.clear()
    }

    override fun release() {
        try {
            previewSurfaceList.clear()
            cameraOpenCloseLock.acquire()
            captureSession?.close()
            captureSession = null
            cameraDevice?.close()
            cameraDevice = null
            cameraAvailable = false
            stopBackgroundThread()
            isPreviewing = false
        } catch (e: InterruptedException) {
            throw RuntimeException("Interrupted while trying to lock camera closing.", e)
        } finally {
            cameraOpenCloseLock.release()
        }
    }

    /**
     * 开启相机的回调
     */
    private val cameraDeviceStateCallback = object : CameraDevice.StateCallback() {

        override fun onOpened(cameraDevice: CameraDevice) {
            cameraOpenCloseLock.release()
            this@CameraController.cameraDevice = cameraDevice
            cameraAvailable = true
            cameraOpenedCallback?.invoke()
        }

        override fun onDisconnected(cameraDevice: CameraDevice) {
            cameraOpenCloseLock.release()
            cameraDevice.close()
            this@CameraController.cameraDevice = null
        }

        override fun onError(cameraDevice: CameraDevice, error: Int) {
            onDisconnected(cameraDevice)
        }

    }

    private fun startBackgroundThread() {
        backgroundThread = HandlerThread("CameraBackground").also { it.start() }
        backgroundHandler = Handler(backgroundThread?.looper)
    }

    private fun stopBackgroundThread() {
        backgroundThread?.quitSafely()
        try {
            backgroundThread?.join()
            backgroundThread = null
            backgroundHandler = null
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

}