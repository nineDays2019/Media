package com.juhezi.orange.media.experimental

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.AudioTrack
import android.os.Bundle
import me.juhezi.eternal.extension.ensureExist
import me.juhezi.eternal.global.logd
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.lang.Exception

// wav 全称是 WAVE，无损的音频文件格式
class WavCodec {

    companion object {
        private var sampleRateInHz = 8000
        private val bufferSize = AudioTrack.getMinBufferSize(
            sampleRateInHz,  // 采样率
            AudioFormat.CHANNEL_CONFIGURATION_STEREO,   // 双声道
            AudioFormat.ENCODING_PCM_16BIT  // 字长
        )   // 计算缓冲区字节大小

        @JvmStatic
        fun encode(pcmPath: String, wavPath: String) {
            wavPath.ensureExist()
            var fis: FileInputStream? = null
            var fos: FileOutputStream? = null
            val totalAudioLength: Long
            val totalDataLength: Long
            val channels = 2
            val byteRate = 16 * sampleRateInHz * channels / 8
            val data = ByteArray(byteRate)
            try {
                fis = FileInputStream(pcmPath)
                fos = FileOutputStream(wavPath)
                totalAudioLength = fis.channel.size()
                totalDataLength = totalAudioLength + 36
                WavUtils.writeWaveFileHeader(
                    fos,
                    totalAudioLength, totalDataLength,
                    sampleRateInHz.toLong(), channels, byteRate.toLong()
                )
                while (fis.read(data) != -1) {
                    fos.write(data)
                }

            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                try {
                    fis?.close()
                    fos?.close()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            logd("Wav Encode Done")
        }

        @JvmStatic
        fun decode(wavPath: String, pcmPath: String): Bundle {
            var fis: FileInputStream? = null
            var fos: FileOutputStream? = null
            try {
                fis = FileInputStream(wavPath)
//                fos = FileOutputStream(pcmPath)

                val header = ByteArray(44)
                fis.read(header, 0, header.size)
                logd(String(header))
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                try {
                    fis?.close()
                    fos?.close()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            logd("Wav Decode Done")
            return Bundle()
        }
    }


}