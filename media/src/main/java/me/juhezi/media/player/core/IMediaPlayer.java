package me.juhezi.media.player.core;

import android.graphics.SurfaceTexture;
import android.support.annotation.FloatRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.Surface;
import android.view.TextureView;
import me.juhezi.media.commen.bean.MediaSource;
import me.juhezi.media.commen.config.ScaleMode;
import me.juhezi.media.player.logger.PlayerLogger;
import me.juhezi.media.player.model.VideoTrack;

import java.util.List;

public interface IMediaPlayer {

    // 开始加载，卡顿开始
    int MEDIA_INFO_BUFFERING_START = 0x01;
    // 加载完成，卡顿结束
    int MEDIA_INFO_BUFFERING_END = 0x02;
    // 首帧加载成功
    int MEDIA_INFO_VIDEO_RENDERING_START = 0x03;
    // 续播时首帧加载成功
    int MEDIA_INFO_VIDEO_RENDERING_RESUMED = 0x04;

    /**
     * 播放器未处理的错误、上层逻辑错误、或播放器抛出的异常
     * 统一使用的错误码
     */
    int MEDIA_BUSINESS_ERROR = 8001;

    /**
     * 用户触发 seek 的标志
     */
    String KEY_UER_SEEK = "user_seek";

    /**
     * 设置{@link Surface}用以渲染播放
     *
     * <p>注意：Surface可以为空，因为Surface并不影响播放状态
     * <ul>
     * <li>null表示没有画面的播放。
     * 比如{@link TextureView.SurfaceTextureListener#onSurfaceTextureAvailable(SurfaceTexture, int, int)}未回调之前，
     * Surface不可用，但可以进行播放器的初始化和准备工作</li>
     * <li>在播放过程中，可以设置不同的Surface，实现视频在不同页面之间的无缝切换</li>
     * </ul>
     * </p>
     *
     * @param surface from TextureView、SurfaceView or SurfaceTexture
     */
    void setSurface(@Nullable Surface surface);

    Surface getSurface();

    void setScaleMode(ScaleMode mode);

    ScaleMode getScaleMode();

    /**
     * 设置启播偏移，从offset位置开始播放
     *
     * @param offset 单位ms
     */
    void setStartOffset(int offset);

    void setMediaSource(@NonNull MediaSource source);

    MediaSource getMediaSource();

    /**
     * 准备播放，异步执行
     */
    void prepareAsync();

    /**
     * 开始播放
     */
    void start();

    /**
     * 停止播放
     */
    void stop();

    /**
     * 暂定播放，{@link #start()}恢复播放
     */
    void pause();

    /**
     * 在seek开始之前调用
     */
    void prepareSeek();

    /**
     * 执行Seek
     *
     * @param ms 单位：ms
     */
    void seekTo(int ms);

    /**
     * 设置播放时是否保持屏幕常量
     *
     * @param screenOn
     */
    void setScreenOnWhilePlaying(boolean screenOn);

    /**
     * @return 视频宽度
     */
    int getVideoWidth();

    /**
     * @return 视频高度
     */
    int getVideoHeight();

    /**
     * @return true 可以正常调用播放器的功能，否则可能会抛出IllegalStateException
     */
    boolean isInPlaybackState();

    /**
     * @return 是否正在播放
     */
    boolean isPlaying();

    /**
     * @return 是否暂停了
     */
    boolean isPaused();

    /**
     * @return 是否正在buffer
     */
    boolean isBuffering();

    /**
     * @return 是否播放完成
     */
    boolean isCompleted();

    /**
     * @return 是否处在错误状态
     */
    boolean isError();

    /**
     * @return 播放器是否已release
     */
    boolean isReleased();

    /**
     * @return 当前播放的位置，单位ms
     */
    int getCurrentPosition();

    /**
     * @return 视频的总时长，单位ms
     */
    int getDuration();

    /**
     * @return 获取当前的解码器类型
     */
    String getCodecType();

    /**
     * @param looping 是否循环播放
     */
    void setLooping(boolean looping);

    /**
     * @return 是否正在循环播放中
     */
    boolean isLooping();

    /**
     * @return 获取播放器当前音量[0，1]；-1无效值
     */
    float getVolume();

    /**
     * 设置音量
     *
     * @param volume [0, 1]设置为当前系统音量的百分比
     */
    void setVolume(@FloatRange(from = 0f, to = 1f) float volume);

    /**
     * 释放播放器的资源
     */
    void release();

    /**
     * 重置播放器
     */
    void reset();

    /**
     * 增加播放器的行为监听
     *
     * @param listener
     */
    void addPlayerActionListener(PlayerActionListener listener);

    /**
     * 添加播放器的行为监听，并指定权重
     *
     * @param listener
     * @param priority >0；权重越大的listener优先处理播放器的行为
     */
    void addPlayerActionListener(PlayerActionListener listener, int priority);

    /**
     * 移除播放器的行为监听
     *
     * @param listener
     */
    void removePlayerActionListener(PlayerActionListener listener);

    /**
     * 增加播放器的状态监听
     *
     * @param listener
     */
    void addPlayerInfoListener(PlayerInfoListener listener);

    /**
     * 添加播放器的状态监听
     *
     * @param listener
     * @param priority >0；权重越大的listener优先处理播放器的状态
     */
    void addPlayerInfoListener(PlayerInfoListener listener, int priority);

    /**
     * 移除播放器的状态监听
     *
     * @param listener
     */
    void removePlayerInfoListener(PlayerInfoListener listener);

    /**
     * 获取播放器的logger
     *
     * @return
     */
    PlayerLogger logger();

    /**
     * 播放器内置的线程池
     *
     * @return
     */
    PlayerOperationExecutor operationExecutor();

    /**
     * 播放器可以存储一些额外信息，按{key, value}的形式。播放器release后失效
     *
     * @param key
     * @param value
     */
    void saveExtraInfo(String key, Object value);

    /**
     * 按key获取之前暂存在播放器中的额外数据，注意数据会被移除
     *
     * @param key
     * @param clazz
     * @return
     */
    <T> T fetchExtraInfo(String key, Class<T> clazz);

    /**
     * 同{@link #fetchExtraInfo(String, Class)}，但数据不会移除，可以多次使用
     *
     * @param key
     * @return
     */
    <T> T getExtraInfo(String key, Class<T> clazz);

    /**
     * 设置播放器的属性
     *
     * @param attribution
     * @param value
     */
    void setAttribution(int attribution, int value);

    /**
     * 获取播放器的属性
     *
     * @param attribution
     * @param valueIfNotFound
     * @return
     */
    int getAttribution(int attribution, int valueIfNotFound);

    /**
     * 获取当前视频支持的所有track，注意目前仅返回视频轨
     * not support
     *
     * @return
     */
    List<VideoTrack> getAllTracks();

    /**
     * 获取当前正在播放的视频轨
     * not support
     *
     * @return
     */
    VideoTrack getCurrentTrack();

    /**
     * 切换track
     * not support
     *
     * @param track
     */
    void switchTrack(VideoTrack track);

    /**
     * 是否支持倍速
     */
    boolean isSupportSpeed();

    /**
     * 获取当前倍速
     */
    float getSpeed();

    /**
     * 设置倍速
     */
    void setSpeed(float speed);
}
