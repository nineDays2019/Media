package me.juhezi.eternal.base

import android.os.Bundle
import android.support.annotation.IdRes
import android.support.annotation.LayoutRes
import android.text.TextUtils
import me.juhezi.eternal.R
import java.util.*
import kotlin.collections.HashMap

/**
 * 盛放多个 Fragment 的 Activity 基类
 * 提供一个默认的布局
 */
abstract class BaseMultiFragmentActivity : BaseActivity() {

    // Fragment 的返回栈
    private val mFragmentStack = Stack<BaseFragment>()
    private val mFragmentMap: MutableMap<String, BaseFragment> = HashMap()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(getActivityLayoutRes())
    }

    @LayoutRes
    open fun getActivityLayoutRes(): Int = R.layout.activity_single_fragment

    @IdRes
    open fun getFragmentContainerId() = R.id.vg_activity_single_fragment

    private fun getShowingFragment(): BaseFragment? =
            if (mFragmentStack.empty()) {
                null
            } else {
                mFragmentStack.peek()
            }

    /**
     * 添加一个 Fragment
     */
    protected fun add(key: String?, fragment: BaseFragment): String {
        var internalKey = key
        if (TextUtils.isEmpty(key)) {
            internalKey = System.currentTimeMillis().toString()
        }
        mFragmentMap[internalKey!!] = fragment
        return internalKey
    }

    /**
     * 删除一个 Fragment
     */
    protected fun remove(key: String) {
        mFragmentMap.remove(key)
    }

    protected fun showFragment(key: String) {
        val fragment = mFragmentMap[key] ?: return
        showFragment(fragment)
    }

    protected fun showFragment(fragment: BaseFragment) {
        mFragmentStack.push(fragment)
        internalShowFragment(fragment)
    }

    private fun internalShowFragment(fragment: BaseFragment) {
        supportFragmentManager
                .beginTransaction()
                .replace(getFragmentContainerId(), fragment)
                .commit()
    }

    protected fun hideFragment(): BaseFragment {
        val currentFragment = mFragmentStack.pop()
        if (mFragmentStack.empty()) {
            supportFragmentManager
                    .beginTransaction()
                    .remove(currentFragment)
                    .commit()
        } else {
            val preFragment = mFragmentStack.peek()
            internalShowFragment(preFragment)
        }
        return currentFragment
    }

    /**
     * Activity 的返回事件
     * @return true: 对返回事件进行拦截 false：对返回事件不进行拦截
     * 默认返回 false，不进行拦截
     */
    open fun handleBackPressed(): Boolean {
        // 默认逻辑
        // 如果当前 fragment 栈里只有一个 fragment 的时候，不进行拦截，执行 Activity 的返回操作
        return if (mFragmentStack.size > 1) {
            hideFragment()
            true
        } else {
            false
        }
    }

    override fun onBackPressed() {
        val fragment = getShowingFragment()
        if (fragment != null && fragment.handleBackPressed()) {     // 前台有 Fragment，并且前台 Fragment 拦截了返回事件
            return
        }
        if (handleBackPressed()) {
            return
        }
        super.onBackPressed()
    }

}