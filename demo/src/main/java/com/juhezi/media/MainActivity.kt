package com.juhezi.media

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler
import com.github.hiteshsondhi88.libffmpeg.FFmpeg
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException
import com.juhezi.orange.bridge.OrangeBridge

class MainActivity : AppCompatActivity() {

    val TAG = "HELLO"
    lateinit var ffmpeg: FFmpeg

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        OrangeBridge.test("http://f.us.sinaimg.cn/0008BFPKlx07rQB0dlrO01041200prFo0E010.mp4?label=mp4_hd&template=720x1280.24.0&Expires=1551199278&ssig=5Qm8eOR3hC&KID=unistore,video")
        ffmpeg = FFmpeg.getInstance(this)
        try {
            ffmpeg.loadBinary(object : LoadBinaryResponseHandler() {
                override fun onFinish() {
                    super.onFinish()
                    Log.i(TAG, "loadBinary onFinish")
                }

                override fun onSuccess() {
                    super.onSuccess()
                    Log.i(TAG, "loadBinary onSuccess")
                    test()
                }

                override fun onFailure() {
                    super.onFailure()
                    Log.i(TAG, "loadBinary onFailure")
                }

                override fun onStart() {
                    super.onStart()
                    Log.i(TAG, "loadBinary onStart")
                }
            })
        } catch (e: FFmpegNotSupportedException) {
            e.printStackTrace()
        }
    }

    fun test() {
        try {
            ffmpeg.execute(arrayOf("ffmpeg -version"), object : ExecuteBinaryResponseHandler() {
                override fun onFinish() {
                    super.onFinish()
                    Log.i(TAG, "execute onFinish")
                }

                override fun onSuccess(message: String?) {
                    super.onSuccess(message)
                    Log.i(TAG, "execute onSuccess $message")
                }

                override fun onFailure(message: String?) {
                    super.onFailure(message)
                    Log.i(TAG, "execute onFailure $message")
                }

                override fun onProgress(message: String?) {
                    super.onProgress(message)
                    Log.i(TAG, "execute onProgress $message")
                }

                override fun onStart() {
                    super.onStart()
                    Log.i(TAG, "execute onStart")
                }
            })
        } catch (e: FFmpegCommandAlreadyRunningException) {
            e.printStackTrace()
        }
    }

}
