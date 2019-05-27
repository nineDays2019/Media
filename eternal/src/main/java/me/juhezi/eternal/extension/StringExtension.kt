package me.juhezi.eternal.extension

import android.media.ExifInterface
import java.io.IOException


/**
 * 获取图片旋转角度
 */
fun String.getBitmapDegree(): Int {

    var degree = 0
    try {
        // 从指定路径下读取图片，并获取其EXIF信息
        val exifInterface = ExifInterface(this)
        // 获取图片的旋转信息
        val orientation = exifInterface.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL
        )
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> degree = 90
            ExifInterface.ORIENTATION_ROTATE_180 -> degree = 180
            ExifInterface.ORIENTATION_ROTATE_270 -> degree = 270
        }
    } catch (e: IOException) {
        e.printStackTrace()
    }
    return degree
}