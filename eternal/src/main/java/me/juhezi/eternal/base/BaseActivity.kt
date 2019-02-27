package me.juhezi.eternal.base

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.annotation.LayoutRes
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import me.juhezi.eternal.R
import me.juhezi.eternal.widget.view.EternalToolbar

/**
 * 基础 Activity
 *
 * 共有 5 类页面
 * 1. content   // 内容界面
 * 2. empty     // 空界面
 * 3. loading   // 加载界面
 * 4. error     // 错误界面
 *
 */
open class BaseActivity : AppCompatActivity() {

    protected var mEmptyView: View? = null
    protected var mLoadingView: View? = null
    protected var mErrorView: View? = null
    protected var mContentView: View? = null

    protected val mLayoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT)

    @LayoutRes
    private var mEmptyViewResId = R.layout.view_empty_default
    @LayoutRes
    private var mErrorViewResId = R.layout.view_error_default
    @LayoutRes
    private var mLoadingViewResId = R.layout.view_loading_default
    //    @LayoutRes private var mContentViewResId = R.layout.activity_default
    @LayoutRes
    private var mRootViewResId = R.layout.activity_root


    private var mRootView: View? = null
    protected var mContainer: FrameLayout? = null
    protected var mToolbar: EternalToolbar? = null
    protected var mInflater: LayoutInflater? = null

    protected val mBaseViewController: BaseViewController = BaseViewController()

    var toolBarVisibility = true
        set(value) {
            field = value
            mToolbar?.visibility = if (value) View.VISIBLE else View.GONE
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mInflater = LayoutInflater.from(this)
        initRootView()
        loadBaseViews()
    }

    private fun initRootView() {
        super.setContentView(mRootViewResId)
        mRootView = findViewById(R.id.vg_base_activity_root)
        mContainer = findViewById(R.id.vg_base_activity_container)
        mToolbar = findViewById(R.id.tb_base_activity)
        mToolbar?.visibility = if (toolBarVisibility) View.VISIBLE else View.GONE
        //这里应该将基础 View 中添加到界面中
    }

    override fun onStart() {
        super.onStart()
        mBaseViewController.init()
    }

    private fun loadBaseViews() {
        onLoadLoadingView()
        onLoadErrorView()
        onLoadEmptyView()
    }

    override fun setContentView(layoutResID: Int) {
        mContentView = mInflater?.inflate(layoutResID, null)
        mContainer?.addView(mContentView, mLayoutParams)
        mBaseViewController.load(BaseViewController.STATUS_CONTENT, mContentView)
    }

    override fun setContentView(view: View?) {
        if (view == null) return
        mContainer?.addView(view, mLayoutParams)
        mBaseViewController.load(BaseViewController.STATUS_CONTENT, mContentView)
    }

    open fun onLoadLoadingView() {
        mLoadingView = mInflater?.inflate(mLoadingViewResId, null)
        mContainer?.addView(mLoadingView, mLayoutParams)
        mBaseViewController.load(BaseViewController.STATUS_LOADING, mLoadingView)
    }

    open fun onLoadErrorView() {
        mErrorView = mInflater?.inflate(mErrorViewResId, null)
        mContainer?.addView(mErrorView, mLayoutParams)
        mBaseViewController.load(BaseViewController.STATUS_ERROR, mErrorView)
    }

    open fun onLoadEmptyView() {
        mEmptyView = mInflater?.inflate(mEmptyViewResId, null)
        mContainer?.addView(mEmptyView, mLayoutParams)
        mBaseViewController.load(BaseViewController.STATUS_EMPTY, mEmptyView)
    }

    protected fun showContent() {
        show(BaseViewController.STATUS_CONTENT)
    }

    protected fun showEmpty() {
        show(BaseViewController.STATUS_EMPTY)
    }

    protected fun showError() {
        show(BaseViewController.STATUS_ERROR)
    }

    protected fun showLoading() {
        show(BaseViewController.STATUS_LOADING)
    }

    protected fun show(status: String) {
        mBaseViewController.show(status)
    }

    protected fun turnTo(clazz: Class<out Activity>) {
        startActivity(Intent(this, clazz))
    }

    @SuppressLint("ObsoleteSdkInt")
    protected fun hideBottomUIMenu() {
        //隐藏虚拟按键，并且全屏
        if (Build.VERSION.SDK_INT in 12..18) { // lower api
            val v = this.window.decorView
            v.systemUiVisibility = View.GONE
        } else if (Build.VERSION.SDK_INT >= 19) {
            //for new api versions.
            val decorView = window.decorView;
            val uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                    View.SYSTEM_UI_FLAG_FULLSCREEN
            decorView.systemUiVisibility = uiOptions
        }
    }

}