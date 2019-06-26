package com.juhezi.media

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_web.view.*
import me.juhezi.eternal.base.BaseFragment

class WebFragment : BaseFragment() {

    lateinit var rootView: View


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.fragment_web, container, false)
        rootView.web.loadUrl("file:///android_asset/navigation.html")
        return rootView
    }

}