package com.juhezi.orange.media.demuxer

import android.os.Handler
import java.nio.ByteBuffer

/**
 * FFmpeg 音视频分离
 * avformat
 */
class SoftDemuxer(
    sourcePath: String,
    handler: Handler?
) : EternalDemuxer(sourcePath, handler) {
    override fun loopReadData(index: Int, async: Boolean, closure: (Int, Int, ByteBuffer) -> Boolean) {
    }

    override fun getTrackCount(): Int = 0

    override fun release() {
    }

    override fun saveTrack(index: Int, type: Int, outputPath: String, callBack: SaveTrackCallBack?) {
    }


}