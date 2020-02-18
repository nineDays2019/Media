package com.juhezi.orange.media.experimental

import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import me.juhezi.eternal.builder.ByteBufferProvider
import me.juhezi.eternal.builder.buildBackgroundHandler
import me.juhezi.eternal.global.judge

/**
 * PCM 播放器
 *
 * 播放成功！！！
 *
 */
class PcmPlayer {

    fun play(path: String) {
        val sampleRateInHz = 8000
        val bufferSize = AudioTrack.getMinBufferSize(
            sampleRateInHz,  // 采样率
            AudioFormat.CHANNEL_CONFIGURATION_STEREO,   // 双声道
            AudioFormat.ENCODING_PCM_16BIT  // 字长
        )
        val trackPlayer = AudioTrack(   // 应该使用 Builder 创建
            AudioManager.STREAM_MUSIC,
            sampleRateInHz,
            AudioFormat.CHANNEL_CONFIGURATION_STEREO,
            AudioFormat.ENCODING_PCM_16BIT,
            bufferSize,
            AudioTrack.MODE_STREAM  // 用户在应用程序通过 write 方式把数据一次一次地写到 audioTrack 中。
        )
        trackPlayer.play()
        val provider = ByteBufferProvider(path)
        var length = 0
        val buffer = ByteArray(bufferSize)
        while (judge {
                length = provider.provide(buffer)
                length != -1
            }) {
            trackPlayer.write(buffer, 0, length)
        }

        trackPlayer.stop()
        trackPlayer.release()
        provider.close()
    }

}