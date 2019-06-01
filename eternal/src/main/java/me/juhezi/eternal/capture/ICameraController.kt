package me.juhezi.eternal.capture

import android.hardware.camera2.CameraCharacteristics
import android.util.Size
import android.view.Surface

interface ICameraController {

    fun addPreviewSurface(surface: Surface)

    fun removePreviewSurfaces(surface: Surface)

    fun getAvailableSizes(id: String): List<Size>

    fun openCamera(id: String)

    fun getCameraIdList(): List<String>

    fun getCameraParams(id: String): CameraCharacteristics?

    fun startPreview()

    fun stopPreview()

    fun release()

}