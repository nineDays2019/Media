package me.juhezi.eternal.extension

import android.text.TextUtils
import android.util.Log
import me.juhezi.eternal.global.isDebug

/**
 * Created by Juhezi[juhezix@163.com] on 2017/8/4.
 */
fun Any.i(message: String) = Log.i(javaClass.simpleName, message)

fun Any.e(message: String) = Log.e(javaClass.simpleName, message)

fun Any.w(message: String) = Log.w(javaClass.simpleName, message)

fun Any.v(message: String) = Log.v(javaClass.simpleName, message)

fun Any.d(message: String) = Log.d(javaClass.simpleName, message)

fun Any.i() = Log.i(javaClass.simpleName, this.toString())

fun Any.e() = Log.e(javaClass.simpleName, this.toString())

fun Any.w() = Log.w(javaClass.simpleName, this.toString())

fun Any.v() = Log.v(javaClass.simpleName, this.toString())

fun Any.d() = Log.d(javaClass.simpleName, this.toString())

fun Any.trim(content: String?): String? {
    return content?.trim()
}

fun Any.isEmpty(content: String?) = TextUtils.isEmpty(content)

fun Any.println() = println(this)

fun Any.print() = print(this)

/*inline fun <T : Any> T.desc() = this::class.memberProperties
        .joinToString(separator = ";") { "${it.name} : ${it.getUnsafed(this@desc)}" }
@Suppress("UNCHECKED_CAST")
fun <T, R> KProperty1<T, R>.getUnsafed(receiver: Any): R = get(receiver as T)*/

fun Any.dd(message: String) {
    if (isDebug())
        d(message)
}

fun Any.di(message: String) {
    if (isDebug()) {
        i(message)
    }
}

fun IntArray.value(): Int {
    return if (isEmpty()) {
        0
    } else {
        this[0]
    }
}
