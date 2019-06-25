package com.juhezi.orange.media.demuxer

import android.os.Handler
import java.nio.ByteBuffer

/**
 * 音视频分离
 * @param sourcePath 源文件路径
 * @param handler 解封装过程所在的线程
 * 需要开一个异步线程
 */
abstract class EternalDemuxer(
    protected val sourcePath: String,
    protected val handler: Handler?
) {

    companion object {
        const val VIDEO_TRACK = 0
        const val AUDIO_TRACK = 1
        const val TEXT_TRACK = 2
        const val OTHER_TRACK = 3
    }

    /**
     * 视频信道索引
     */
    open val videoTrackIndexList: MutableList<Int> = mutableListOf()

    /**
     * 音频信道索引
     */
    open val audioTrackIndexList: MutableList<Int> = mutableListOf()

    /**
     * 字幕轨道索引
     */
    open val textTrackIndexList: MutableList<Int> = mutableListOf()

    /**
     * 其他轨道索引
     * [不确定有没有]
     */
    open val otherTrackIndexList: MutableList<Int> = mutableListOf()

    open fun defaultVideoTrackIndex() = if (videoTrackIndexList.isEmpty()) -1 else videoTrackIndexList.first()

    open fun defaultAudioTrackIndex() = if (audioTrackIndexList.isEmpty()) -1 else audioTrackIndexList.first()

    open fun defaultTextTrackIndex() = if (textTrackIndexList.isEmpty()) -1 else textTrackIndexList.first()

    open fun defaultOtherTrackIndex() = if (otherTrackIndexList.isEmpty()) -1 else otherTrackIndexList.first()

    /**
     * 信道总数
     */
    abstract fun getTrackCount(): Int

    protected fun runWithHandler(async: Boolean = true, closure: () -> Unit) {
        if (handler == null || !async) {    // 同步执行
            closure()
        } else {
            handler.post {
                closure()
            }
        }
    }

    abstract fun release()

    //---以下函数会在 Handler 对应的线程中进行
    abstract fun saveTrack(
        index: Int = -1,
        type: Int = VIDEO_TRACK,
        outputPath: String,
        callBack: SaveTrackCallBack?
    )

    /**
     * 循环读取数据
     * @param index 信道索引
     * @param async 是否异步
     * @param closure
     *  Int - loopIndex
     *  Int - sampleSize
     *  ByteBuffer - data
     */
    abstract fun loopReadData(index: Int, async: Boolean = true, closure: (Int, Int, ByteBuffer) -> Boolean)

    /**
     * 保存 Track 的回调
     */
    interface SaveTrackCallBack {

        /**
         * @param outputPath 输出路径
         */
        fun onSuccess(outputPath: String)

        /**
         * @param message 错误信息
         */
        fun onFail(message: String)

    }

}