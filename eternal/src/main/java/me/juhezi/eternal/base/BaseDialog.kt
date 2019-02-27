package me.juhezi.eternal.base

import android.app.Dialog
import android.content.Context

open class BaseDialog(context: Context?, themeResId: Int = 0) : Dialog(context, themeResId) {

    var canBack: Boolean = true   // 是否可以返回

    override fun onBackPressed() {
        if (canBack) {
            super.onBackPressed()
        }
    }

    fun config(action: (BaseDialog.() -> Unit)?) {
        action?.invoke(this)
    }

}