package me.juhezi.eternal.media.gpuimage.helper

object FPSHelper {
    fun getDelayTime(fps: Int) = if (fps > 0) {
        1000 / fps
    } else {
        1000 / 30
    }
}