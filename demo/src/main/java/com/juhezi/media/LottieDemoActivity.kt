package com.juhezi.media

import android.os.Build
import android.os.Bundle
import android.widget.SeekBar
import kotlinx.android.synthetic.main.activity_lottie_demo.*
import me.juhezi.eternal.base.BaseActivity

class LottieDemoActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lottie_demo)
        seek_bar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                lottie_anim.pauseAnimation()
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                lottie_anim.resumeAnimation()
            }

            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    lottie_anim.progress = progress / 100.toFloat()
                }
            }

        })
        lottie_anim.addAnimatorUpdateListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                seek_bar.setProgress(((it.animatedValue as Float) * 100).toInt(), false)
            }
        }
    }

}