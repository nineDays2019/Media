package com.juhezi.media

import android.os.Bundle
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import me.juhezi.eternal.base.BaseActivity
import me.juhezi.eternal.base.BaseFragment
import me.juhezi.mediademo.grafika.VideoPlayer

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
        val viewPager: ViewPager = findViewById(R.id.view_pager)
        viewPager.adapter = adapter
        val tabLayout: TabLayout = findViewById(R.id.tab_layout)
        tabLayout.setupWithViewPager(viewPager)
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
