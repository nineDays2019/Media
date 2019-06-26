package com.juhezi.media

import android.os.Bundle
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import kotlinx.android.synthetic.main.activity_main.*
import me.juhezi.eternal.base.BaseActivity
import me.juhezi.eternal.base.BaseFragment

class MainActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        toolBarVisibility = false
        val adapter = Adapter(
            supportFragmentManager,
            listOf("Panel", "Web"),
            listOf(PanelFragment(), WebFragment())
        )
        view_pager.adapter = adapter
        tab_layout.setupWithViewPager(view_pager)
    }
}

class Adapter(
    fm: FragmentManager,
    var tabNames: List<String>,
    var fragments: List<BaseFragment>
) :
    FragmentStatePagerAdapter(fm) {
    override fun getItem(position: Int) = fragments[position]

    override fun getCount() = fragments.size

    override fun getPageTitle(position: Int) = tabNames[position]

}
