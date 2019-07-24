package me.juhezi.eternal.base

import android.app.Activity
import android.app.Application
import android.os.Bundle
import me.juhezi.eternal.global.loge
import me.juhezi.eternal.other.CrashHandler

/**
 * Created by Juhezi[juhezix@163.com] on 2018/7/24.
 */
open class BaseApplication : Application() {

    companion object {
        @JvmStatic
        private lateinit var sContext: BaseApplication

        fun getApplicationContext() = sContext
    }

    override fun onCreate() {
        super.onCreate()
        sContext = this
        CrashHandler(this)
        registerLifecycle()
    }

    private fun registerLifecycle() {
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityPaused(activity: Activity?) {
                loge("ActivityPaused: $activity")
            }

            override fun onActivityResumed(activity: Activity?) {
                loge("ActivityResumed: $activity")
            }

            override fun onActivityStarted(activity: Activity?) {
                loge("ActivityStarted: $activity")
            }

            override fun onActivityDestroyed(activity: Activity?) {
                loge("ActivityDestroyed: $activity")
            }

            override fun onActivitySaveInstanceState(activity: Activity?, outState: Bundle?) {
                loge("ActivitySaveInstanceState: $activity")
            }

            override fun onActivityStopped(activity: Activity?) {
                loge("ActivityStopped: $activity")
            }

            override fun onActivityCreated(activity: Activity?, savedInstanceState: Bundle?) {
                loge("ActivityCreated: $activity")
            }
        })
    }

}
