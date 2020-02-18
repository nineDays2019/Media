package com.juhezi.orange.media.experimental

import android.media.*
import java.lang.Exception

/**
 * PCM 边录边播
 */
fun pcm_record_and_play() {
    // 采样率
    val sampleRateInHz = 8000
    val bufferSize = AudioTrack.getMinBufferSize(
        sampleRateInHz,  // 采样率
        AudioFormat.CHANNEL_CONFIGURATION_STEREO,   // 双声道
        AudioFormat.ENCODING_PCM_16BIT  // 字长
    )   // 计算缓冲区字节大小

    val audioRecorder = AudioRecord(
        MediaRecorder.AudioSource.MIC,
        sampleRateInHz,
        AudioFormat.CHANNEL_CONFIGURATION_STEREO,
        AudioFormat.ENCODING_PCM_16BIT,
        bufferSize
    )

    val trackPlayer = AudioTrack(   // 应该使用 Builder 创建
        AudioManager.STREAM_MUSIC,
        sampleRateInHz,
        AudioFormat.CHANNEL_CONFIGURATION_STEREO,
        AudioFormat.ENCODING_PCM_16BIT,
        bufferSize,
        AudioTrack.MODE_STREAM  // 用户在应用程序通过 write 方式把数据一次一次地写到 audioTrack 中。
    )
    try {
        audioRecorder.startRecording()
        trackPlayer.play()

        val startTime = System.currentTimeMillis()
        val buffers = Array<ByteArray>(2) { ByteArray(bufferSize) } // 双缓冲
        var currentWriteBuffer = 0  // 当前写入的 buffer 号

        while (true) {
            val length = audioRecorder.read(buffers[currentWriteBuffer], 0, bufferSize)
            trackPlayer.write(buffers[1 - currentWriteBuffer], 0, length)
            currentWriteBuffer = 1 - currentWriteBuffer
            if (System.currentTimeMillis() - startTime >= 20000) {   // 只录制 5 s
                break
            }
        }

        audioRecorder.stop()
        audioRecorder.release()
        trackPlayer.stop()
        trackPlayer.release()
    } catch (e: Exception) {
        e.printStackTrace()
    }


}