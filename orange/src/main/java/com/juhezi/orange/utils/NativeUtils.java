package com.juhezi.orange.utils;

public class NativeUtils {

    public static void loadLibraries() {
        System.loadLibrary("avutil-56");
        System.loadLibrary("swresample-3");
        System.loadLibrary("avcodec-58");
        System.loadLibrary("avformat-58");
        System.loadLibrary("swscale-5");
        System.loadLibrary("avfilter-7");
        System.loadLibrary("avdevice-58");
        System.loadLibrary("orange");
    }

}
