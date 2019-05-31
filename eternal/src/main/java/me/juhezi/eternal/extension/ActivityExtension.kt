package me.juhezi.eternal.extension

import android.app.Activity
import me.juhezi.eternal.service.PermissionService

fun Activity.checkPermissionWith(permission: String, action: (() -> Unit)? = null) =
    if (PermissionService.requestPermission(permission, this)) {
        action?.invoke()
        true
    } else false