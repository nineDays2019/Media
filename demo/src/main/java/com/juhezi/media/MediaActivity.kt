package com.juhezi.media

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.juhezi.orange.media.demuxer.EternalDemuxer
import com.juhezi.orange.media.demuxer.HardDemuxer
import kotlinx.android.synthetic.main.activity_media.*
import me.juhezi.eternal.base.BaseActivity
import me.juhezi.eternal.builder.buildBackgroundHandler
import me.juhezi.eternal.extension.e
import me.juhezi.eternal.extension.i
import me.juhezi.eternal.extension.isEmpty
import me.juhezi.eternal.router.OriginalPicker
import me.juhezi.eternal.util.UriUtils

class MediaActivity : BaseActivity() {

    var sourcePath = ""
    val pair = buildBackgroundHandler("Media")

    var demuxer: EternalDemuxer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_media)
        if (isEmpty(sourcePath)) {
            val intent = OriginalPicker.getIntent(OriginalPicker.Type.VIDEO_AND_AUDIO)
            startActivityForResult(intent, 0x123)
        }

        media_demuxer.setOnClickListener {
            demuxer = HardDemuxer(sourcePath, pair.first)
            demuxer!!.saveTrack(outputPath = "storage/emulated/0/demuxer",
                callBack = object : EternalDemuxer.SaveTrackCallBack {
                    override fun onSuccess(outputPath: String) {
                        i("成功！！\n$outputPath")
                    }

                    override fun onFail(message: String) {
                        e("失败！！\n$message")
                    }

                })
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            val uri = data?.data
            if (requestCode == 0x123) {
                if (uri != null) {
                    sourcePath = UriUtils.getPathFromUri(this, uri)
                    i("Source Path is $sourcePath")
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        pair.second.quitSafely()
        try {
            pair.second.join()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
        demuxer?.release()
    }

}