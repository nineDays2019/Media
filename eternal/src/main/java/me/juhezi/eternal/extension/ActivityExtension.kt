package me.juhezi.eternal.extension

import android.app.Activity
import android.support.v4.app.Fragment
import android.widget.Toast
import me.juhezi.eternal.service.PermissionService

fun Activity.checkPermissionWith(permission: String, action: (() -> Unit)? = null) =
    if (PermissionService.requestPermission(permission, this)) {
        action?.invoke()
        true
    } else false

fun Fragment.checkPermissionWith(permission: String, action: (() -> Unit)? = null) =
    if (PermissionService.requestPermission(permission, activity!!)) {
        action?.invoke()
        true
    } else false

fun Fragment.showToast(message: String) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}