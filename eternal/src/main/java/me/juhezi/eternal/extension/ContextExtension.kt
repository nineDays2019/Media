package me.juhezi.eternal.extension

import android.content.Context
import android.graphics.Point
import android.net.Uri
import android.support.annotation.RawRes
import android.view.WindowManager
import android.widget.Toast
import me.juhezi.eternal.util.UriUtils
import java.io.BufferedReader
import java.io.InputStreamReader

fun Context.getScreenWidth(): Int {
    val wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
    val p = Point()
    wm.defaultDisplay.getSize(p)
    return p.x
}

fun Context.getScreenHeight(): Int {
    val wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
    val p = Point()
    wm.defaultDisplay.getSize(p)
    return p.y + getBottomNavBarHeight()
}

fun Context.getScreenHeightNoNav(): Int {
    val wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
    val p = Point()
    wm.defaultDisplay.getSize(p)
    return p.y
}

fun Context.getBottomNavBarHeight(): Int {
    if (!hasNavBar()) {
        return 0
    }
    val resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
    return if (resourceId > 0) {
        resources.getDimensionPixelSize(resourceId)
    } else 0
}

fun Context.hasNavBar(): Boolean {
    val wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
    val display = wm.defaultDisplay
    val size = Point()
    val realSize = Point()
    display.getSize(size)
    display.getRealSize(realSize)
    return realSize.y != size.y
}


fun Context.dip2px(dpValue: Float): Int {
    val scale = resources.displayMetrics.density
    return (dpValue * scale + 0.5f).toInt()
}

fun Context.px2dip(pxValue: Float): Int {
    val scale = resources.displayMetrics.density
    return (pxValue / scale + 0.5f).toInt()
}

fun Context.getScreenInches(): Double {
    val dm = resources.displayMetrics
    val width = dm.widthPixels
    val height = dm.heightPixels
    val wi = width.toDouble() / dm.xdpi.toDouble()
    val hi = height.toDouble() / dm.ydpi.toDouble()
    val x = Math.pow(wi, 2.0)
    val y = Math.pow(hi, 2.0)
    return Math.sqrt(x + y)
}

fun Context.getScreenWidthInches(): Double {
    val dm = resources.displayMetrics
    val width = dm.widthPixels
    return width.toDouble() / dm.xdpi.toDouble()
}

fun Context.getScreenHeightInches(): Double {
    val dm = resources.displayMetrics
    val height = dm.heightPixels
    return height.toDouble() / dm.ydpi.toDouble()
}

fun Context.showToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

/**
 * 获取 URI 对应的真实路径
 */
fun Context.getRealPathFromUri(uri: Uri): String = UriUtils.getPathFromUri(this, uri)

fun Context.readContentFromRaw(@RawRes resourceId: Int): String {
    return try {
        val inputStream = resources.openRawResource(resourceId)
        val inputStreamReader = InputStreamReader(inputStream)
        val bufferedReader = BufferedReader(inputStreamReader)
        buildString {
            var nextLine = bufferedReader.readLine()
            while (nextLine != null) {
                append(nextLine)
                append('\n')
                nextLine = bufferedReader.readLine()
            }
        }
    } catch (e: Exception) {
        ""
    }
}
