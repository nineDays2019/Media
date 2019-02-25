package me.juhezi.media.player.core;


import me.juhezi.media.player.model.VideoTrack;

/**
 * 播放器的[状态]回调
 */
public interface PlayerInfoListener {

    /**
     * 播放器准备工作完成，可以开始播放时回调
     * @param mp
     */
    void onPrepared(IMediaPlayer mp);

    /**
     * 设置软解、硬解的回调，注意该回调发生在异步线程
     * @param mp
     */
    void onCodecTypeSelect(IMediaPlayer mp);

    /**
     * 播放完成时回调
     * @param mp
     */
    void onCompletion(IMediaPlayer mp);

    /**
     * 视频尺寸变化时回调
     * @param mp
     * @param width
     * @param height
     */
    void onVideoSizeChanged(IMediaPlayer mp, int width, int height);

    /**
     * 当播放进度变化时回调
     * @param mp
     * @param currentPosition 单位ms
     * @param duration 单位ms
     */
    void onProgressUpdate(IMediaPlayer mp, int currentPosition, int duration);

    /**
     * 播放时，缓冲更新的回调
     * @param mp
     * @param percent 已缓冲的时长与视频总时长的比例, 如已缓冲50% ~ return 50
     */
    void onBufferingUpdate(IMediaPlayer mp, int percent);

    /**
     * 播放器错误时回调
     * @param mp
     * @param errorCode 错误码
     * @param errorMode 错误模块
     * @param cause 错误信息
     */
    void onError(IMediaPlayer mp, int errorCode, int errorMode, String cause);

    /**
     * 缓存变化时回调
     * @param mp
     * @param type 类型
     * @param size 大小
     */
    void onCacheUpdate(IMediaPlayer mp, int type, int size);

    /**
     * 播放过程中的关键信息回调
     * @param mp
     * @param what 类型
     * @param extra 额外的标志
     */
    void onInfo(IMediaPlayer mp, int what, int extra);

    /**
     * 首帧开播的回调
     * <p>注意：这个回调对应onInfo中{@link IMediaPlayer#MEDIA_INFO_VIDEO_RENDERING_START}
     * 和{@link IMediaPlayer#MEDIA_INFO_VIDEO_RENDERING_RESUMED}的时机，方便业务层使用</p>
     * @param mp
     * @param what
     * @param extra
     */
    void onFirstFrameStart(IMediaPlayer mp, int what, int extra);

    /**
     * 视频播放每一帧的回调，谨慎用之，不可处理耗时操作
     * @param mp
     * @param what 类型
     */
    void onFrameInfo(IMediaPlayer mp, int what);

    /**
     * 音量调整后回调
     * @param volume [0,1]，当前系统音量的百分比
     */
    void onVolumeChanged(float volume);

    /**
     * seek开始：用户开始拖拽进度条
     * @param mp
     */
    void onSeekStart(IMediaPlayer mp);

    /**
     * 播放器真正seekTo前；与{@link #onSeekStart(IMediaPlayer)}区别：用户可能拖拽进度条来回调整，松手后才真正seek
     * @param mp
     * @param start
     * @param end
     */
    void onSeeking(IMediaPlayer mp, int start, int end);

    /**
     * seek结束时回调
     * @param mp
     * @param start
     * @param end
     */
    void onSeekComplete(IMediaPlayer mp, int start, int end);

    /**
     * track变化
     * @param mp
     * @param track
     */
    void onTrackChanged(IMediaPlayer mp, VideoTrack track);

    /**
     * 播放速度变化
     * @param mp
     * @param speed
     */
    void onSpeedChanged(IMediaPlayer mp, float speed);
}
