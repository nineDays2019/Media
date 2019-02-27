package me.juhezi.eternal.base

import android.view.View

/**
 * 控制基本 View 的显示与消失
 * Created by shixi_yunrui on 2018/1/16.
 */
class BaseViewController() {

    companion object {
        const val STATUS_CONTENT = "content"     //内容
        const val STATUS_LOADING = "loading"     //加载 View
        const val STATUS_EMPTY = "empty"       //空界面
        const val STATUS_ERROR = "error"       //错误界面

        const val STATUS_NONE = "none"

        const val STATUS_INIT = STATUS_CONTENT    //初始界面
        const val ANIM_DURATION = 500L
    }

    var currentStatus = STATUS_INIT

    private var mViewMap = HashMap<String, View>()  //管理所有的 View

    fun load(status: String, view: View?): Boolean {
        if (STATUS_NONE == status) return false
        if (mViewMap.containsKey(status)) return false
        if (view == null) return false
        mViewMap[status] = view
        return true
    }

    fun unload(status: String): Boolean {
        if (!mViewMap.containsKey(status)) return false
        mViewMap.remove(status)
        return true
    }

    /**
     * 显示 status 对应的 View
     */
    fun show(status: String) {
        val preStatus = currentStatus
        currentStatus = status
        val preView = mViewMap[preStatus]
        val currentView = mViewMap[status]
        hideView(preView)
        showView(currentView)
    }

    /**
     * 隐藏 status 对应的 View
     */
    fun hide(status: String) {
        hideView(mViewMap[status])
    }

    fun hideAll() {
        mViewMap.forEach {
            it.value.visibility = View.GONE
        }
        currentStatus = STATUS_NONE
    }

    private fun showView(view: View?) {
        if (view == null) return
        if (view.visibility == View.VISIBLE) return
        view.visibility = View.VISIBLE
    }

    private fun hideView(view: View?) {
        if (view == null) return
        if (view.visibility != View.VISIBLE) return
        view.visibility = View.GONE
    }

    fun size() = mViewMap.size

    fun init() {
        mViewMap.forEach {
            it.value.visibility = if (it.key == currentStatus) View.VISIBLE else View.GONE
        }
    }

}