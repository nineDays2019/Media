package com.juhezi.media

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.JsResult
import android.webkit.WebChromeClient
import android.webkit.WebView
import kotlinx.android.synthetic.main.activity_web.*
import me.juhezi.eternal.base.BaseActivity
import me.juhezi.eternal.enum.ToolbarStyle
import me.juhezi.eternal.extension.showToast

class WebActivity : BaseActivity() {

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web)
        showContent()
        mToolbar?.leftStyle = ToolbarStyle.ICON_AND_TEXT
        mToolbar?.rightStyle = ToolbarStyle.ICON
        mToolbar?.onRightIconClickListener = {
            vg_message.visibility = if (vg_message.visibility == View.VISIBLE) {
                View.GONE
            } else {
                View.VISIBLE
            }
        }
        mToolbar?.onLeftGroupIconClickListener = {
            onBackPressed()
        }
        wv_show.loadUrl("file:///android_asset/index.html")
        wv_show.addJavascriptInterface(this, "android")  // 添加 JS 监听
        wv_show.webChromeClient = client
        val settings = wv_show.settings
        settings.javaScriptEnabled = true
    }

    private val client = object : WebChromeClient() {

        override fun onJsAlert(view: WebView?, url: String?, message: String?, result: JsResult?): Boolean {
            val builder = AlertDialog.Builder(view?.context)
            builder.setMessage(message)
                .setPositiveButton("确定", null)
                .setCancelable(true)
                .create()
                .show()
            // Note
            //必须要这一句代码:result.confirm()表示:
            //处理结果为确定状态同时唤醒WebCore线程
            //否则不能继续点击按钮
            result?.confirm()
            return true
        }

        override fun onReceivedTitle(view: WebView?, title: String?) {
            super.onReceivedTitle(view, title)
            mToolbar?.configLeftGroup(title ?: "🐴😄😌⚽️")
        }

    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (wv_show.canGoBack() && keyCode == KeyEvent.KEYCODE_BACK) {//点击返回按钮的时候判断有没有上一页
            wv_show.goBack() // goBack()表示返回webView的上一页面
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    @JavascriptInterface
    fun sendMessage(message: String) {
        showToast("$message🏆")
    }

    override fun onDestroy() {
        super.onDestroy()
        wv_show.destroy()
    }

}