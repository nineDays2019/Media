package com.juhezi.orange.media.experimental

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import me.juhezi.eternal.extension.ensureExist
import me.juhezi.eternal.extension.i
import java.io.FileOutputStream
import java.io.IOException

class PcmRecorder {

    fun record(path: String) {

        i("path is $path")

        path.ensureExist(true)

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
        try {
            audioRecorder.startRecording()
            val startTime = System.currentTimeMillis()
            val buffer = ByteArray(bufferSize)
            val fos = FileOutputStream(path)
            while (true) {
                val length = audioRecorder.read(buffer, 0, bufferSize)
                fos.write(buffer, 0, length)
                fos.flush()
                if (System.currentTimeMillis() - startTime >= 10000) {   // 只录制 5 s
                    break
                }
            }
            audioRecorder.stop()
            audioRecorder.release()
            try {
                fos.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}