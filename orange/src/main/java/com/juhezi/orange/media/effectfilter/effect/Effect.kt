package com.juhezi.orange.media.effectfilter.effect

import android.content.Context
import java.lang.ref.WeakReference
import java.util.*

abstract class Effect(var id: Int) {

    enum class EffectType {
        INPUT,
        NORMAL
    }

    protected var type: EffectType = EffectType.NORMAL

    protected var contextRef: WeakReference<Context>? = null

    protected var runOnDrawQueue: Queue<() -> Unit> = LinkedList()

    fun init(context: Context) {
        contextRef = WeakReference(context)
        config()
    }

    fun runOnDraw(runnable: () -> Unit) {
        synchronized(runOnDrawQueue) {
            runOnDrawQueue.add(runnable)
        }
    }

    protected abstract fun config()

    abstract fun update()

}