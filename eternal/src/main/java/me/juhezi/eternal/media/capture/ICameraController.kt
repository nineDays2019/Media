package me.juhezi.eternal.media.capture

import android.hardware.camera2.CameraCharacteristics
import android.util.Size
import android.view.Surface

interface ICameraController {

    fun addPreviewSurface(surface: Surface)

    fun removePreviewSurfaces(surface: Surface)


    fun openCamera(id: String)

    fun getAvailableCameraIds(): List<String>

    fun getAvailableFormats(id: String): List<Int>

    /**
     * Size 和 format 是相互绑定的
     */
    fun getAvailableSizes(id: String, format: Int): List<Size>

    fun getCameraParams(id: String): CameraCharacteristics?

    fun startPreview()

    fun stopPreview()

    fun release()

}