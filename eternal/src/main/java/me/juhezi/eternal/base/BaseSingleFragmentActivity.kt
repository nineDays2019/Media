package me.juhezi.eternal.base

import android.os.Bundle
import android.support.annotation.IdRes
import android.support.annotation.LayoutRes
import android.support.v4.app.Fragment
import me.juhezi.eternal.R

/**
 * 此 Activity 可以看作是 Fragment 的 容器
 * Created by Juhezi[juhezix@163.com] on 2018/7/23.
 */
abstract class BaseSingleFragmentActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(getActivityLayoutRes())
        toolBarVisibility = false   // 显示 Toolbar
        var fragment = supportFragmentManager.findFragmentById(getFragmentContainerId())
        if (fragment == null) {
            fragment = getFragment()
            supportFragmentManager.beginTransaction()
                    .add(getFragmentContainerId(), fragment)
                    .commit()
        }
    }

    abstract fun getFragment(): Fragment

    @LayoutRes
    open fun getActivityLayoutRes() : Int= R.layout.activity_single_fragment

    @IdRes
    open fun getFragmentContainerId() = R.id.vg_activity_single_fragment

}