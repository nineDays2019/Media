package me.juhezi.eternal.router

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import me.juhezi.eternal.builder.PERMISSION
import me.juhezi.eternal.extension.checkPermissionWith
import me.juhezi.eternal.extension.e
import me.juhezi.eternal.extension.i
import me.juhezi.eternal.extension.isEmpty


/**
 * Scheme 跳转相关
 */
object SchemeUtils {

    // 暂时只能简单的 startActivity
    fun openScheme(context: Context, scheme: String): Boolean {
        i("[Scheme]: $scheme")
        val uri = Uri.parse(scheme)
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = uri
        intent.addCategory(Intent.CATEGORY_DEFAULT)
        if (context !is Activity) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return if (queryActivityIntent(context, intent)) {
            if (!isEmpty(uri.getQueryParameter(PERMISSION))) { // 需要进行权限判断
                if (context is Activity) {
                    context.checkPermissionWith(uri.getQueryParameter(PERMISSION)!!) {
                        context.startActivity(intent)
                        true
                    }
                }
            } else {
                context.startActivity(intent)
                true
            }
            false
        } else {
            e("open Scheme: $scheme error")
            false
        }
    }

    private fun queryActivityIntent(context: Context, intent: Intent): Boolean {
        val packageManager = context.packageManager
        val queryIntentActivities = packageManager
            .queryIntentActivities(intent, 0)
        return queryIntentActivities != null && queryIntentActivities.size > 0
    }


}