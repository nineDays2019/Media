package com.juhezi.media.capture

import android.util.Size
import java.lang.Long.signum
import java.util.*

/**
 * 通过面积确定大小
 */
internal class CompareSizesByArea : Comparator<Size> {

    // We cast here to ensure the multiplications won't overflow
    override fun compare(lhs: Size, rhs: Size) =
            signum(lhs.width.toLong() * lhs.height - rhs.width.toLong() * rhs.height)

}
