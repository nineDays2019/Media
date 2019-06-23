package com.juhezi.orange.media.tools

import android.media.Image

object ImageHelper {

    /**
     * image format: 35
    get data from 3 planes
    pixelStride 1
    rowStride 1920
    width 1920
    height 1080
    buffer size 2088960
    Finished reading data from plane 0
    pixelStride 1
    rowStride 960
    width 1920
    height 1080
    buffer size 522240
    Finished reading data from plane 1
    pixelStride 1
    rowStride 960
    width 1920
    height 1080
    buffer size 522240
    Finished reading data from plane 2
     */
    fun getFullMessage(image: Image) = buildString {
        append("format\t${getEternalFormatById(image.format) ?: "UNDEFINED"}[${image.format}]\n")
        append("get data from ${image.planes.size} planes\n")
        append("width ${image.width}\n")
        append("height ${image.height}")
    }

}
