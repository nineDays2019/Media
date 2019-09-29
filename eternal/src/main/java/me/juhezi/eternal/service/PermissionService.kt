package me.juhezi.eternal.service

import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat


object PermissionService {

    fun requestPermission(permission: String, activity: Activity) : Boolean{
        val hasWriteStoragePermission =
            ContextCompat.checkSelfPermission(
                activity,
                permission
            )
        return if (hasWriteStoragePermission == PackageManager.PERMISSION_GRANTED) {
            true
        } else {
            //没有权限，向用户请求权限
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(permission), 0x123
            )
            false
        }
    }

}