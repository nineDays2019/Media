package com.juhezi.media

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import com.juhezi.orange.media.gpuimage.EternalGPUImage
import com.juhezi.orange.media.gpuimage.buildSpecialFragmentShader
import com.juhezi.orange.media.gpuimage.filter.FragmentShaderFilter
import kotlinx.android.synthetic.main.activity_gpu_image.*
import me.juhezi.eternal.base.BaseActivity
import me.juhezi.eternal.extension.e
import me.juhezi.eternal.extension.isEmpty
import me.juhezi.eternal.extension.readContentFromRaw
import me.juhezi.eternal.router.OriginalPicker
import me.juhezi.eternal.service.FileService
import me.juhezi.eternal.util.UriUtils

class GPUImageActivity : BaseActivity() {

    private var gpuImage: EternalGPUImage? = null
    private var schemeFragmentPath: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setContentView(R.layout.activity_gpu_image)
        hideBottomUIMenu()
        showContent()
        val uri = intent.data
        // Scheme 的逻辑，从 Scheme 中带入 FragmentShader 的路径
        if (uri != null) {
            schemeFragmentPath = uri.getQueryParameter(GPU_FRAGMENT)
        }
        toolBarVisibility = false
        gpuImage = EternalGPUImage(this)
        gpuImage!!.textureView = tv_demo_show
        fab_demo_list.setOnClickListener {
            startActivityForResult(OriginalPicker.getIntent(OriginalPicker.Type.ANY), 0x123)
        }
        if (isEmpty(schemeFragmentPath)) {
            setUpFilter(readContentFromRaw(R.raw.color3))
        } else {
            updateFragmentShader()
        }
    }

    private fun setUpFilter(fragment: String) {
        if (isEmpty(fragment)) {
            e("The Fragment Shader is empty!!")
            return
        }
        val filter = FragmentShaderFilter(
            fragmentShader = fragment
        )
        gpuImage!!.setFilter(filter)
        gpuImage!!.continuous = true
    }

    // 使用如此方式创建片段着色器
    private fun buildRepeatFragmentShader() = buildSpecialFragmentShader {
        function(
            """float circle(in vec2 _st, in float _radius) {
                        vec2 l = _st - vec2(0.5);   // 到圆心的距离
                        return 1.0 - smoothstep(_radius - (_radius * 0.01),
                            _radius + (_radius * 0.01),
                            dot(l, l) * 4.0);
                    }
        """.trimIndent()
        )
        main(
            """vec2 st = gl_FragCoord.xy / u_resolution;
                vec3 color = vec3(0.0);

                st *= 4.0;  // 坐标系扩大为 3 倍
                st = fract(st); // 取小数部分

                color = vec3(st, 0.0);
//                color = vec3(circle(st, 0.5));
                gl_FragColor = vec4(color,1.0);
            """.trimIndent()
        )
    }

    private fun updateFragmentShader() {
        showLoading()
        Thread {
            val fragmentShader = FileService.getInstance().read(schemeFragmentPath!!)
            runOnUiThread {
                setUpFilter(fragmentShader)
                showContent()
            }
        }.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        gpuImage?.destroy()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            val uri = data?.data
            if (requestCode == 0x123) {
                schemeFragmentPath = UriUtils.getPathFromUri(this, uri)
                updateFragmentShader()
            }
        }
    }

}