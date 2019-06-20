package com.juhezi.media.capture

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

class CaptureActivity : BaseActivity() {

    private lateinit var captureController: CaptureController

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
            useImageReaderForPreview = false
        )
        val arrayAdapter = ArrayAdapter<Size>(
            this,
            android.R.layout.simple_list_item_activated_1,
            captureController.getPreviewSizes()
        )
        btn_switch.setOnClickListener {
            captureController.switchCamera()
            arrayAdapter.clear()
            arrayAdapter.addAll(captureController.getPreviewSizes())
        }
        var flag1 = 0
        preview.setOnClickListener {
            if (flag1 % 2 == 0) {
                captureController.stopPreview()
            } else {
                captureController.startPreview()
            }
            flag1++
        }
        spinner.adapter = arrayAdapter
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                captureController.switchPreviewSize(
                    captureController.getPreviewSizes()[position]
                )
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

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