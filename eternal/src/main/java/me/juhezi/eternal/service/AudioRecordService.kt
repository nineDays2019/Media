package me.juhezi.eternal.service

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import me.juhezi.eternal.global.logi
import java.io.File
import java.io.FileOutputStream

class AudioRecordService {

    companion object {
        var SAMPLE_RATE_IN_HZ = 44100   // 采样率
        private val CHANNEL_CONFIGURATION = AudioFormat.CHANNEL_IN_DEFAULT  // 单声道
        private val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
        val instance = AudioRecordService()
    }

    private var audioRecord: AudioRecord? = null
    private var bufferSizeInBytes: Int = 0
    private var isRecording = false
    private var recordThread: Thread? = null
    private var outputStream: FileOutputStream? = null

    fun initMeta() {
        audioRecord?.release()
        // 获取音频缓冲区大小
        bufferSizeInBytes = AudioRecord.getMinBufferSize(
                SAMPLE_RATE_IN_HZ, CHANNEL_CONFIGURATION, AUDIO_FORMAT)
        try {
            audioRecord = AudioRecord(MediaRecorder.AudioSource.MIC,
                    SAMPLE_RATE_IN_HZ, CHANNEL_CONFIGURATION, AUDIO_FORMAT, bufferSizeInBytes)
        } catch (e: IllegalArgumentException) {
            logi(e.message!!)
        }

        if (audioRecord == null ||
                audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
            SAMPLE_RATE_IN_HZ = 16000
            bufferSizeInBytes = AudioRecord.getMinBufferSize(SAMPLE_RATE_IN_HZ,
                    CHANNEL_CONFIGURATION, AUDIO_FORMAT)
            try {
                audioRecord = AudioRecord(MediaRecorder.AudioSource.MIC,
                        SAMPLE_RATE_IN_HZ,
                        CHANNEL_CONFIGURATION,
                        AUDIO_FORMAT,
                        bufferSizeInBytes)
            } catch (e: IllegalArgumentException) {
                logi("Again ${e.message}")
            }
        }

        if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
            throw Exception("初始化失败")
        }

    }

    fun start(filePath: String) {
        audioRecord?.run {
            if (state != AudioRecord.STATE_INITIALIZED) {
                null
            } else {
                startRecording()
            } ?: throw Exception("Error")
        }
        isRecording = true
        recordThread = Thread(RecordRunnable(filePath))
        recordThread!!.start()
    }

    private fun getAudioRecordBuffer(bufferSizeInBytes: Int, audioSamples: ByteArray): Int {
        audioRecord?.run {
            return read(audioSamples, 0, bufferSizeInBytes)
        } ?: return 0
    }

    fun stop() {
        audioRecord?.run {
            isRecording = false
            recordThread?.join()
            releaseAudioRecord()
            outputStream?.close()
            outputStream = null
        }
    }

    private fun releaseAudioRecord() {
        audioRecord?.run {
            stop()
            release()
        }
    }

    inner class RecordRunnable(var outputFilepath: String) : Runnable {
        override fun run() {

            val file = File(outputFilepath)
            if (!file.exists()) {
                file.createNewFile()
            }
            outputStream = FileOutputStream(outputFilepath)

            val audioSamples = ByteArray(bufferSizeInBytes)
            outputStream.use {
                while (isRecording) {
                    val audioSampleSize = getAudioRecordBuffer(bufferSizeInBytes, audioSamples)
                    if (audioSampleSize != 0) {
                        outputStream?.write(audioSamples)
                    }
                }
            }
        }

    }

}