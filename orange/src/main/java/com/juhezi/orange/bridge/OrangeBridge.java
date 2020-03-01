package com.juhezi.orange.bridge;

import com.juhezi.orange.utils.NativeUtils;

public class OrangeBridge {

    static {
        NativeUtils.loadLibraries();
    }

    public static native void test();

    public static native void testJson();

    public static native int decode2Pcm(String audioPath, String pcmPath);

}
