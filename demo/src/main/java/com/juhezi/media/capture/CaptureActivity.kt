package com.juhezi.media.capture

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Size
import android.view.View
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.juhezi.media.R
import kotlinx.android.synthetic.main.activity_capture.*
import me.juhezi.eternal.base.BaseActivity
import me.juhezi.eternal.capture.CaptureController
import me.juhezi.eternal.capture.EternalFormat
import me.juhezi.eternal.capture.getEternalFormatById

class CaptureActivity : BaseActivity() {

    private lateinit var captureController: CaptureController

    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setContentView(R.layout.activity_capture)
        hideBottomUIMenu()
        showContent()
        toolBarVisibility = false
        captureController = CaptureController(
            this, tv_capture_00,
            useImageReaderForPreview = true
        )
        val sizeArrayAdapter = ArrayAdapter<Size>(
            this,
            android.R.layout.simple_list_item_activated_1,
            captureController.getPreviewSizes().toMutableList()
        )
        val formatArrayAdapter = ArrayAdapter<EternalFormat>(
            this,
            android.R.layout.simple_list_item_activated_1,
            captureController.getPreviewFormats().toMutableList().map {
                getEternalFormatById(it)
            }
        )

        fun notifyConfigChanged() {
            sizeArrayAdapter.clear()
            sizeArrayAdapter.addAll(
                captureController
                    .getPreviewSizes()
                    .toMutableList()
            )
            formatArrayAdapter.clear()
            formatArrayAdapter.addAll(captureController
                .getPreviewFormats()
                .toMutableList().map {
                    getEternalFormatById(it)
                })
        }

        btn_switch.setOnClickListener {
            captureController.switchCamera()
            notifyConfigChanged()
        }
        var flag = 0
        preview.setOnClickListener {
            if (flag % 2 == 0) {
                captureController.stopPreview()
            } else {
                captureController.startPreview()
            }
            flag++
        }
        size_spinner.adapter = sizeArrayAdapter
        size_spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                captureController.switchPreviewSize(
                    captureController.getPreviewSizes()[position]
                )
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

        format_spinner.adapter = formatArrayAdapter
        format_spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                captureController.switchPreviewFormat(
                    captureController.getPreviewFormats()[position]
                )
                // 切换 format 之后，Size 也会相应的改变
                sizeArrayAdapter.clear()
                sizeArrayAdapter.addAll(
                    captureController
                        .getPreviewSizes()
                        .toMutableList()
                )
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

        fab_show_panel.setOnClickListener {
            vg_panel.visibility = View.VISIBLE
            fab_show_panel.hide()
        }

        vg_panel.setOnClickListener {
            it.visibility = View.GONE
            fab_show_panel.show()
        }

    }

    override fun onResume() {
        super.onResume()
        captureController.onResume()
    }

    override fun onPause() {
        super.onPause()
        captureController.onPause()
    }

}