package com.juhezi.orange.bridge;

public class OrangeBridge {

    static {
        System.loadLibrary("avutil-56");
        System.loadLibrary("swresample-3");
        System.loadLibrary("avcodec-58");
        System.loadLibrary("avformat-58");
        System.loadLibrary("swscale-5");
        System.loadLibrary("avfilter-7");
        System.loadLibrary("avdevice-58");
        System.loadLibrary("orange");
    }

    /**
     * 打印 FFmpeg 支持的协议
     *
     * @return
     */
    public static native String getUrlProtocolInfo();

    /**
     * 打印 FFmpeg 支持的封装格式
     *
     * @return
     */
    public static native String getAvFormationInfo();

    /**
     * 打印 FFmpeg 支持的编解码器
     *
     * @return
     */
    public static native String getAvCodecInfo();

    /**
     * 打印 FFmpeg 支持的滤镜
     *
     * @return
     */
    public static native String getAvFilterInfo();

    /**
     * 打印 FFmpeg 的配置信息
     *
     * @return
     */
    public static native String getConfigurationInfo();

}
