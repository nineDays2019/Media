package com.juhezi.media

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import com.juhezi.ffmcli.core.FFmpegCli
import com.juhezi.orange.bridge.OrangeBridge
import com.juhezi.orange.media.experimental.PcmPlayer
import com.juhezi.orange.media.experimental.PcmRecorder
import com.juhezi.orange.media.experimental.WavCodec
import com.juhezi.orange.media.experimental.pcm_record_and_play
import kotlinx.android.synthetic.main.activity_audio_record.*
import me.juhezi.eternal.base.BaseActivity
import me.juhezi.eternal.builder.buildBackgroundHandler
import me.juhezi.eternal.extension.*
import me.juhezi.eternal.global.logd
import me.juhezi.eternal.other.EShell
import me.juhezi.eternal.other.EShellCallback
import me.juhezi.eternal.other.Shell
import me.juhezi.eternal.other.ShellResult
import me.juhezi.eternal.router.OriginalPicker
import me.juhezi.eternal.util.UriUtils

/**
 * 使用 Audio Record 和 Audio Track API 完成 PCM 数据的采集和播放，并实现读写音频 wav 文件
 * 0. 播放 PCM，使用 AudioTrack [x]
 * 0.5 FFmpeg 将 普通音频文件解码为 PCM 文件 [x]
 * 0.6 Shell 工具还需要完善 [x]
 * 0.65 完善 FFmpegCli [x]
 * 0.7 移植到 Android 上 [x]
 * 1. 录制 PCM [x] 使用 AudioRecorder
 * 2. 变录边播 [x] （有回音环绕效果）
 * 4. 解码 wav 数据 [x]
 * 5. 编码 wav 数据 [x]
 * 6. 软解 [x]
 * 7. pcm 重采样 [ ]
 *
 * AudioTrack 可以播放音频，但是只能播放 PCM 数据流
 *
 */
class AudioActivity : BaseActivity() {

