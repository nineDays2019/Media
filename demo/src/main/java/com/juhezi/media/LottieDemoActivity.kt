package com.juhezi.media

import android.os.Build
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.style.SuperscriptSpan
import android.view.MotionEvent
import android.widget.SeekBar
import kotlinx.android.synthetic.main.activity_lottie_demo.*
import kotlinx.coroutines.*
import me.juhezi.eternal.base.BaseActivity
import me.juhezi.eternal.builder.buildTypeface
import me.juhezi.eternal.enum.ToolbarStyle
import me.juhezi.eternal.global.logi
import me.juhezi.eternal.global.荷包鼓鼓
import kotlin.math.sqrt
import kotlin.properties.Delegates

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
        val spannableString = SpannableString("RM123.456");
        spannableString.setSpan(
            TopAlignSuperscriptSpan(0.35f),
            0,
            2,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        tv_top_align.text = spannableString

    }

    private var job: Job? = null

    private fun startJob() =
        GlobalScope.launch(context = Dispatchers.Main) {
            while (true) {
                delay(1000)
                val lp = tv_auto_size.layoutParams
                lp.height = getAutoTextViewHeight()
                tv_auto_size.layoutParams = lp
            }
        }

    private fun getAutoTextViewHeight(): Int {
        val currentHeight = tv_auto_size.height
        if (currentHeight >= 1000) {
            return 100
        } else {
            return currentHeight + 100
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

    override fun onResume() {
        super.onResume()
        job = startJob()
    }

    override fun onPause() {
        super.onPause()
        job?.cancel()
    }

}

class TopAlignSuperscriptSpan(shiftPercentage: Float = 0f) : SuperscriptSpan() {

    private val fontScale = 2
    private var shiftPercentage = 0f

    init {
        if (shiftPercentage > 0f && shiftPercentage < 1) {
            this.shiftPercentage = shiftPercentage
        }
    }

    override fun updateDrawState(textPaint: TextPaint) {
        val ascent = textPaint.ascent() // Baseline 上方的距离，负值，Baseline 上方的值为正，下方的值为负
        textPaint.textSize = textPaint.textSize / fontScale
        val newAscent = textPaint.fontMetrics.ascent
        textPaint.baselineShift += ((ascent - ascent * shiftPercentage) - (newAscent - newAscent * shiftPercentage)).toInt()
    }

    override fun updateMeasureState(textPaint: TextPaint) {
        updateDrawState(textPaint)
    }

}