package me.juhezi.eternal.activity

import android.os.Bundle
import me.juhezi.eternal.base.BaseActivity

class DefaultActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showEmpty()
    }

}