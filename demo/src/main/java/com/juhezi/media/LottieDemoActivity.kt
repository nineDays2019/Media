package com.juhezi.media

import android.os.Build
import android.os.Bundle
import android.widget.SeekBar
import kotlinx.android.synthetic.main.activity_lottie_demo.*
import me.juhezi.eternal.base.BaseActivity
import me.juhezi.eternal.builder.buildTypeface
import me.juhezi.eternal.enum.ToolbarStyle
import me.juhezi.eternal.global.荷包鼓鼓

class LottieDemoActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lottie_demo)
        initToolbar()

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

    private fun initToolbar() {
        mToolbar?.leftStyle = ToolbarStyle.ICON_AND_TEXT
        mToolbar?.onLeftGroupIconClickListener = {
            onBackPressed()
        }
        mToolbar?.configLeftGroup(textClosure = {
            this?.typeface = buildTypeface {
                assetManager = assets
                path = 荷包鼓鼓
            }
        })
        mToolbar?.configLeftGroup("动画")
    }

}