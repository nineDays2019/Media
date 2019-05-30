package me.juhezi.eternal.service

import android.content.Context
import android.Manifest.permission
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.app.Activity
import android.support.v4.app.ActivityCompat
import android.content.pm.PackageManager
import android.support.v4.content.ContextCompat
import me.juhezi.eternal.base.BaseApplication


object PermissionService {

    fun requestPermission(permission: String, activity: Activity) : Boolean{
        val hasWriteStoragePermission =
            ContextCompat.checkSelfPermission(
                activity,
                permission
            )
        if (hasWriteStoragePermission == PackageManager.PERMISSION_GRANTED) {
            return true
        } else {
            //没有权限，向用户请求权限
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(permission), 0x123
            )
            return false
        }
    }

}