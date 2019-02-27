package me.juhezi.eternal.media

import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMuxer
import me.juhezi.eternal.global.logi
import me.juhezi.eternal.global.range
import java.io.IOException
import java.nio.ByteBuffer

/**
 * 获取媒体格式
 */
fun getMediaFormat(path: String): List<String> {
    logi("path is $path")
    val extractor = MediaExtractor()
    try {
        extractor.setDataSource(path)
    } catch (e: IOException) {
        e.printStackTrace()
    }
    val tracks = extractor.trackCount
    val result = range(end = tracks).map {
        extractor.getTrackFormat(it).getString(MediaFormat.KEY_MIME)
    }
    extractor.release()
    return result
}

/**
 * 提取音、视频
 */
fun trackExtractor(inPath: String, outPath: String, video: Boolean) {
    val mediaExtractor = MediaExtractor()
    mediaExtractor.setDataSource(inPath)
    val mediaMuxer = MediaMuxer(outPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
    var trackIndex = 0
    val trackCount = mediaExtractor.trackCount
    for (i in 0 until trackCount) {
        val trackFormat = mediaExtractor.getTrackFormat(i)
        val mime = trackFormat.getString(MediaFormat.KEY_MIME)
        if (mime.startsWith("video/") && video) {
            trackIndex = mediaMuxer.addTrack(trackFormat)
            mediaExtractor.selectTrack(i)
            break
        } else if (mime.startsWith("audio/") && !video) {
            trackIndex = mediaMuxer.addTrack(trackFormat)
            mediaExtractor.selectTrack(i)
            break
        }
    }
    mediaMuxer.start()
    val buffer = ByteBuffer.allocate(1024 * 1000)
    val bufferInfo = MediaCodec.BufferInfo()
    val videoSampleTime = getSampleTime(mediaExtractor, buffer)
    while (true) {
        val readSampleDataSize = mediaExtractor.readSampleData(buffer, 0)   // 读取数据
        if (readSampleDataSize < 0) {
            break
        }
        // 写入数据的大小
        bufferInfo.size = readSampleDataSize
        // 偏移量
        bufferInfo.offset = 0
        // 是否为关键帧
        bufferInfo.flags = mediaExtractor.sampleFlags
        // PTS 单位为微秒，必须为递增
        bufferInfo.presentationTimeUs += videoSampleTime
        mediaMuxer.writeSampleData(trackIndex, buffer, bufferInfo)
        mediaExtractor.advance()    // 下一帧
    }
    mediaExtractor.release()
    mediaMuxer.stop()
    mediaMuxer.release()
}

// 计算帧时间距
fun getSampleTime(mediaExtractor: MediaExtractor, byteBuffer: ByteBuffer): Long {
    val videoSampleTime: Long
    mediaExtractor.readSampleData(byteBuffer, 0)
    //skip first I frame
    if (mediaExtractor.sampleFlags == MediaExtractor.SAMPLE_FLAG_SYNC) {
        mediaExtractor.advance()
    }

    mediaExtractor.readSampleData(byteBuffer, 0)
    val firstVideoPTS = mediaExtractor.sampleTime
    mediaExtractor.advance()
    mediaExtractor.readSampleData(byteBuffer, 0)
    val secondVideoPTS = mediaExtractor.sampleTime
    videoSampleTime = Math.abs(secondVideoPTS - firstVideoPTS)
    return videoSampleTime
}

fun trackMuxer(videoPath: String, audioPath: String,
               outputPath: String) {
    val videoExtractor = MediaExtractor()
    videoExtractor.setDataSource(videoPath)
    val videoTrackIndex = range(end = videoExtractor.trackCount).first {
        videoExtractor.getTrackFormat(it).getString(MediaFormat.KEY_MIME).startsWith("video/")
    }
    val audioExtractor = MediaExtractor()
    audioExtractor.setDataSource(audioPath)
    val audioTrackIndex = range(end = audioExtractor.trackCount).first {
        audioExtractor.getTrackFormat(it).getString(MediaFormat.KEY_MIME).startsWith("audio/")
    }
    videoExtractor.selectTrack(videoTrackIndex)
    audioExtractor.selectTrack(audioTrackIndex)
    val videoBufferInfo = MediaCodec.BufferInfo()
    val audioBufferInfo = MediaCodec.BufferInfo()
    val mediaMuxer = MediaMuxer(outputPath,
            MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
    val writeVideoTrackIndex = mediaMuxer.addTrack(videoExtractor.getTrackFormat(videoTrackIndex))
    val writeAudioTrackIndex = mediaMuxer.addTrack(audioExtractor.getTrackFormat(audioTrackIndex))
    mediaMuxer.start()
    val byteBuffer = ByteBuffer.allocate(1024 * 1000)
    val videoSampleTime = getSampleTime(videoExtractor, byteBuffer)
    while (true) {
        val readVideoSampleSize = videoExtractor.readSampleData(byteBuffer, 0)
        if (readVideoSampleSize < 0) {
            break
        }
        videoBufferInfo.size = readVideoSampleSize
        videoBufferInfo.presentationTimeUs += videoSampleTime
        videoBufferInfo.offset = 0
        videoBufferInfo.flags = videoExtractor.sampleFlags
        mediaMuxer.writeSampleData(writeVideoTrackIndex, byteBuffer, videoBufferInfo)
        videoExtractor.advance()
    }
    val audioSampleTime = getSampleTime(audioExtractor, byteBuffer)
    while (true) {
        val readAudioSampleSize = audioExtractor.readSampleData(byteBuffer, 0)
        if (readAudioSampleSize < 0) {
            break
        }
        audioBufferInfo.size = readAudioSampleSize
        audioBufferInfo.presentationTimeUs += audioSampleTime
        audioBufferInfo.offset = 0
        audioBufferInfo.flags = audioExtractor.sampleFlags
        mediaMuxer.writeSampleData(writeAudioTrackIndex, byteBuffer, audioBufferInfo)
        audioExtractor.advance()
    }
    mediaMuxer.stop()
    mediaMuxer.release()
    videoExtractor.release()
    audioExtractor.release()
}