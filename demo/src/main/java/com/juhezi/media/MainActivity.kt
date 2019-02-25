package com.juhezi.media

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.juhezi.orange.bridge.OrangeBridge

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        OrangeBridge.test("http://f.us.sinaimg.cn/0008BFPKlx07rQB0dlrO01041200prFo0E010.mp4?label=mp4_hd&template=720x1280.24.0&Expires=1551199278&ssig=5Qm8eOR3hC&KID=unistore,video")
    }
}
