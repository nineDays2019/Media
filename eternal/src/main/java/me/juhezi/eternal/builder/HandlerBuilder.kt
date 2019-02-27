package me.juhezi.eternal.builder

/**
 * Created by Juhezi[juhezix@163.com] on 2018/7/26.
 */
import android.os.Handler
import android.os.Looper
import android.util.ArrayMap

enum class HandlerType {
    UI,
    CURRENT
}

private object UIHandlerHolder {
    val uiHandler = createUIHandler()
}

/**
 * [String, Handler]
 */
private object HandlerPool {
    val map = ArrayMap<String, Handler>()
}

/**
 * build a Handler
 * Created by Juhezi[juhezix@163.com] on 2017/9/14.
 */
fun buildHandler(type: HandlerType) =
        when (type) {
            HandlerType.UI -> buildUIHandler()
            HandlerType.CURRENT -> buildCurrentHandler()
        }

fun buildHandler(looper: Looper) = buildSpecialHandler(looper)

fun buildHandler(threadName: String) = HandlerPool.map[threadName]

/**
 * Create a Handler in UI Thread. {Singleton!!!}
 */
fun buildUIHandler() = UIHandlerHolder.uiHandler

/**
 * Create a Handler in Current Thread
 */
fun buildCurrentHandler(): Handler {
    val currentThread = Thread.currentThread()
    var currentHandler = HandlerPool.map[currentThread.name]
    if (currentHandler == null) {
        currentHandler = createCurrentHandler()
        HandlerPool.map[currentThread.name] = currentHandler
    }
    return currentHandler
}

/**
 * Create a handler with Special Thread
 */
fun buildSpecialHandler(looper: Looper) = Handler(looper)

private fun createUIHandler() = Handler(Looper.getMainLooper())

private fun createCurrentHandler(): Handler {
    Looper.prepare()
    val handler = Handler()
    Looper.loop()
    return handler
}
