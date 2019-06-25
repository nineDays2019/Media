package com.juhezi.orange.media.demuxer

import android.media.MediaExtractor
import android.media.MediaFormat
import android.os.Handler
import me.juhezi.eternal.extension.e
import me.juhezi.eternal.extension.i
import me.juhezi.eternal.extension.isEmpty
import me.juhezi.eternal.global.range
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer

/**
 * MediaExtractor 音视频分离
 */
class HardDemuxer(sourcePath: String, handler: Handler) :
    EternalDemuxer(sourcePath, handler) {

    private val extractor = MediaExtractor()

    init {
        extractor.setDataSource(sourcePath)
        range(0, getTrackCount()).forEach {
            val mime = extractor.getTrackFormat(it).getString(MediaFormat.KEY_MIME)
            when {
                mime.startsWith("video/") -> videoTrackIndexList.add(it)
                mime.startsWith("audio/") -> audioTrackIndexList.add(it)
                mime.startsWith("text/") -> textTrackIndexList.add(it)
                else -> otherTrackIndexList.add(it)
            }
        }
    }

    override fun getTrackCount() = extractor.trackCount

    // 这里需要添加一个同步选项
    override fun loopReadData(
        index: Int,
        async: Boolean,
        closure: (Int, Int, ByteBuffer) -> Boolean
    ) {
        runWithHandler(async) {
            val byteBuffer = ByteBuffer.allocate(500 * 1024)
            extractor.selectTrack(index)
            var loopIndex = 0
            while (true) {
                val sampleSize = extractor.readSampleData(byteBuffer, 0)
                if (sampleSize < 0) {
                    i("END!!!")
                    break
                }
                if (!closure(loopIndex, sampleSize, byteBuffer)) {
                    i("Intercepted!")
                    break
                }
                byteBuffer.clear()
                extractor.advance()
                loopIndex++
            }
        }
    }

    override fun saveTrack(index: Int, type: Int, outputPath: String, callBack: SaveTrackCallBack?) {

        runWithHandler {
            val trackIndex = if (index < 0) {
                when (type) {   // 选取各自默认的信道
                    VIDEO_TRACK -> defaultVideoTrackIndex()
                    AUDIO_TRACK -> defaultAudioTrackIndex()
                    TEXT_TRACK -> defaultTextTrackIndex()
                    OTHER_TRACK -> defaultOtherTrackIndex()
                    else -> -1
                }
            } else {    // 指定信道
                when (type) {   // 这里有越界风险，等出现 Crash 以后再保护
                    VIDEO_TRACK -> videoTrackIndexList[index]
                    AUDIO_TRACK -> audioTrackIndexList[index]
                    TEXT_TRACK -> textTrackIndexList[index]
                    OTHER_TRACK -> otherTrackIndexList[index]
                    else -> index
                }
            }
            if (trackIndex == -1) {
                e("Have No Track!!")
                callBack?.onFail("Have No Track!!")
                return@runWithHandler
            }
            if (isEmpty(outputPath)) {
                e("Output Path is Empty!!")
                callBack?.onFail("Output Path is Empty!!")
                return@runWithHandler
            }
            val file = File(outputPath)
            if (!file.exists()) {
                file.createNewFile()
            }

            val fos = FileOutputStream(file)
            // 同步执行
            loopReadData(trackIndex, false) { loopIndex, sampleSize, byteBuffer ->
                i("[$loopIndex] Frame")
                val buffer = ByteArray(sampleSize)
                byteBuffer.get(buffer)
                try {
                    fos.write(buffer)
                    true
                } catch (e: Exception) {
                    e.printStackTrace()
                    callBack?.onFail(e.message ?: "Exception")
                    false
                }
            }
            fos.close()
            callBack?.onSuccess(outputPath)
        }
    }

    override fun release() {
        extractor.release()
    }

}