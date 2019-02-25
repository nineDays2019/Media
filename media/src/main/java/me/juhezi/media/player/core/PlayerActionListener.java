package me.juhezi.media.player.core;

import android.view.Surface;
import me.juhezi.media.commen.bean.MediaSource;

/**
 * 播放器的[行为]回调
 */
public interface PlayerActionListener {

    /**
     * 播放器初始化
     *
     * @param mp
     */
    void onInitialize(IMediaPlayer mp);

    /**
     * {@link Surface}设置后回调
     *
     * @param mp
     */
    void onSurfaceSet(IMediaPlayer mp);

    /**
     * {@link Surface}切换时回调，可以通过{@link IMediaPlayer#getSurface()}获取当前的Surface
     *
     * @param mp
     */
    void onSurfaceChanged(IMediaPlayer mp);

    /**
     * 数据源设置后回调
     *
     * @param mp
     * @param source
     */
    void onSourceSet(IMediaPlayer mp, MediaSource source);

    /**
     * 播放开始后回调
     *
     * @param mp
     */
    void onStart(IMediaPlayer mp);

    /**
     * 播放暂停时回调
     *
     * @param mp
     */
    void onPause(IMediaPlayer mp);

    /**
     * 播放停止后回调
     *
     * @param mp
     */
    void onStop(IMediaPlayer mp);

    /**
     * 播放器被重置后回调
     *
     * @param mp
     */
    void onReset(IMediaPlayer mp);

    /**
     * 播放器被销毁时回调
     *
     * @param mp
     */
    void onRelease(IMediaPlayer mp);
}
