package me.juhezi.eternal.base

import android.app.Application
import me.juhezi.eternal.db.DB
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
        DB.init()
    }

}
