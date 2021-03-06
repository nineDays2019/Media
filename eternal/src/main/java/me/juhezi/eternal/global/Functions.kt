package me.juhezi.eternal.global

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.telephony.TelephonyManager
import android.util.Log
import android.view.MotionEvent
import android.view.View
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull
import me.juhezi.eternal.BuildConfig
import me.juhezi.eternal.base.BaseApplication
import me.juhezi.eternal.builder.buildUIHandler
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList
import kotlin.coroutines.Continuation

/**
 * Created by Juhezi[juhezix@163.com] on 2018/7/26.
 */

fun run(handler: Handler, runnable: Runnable) = handler.post(runnable)

fun runInUIThread(runnable: Runnable) = buildUIHandler().post(runnable)

fun isDebug() = BuildConfig.DEBUG

fun logi(message: String?) {
    if (isDebug()) {
        Log.i(TAG, message)
    }
}

fun logd(message: String?) {
    if (isDebug()) {
        Log.d(TAG, message)
    }
}

fun logw(message: String?) {
    if (isDebug()) {
        Log.w(TAG, message)
    }
}

fun loge(message: String?) {
    if (isDebug()) {
        Log.e(TAG, message)
    }
}

@SuppressLint("MissingPermission", "HardwareIds")
fun getIMEI(): String {
    val telManager = BaseApplication.getApplicationContext()
        .getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
    return telManager.deviceId
}

fun log(tag: String, enable: Boolean = true): (String) -> Unit = { message: String ->
    if (enable) {
        Log.i(tag, message)
    }
}

/**
 * 设置点击特效
 * 必须要实现点击事件
 */
fun applyClickEffect(view: View, effect: (View, Boolean) -> Unit) {
    view.setOnTouchListener { _, event ->
        when (event.action) {
            MotionEvent.ACTION_DOWN ->
                effect(view, true)
            MotionEvent.ACTION_UP ->
                effect(view, false)
            MotionEvent.ACTION_CANCEL ->
                effect(view, false)
        }
        false
    }
}

fun applyClickEffect(views: Array<View>, effect: (View, Boolean) -> Unit) {
    views.forEach {
        applyClickEffect(it, effect)
    }
}

fun createAlphaEffect(alpha: Float) = { view: View, enable: Boolean ->
    if (enable) {
        view.alpha = alpha
    } else {
        view.alpha = 1f
    }
}

fun createScaleEffect(scale: Float) = { view: View, enable: Boolean ->
    if (enable) {
        view.scaleX = scale
        view.scaleY = scale
    } else {
        view.scaleX = 1f
        view.scaleY = 1f
    }
}

fun generateRandomID() = UUID.randomUUID().toString()

/**
 * 对时间戳进行格式化
 */
fun formatTime(time: Long, format: String = "yyyy年 MM月dd日 HH:mm"): String {
    val dateFormat = SimpleDateFormat(format)
    return dateFormat.format(time)
}

fun <T> buildList(
    closure: MutableList<T>.() -> Unit,
    constructor: () -> MutableList<T> = { ArrayList<T>() }
) =
    constructor().apply(closure)

fun range(start: Int = 0, end: Int): List<Int> = buildList({
    // 不包含结尾元素
    for (i in start until end) {
        add(i)
    }
})

inline fun <reified T> Pair<T, T>.toArray() = arrayOf(first, second)

fun Pair<Float, Float>.toFloatArray() = floatArrayOf(first, second)

fun Pair<Int, Int>.toFloatArray2() = floatArrayOf(first.toFloat(), second.toFloat())

inline fun <reified T> concatenate(a: Array<T>, b: Array<T>): Array<T> {
    val aLen = a.size
    val bLen = b.size

    return Array(aLen + bLen) {
        if (it < aLen) { // a
            a[it]
        } else { // b
            b[it - aLen]
        }
    }
}

fun judge(closure: () -> Boolean) = closure()

suspend inline fun <T> suspendCoroutineWithTimeout(
    timeMillis: Long,
    crossinline block: (Continuation<T>) -> Unit
) = withTimeout(timeMillis) {
    suspendCancellableCoroutine(block = block)
}

suspend inline fun <T> suspendCoroutineWithTimeoutOrNull(
    timeMillis: Long,
    crossinline block: (Continuation<T>) -> Unit
) = withTimeoutOrNull(timeMillis) {
    suspendCancellableCoroutine(block = block)
}