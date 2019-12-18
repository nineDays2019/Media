package me.juhezi.apple

import android.os.Bundle
import android.widget.TextView
import kotlinx.android.synthetic.*

class MainActivity : AppleBaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<TextView>(R.id.apple_text)
            .text = "奥利给"
    }
}
