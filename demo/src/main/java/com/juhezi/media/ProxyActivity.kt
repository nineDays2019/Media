package com.juhezi.media

import android.os.Bundle
import me.juhezi.eternal.base.BaseActivity

class ProxyActivity : BaseActivity() {

    private var className: String? = null //

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        className = intent.getStringExtra("class_name")
        // todo 
    }

}