    private val PICK_PCM_REQUEST_CODE = 0x123
    private val PICK_AUDIO_REQUEST_CODE = 0x124
    private val PICK_PCM_REQUEST_CODE_FOR_ENCODE = 0x125
    private val PICK_PCM_REQUEST_CODE_FOR_DECODE = 0x126
    private val PICK_PCM_REQUEST_CODE_FOR_FFMPEG_DECODE = 0x127


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audio_record)
        record.setOnClickListener {
            checkPermissionWith(Manifest.permission.RECORD_AUDIO) {
                buildBackgroundHandler("PCM_RECORDER").first.post {
                    PcmRecorder().record("${Environment.getExternalStorageDirectory().path}/pcm/${System.currentTimeMillis()}.pcm")
                    logd("录制完成")
                }
            }
        }
        pick_pcm.setOnClickListener {
            startActivityForResult(
                OriginalPicker.getIntent(OriginalPicker.Type.ANY),
                PICK_PCM_REQUEST_CODE
            )
        }
        turn_to_pcm.setOnClickListener {
            startActivityForResult(
                OriginalPicker.getIntent(OriginalPicker.Type.AUDIO),
                PICK_AUDIO_REQUEST_CODE
            )
        }
        pcm_record_and_play_.setOnClickListener {
            checkPermissionWith(Manifest.permission.RECORD_AUDIO) {
                buildBackgroundHandler("PCM_RECORD_AND_PLAY").first.post {
                    pcm_record_and_play()
                    logd("Record And Play Done")
                }
            }
        }
        encode_pcm_to_wav.setOnClickListener {
            startActivityForResult(
                OriginalPicker.getIntent(OriginalPicker.Type.ANY),
                PICK_PCM_REQUEST_CODE_FOR_ENCODE
            )
        }
        decode_pcm_from_wav.setOnClickListener {
            startActivityForResult(
                OriginalPicker.getIntent(OriginalPicker.Type.AUDIO),
                PICK_PCM_REQUEST_CODE_FOR_DECODE
            )
        }
        ffmpeg_decode_pcm_from_audio.setOnClickListener {
            startActivityForResult(
                OriginalPicker.getIntent(OriginalPicker.Type.AUDIO),
                PICK_PCM_REQUEST_CODE_FOR_FFMPEG_DECODE
            )
        }
        test_0.setOnClickListener {
            d(
                Shell().run(
                    java.lang.String("/data/user/0/com.juhezi.media.Linux/files/ffmpeg -i /storage/emulated/0/test.mp3 -f s16le -ar 8000 -ac 2 -acodec pcm_s16le /storage/emulated/0/pcm/test.pcm"),
                    10000
                ).getResult()
            )
        }
        test_1.setOnClickListener {
            val command =
                "/data/user/0/com.juhezi.media.Linux/files/ffmpeg -i /storage/emulated/0/test.mp3 -f s16le -ar 8000 -ac 2 -acodec pcm_s16le /storage/emulated/0/pcm/test_${System.currentTimeMillis()}.pcm"
            EShell()
                .setCallback(object : EShellCallback {
                    override fun onOutputUpdate(order: Int, shellResult: ShellResult) {
                        logd("${shellResult.type} [$order]:${shellResult.message}")
                    }

                    override fun onFinish(
                        shellResult: ShellResult?,
                        returnCode: Int,
                        costTimeMs: Long
                    ) {
                        d("Finish, result is $shellResult, returnCode is $returnCode, costTimeUs is $costTimeMs")
                    }

                })
                .runAsync(command)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                PICK_PCM_REQUEST_CODE -> {
                    val uri = data?.data
                    val pcmPath = UriUtils.getPathFromUri(this, uri)
                    playPCM(pcmPath)
                }
                PICK_AUDIO_REQUEST_CODE -> {
                    val uri = data?.data
                    val audioPath = UriUtils.getPathFromUri(this, uri)
                    turn2Pcm(audioPath)
                }
                PICK_PCM_REQUEST_CODE_FOR_ENCODE -> {
                    val uri = data?.data
                    val pcmPath = UriUtils.getPathFromUri(this, uri)
                    encodeWav(pcmPath)
                }
                PICK_PCM_REQUEST_CODE_FOR_DECODE -> {
                    val uri = data?.data
                    val wavPath = UriUtils.getPathFromUri(this, uri)
                    decodeWav(wavPath)
                }
                PICK_PCM_REQUEST_CODE_FOR_FFMPEG_DECODE -> {
                    val uri = data?.data
                    val audioPath = UriUtils.getPathFromUri(this, uri)
                    val pcmPath =
                        "${Environment.getExternalStorageDirectory().path}/pcm/${System.currentTimeMillis()}.pcm"
                    buildBackgroundHandler("FFMPEG_DECODE_AUDIO").first.post {
                        val returnCode = OrangeBridge.decode2Pcm(audioPath, pcmPath)
                        logd("ffmpeg decode end, code is $returnCode")
                    }
                }
            }
        }
    }

    private fun encodeWav(pcmPath: String) {
        buildBackgroundHandler("ENCODE_WAV").first.post {
            val outputPath =
                "${Environment.getExternalStorageDirectory().path}/wav/${System.currentTimeMillis()}.wav"
            WavCodec.encode(pcmPath, outputPath)
        }
    }

    private fun decodeWav(wavPath: String) {
        buildBackgroundHandler("ENCODE_WAV").first.post {
            val outputPath =
                "${Environment.getExternalStorageDirectory().path}/pcm/${System.currentTimeMillis()}.pcm"
            d("PCM 信息： ${WavCodec.decode(wavPath, outputPath)}")
        }
    }


    private fun playPCM(pcmPath: String) {
        i("PCM PATH is $pcmPath")
        buildBackgroundHandler("PCM_PLAYER").first.post {
            PcmPlayer().play(pcmPath)
        }
    }

    private fun turn2Pcm(audioPath: String) =
        checkPermissionWith(Manifest.permission.WRITE_EXTERNAL_STORAGE) {
            i("Audio PATH is $audioPath")
            val outputPath =
                "${Environment.getExternalStorageDirectory().path}/pcm/${System.currentTimeMillis()}.pcm"
            outputPath.ensureExist()
            FFmpegCli.execute(
                this,
                "ffmpeg -i $audioPath -y -f s16le -ar 8000 -ac 2 -acodec pcm_s16le $outputPath",
                shellCallback = object : EShell.Callback() {

                    override fun onFinish(
                        shellResult: ShellResult?,
                        returnCode: Int,
                        costTimeMs: Long
                    ) {
                        logd("Finish, result is $shellResult, returnCode is $returnCode, costTimeUs is $costTimeMs")
                        runOnUiThread { showToast("Done， Code is $returnCode") }
                    }

                })
        }

}