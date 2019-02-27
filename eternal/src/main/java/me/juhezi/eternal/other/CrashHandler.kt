package me.juhezi.eternal.other

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.support.v4.app.ActivityCompat
import me.juhezi.eternal.extension.e
import me.juhezi.eternal.global.SDCARD_DIR
import me.juhezi.eternal.global.formatTime
import me.juhezi.eternal.global.logi
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.PrintWriter
import java.util.concurrent.Executors

class CrashHandler(val context: Context) : Thread.UncaughtExceptionHandler {

    companion object {
        var PATH = "${Environment.getExternalStorageDirectory().path}$SDCARD_DIR"
        const val FILE_NAME = "crash"
        const val FILE_NAME_SUFFIX = ".log"
    }

    var defaultExceptionHandler =
            Thread.getDefaultUncaughtExceptionHandler()

    init {
        Thread.setDefaultUncaughtExceptionHandler(this)
    }

    override fun uncaughtException(t: Thread?, e: Throwable?) {
        if (PackageManager.PERMISSION_GRANTED == ActivityCompat
                        .checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            handleException(e)
        }
        defaultExceptionHandler.uncaughtException(t, e)
    }

    private fun handleException(exception: Throwable?) {
        try {
            Executors.newSingleThreadExecutor()
                    .submit {
                        dumpExceptionToSDCard(exception)
                    }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun dumpExceptionToSDCard(ex: Throwable?) {
        // 判断是否支持 SD 卡
        if (Environment.getExternalStorageState() != Environment.MEDIA_MOUNTED) {
            logi("sdcard not find ,skip dump exception")
        }
        val dir = File(PATH)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        val currentTime = System.currentTimeMillis()
        val timeStr = formatTime(currentTime)
        val file = File("$PATH${FILE_NAME}_$timeStr$FILE_NAME_SUFFIX")
        try {
            file.createNewFile()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        e("Write to ${file.path}")
        try {
            val pw = PrintWriter(BufferedWriter(FileWriter(file)))
            pw.println(timeStr)
            dumpPhoneInfo(pw)
            pw.println()
            ex?.printStackTrace(pw)
            pw.close()
        } catch (e: Exception) {
            logi("dump Exception: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun dumpPhoneInfo(pw: PrintWriter) {
        val pm = context.packageManager
        val pi = pm.getPackageInfo(context.packageName, PackageManager.GET_ACTIVITIES)

        pw.print("App Version:${pi.versionName}_${pi.versionCode}\n" +
                "OS Version:${Build.VERSION.RELEASE}_${Build.VERSION.SDK_INT}\n" +
                "Vendor:${Build.MANUFACTURER}\n" +
                "Model:${Build.MODEL}\n")
    }


}
