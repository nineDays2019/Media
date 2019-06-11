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

    public static native void test();

    public static native void testJson();

    //--------------------

    protected long mNativeClassId = 0;

    public static final int NoRotation = 0;
    public static final int RotateLeft = 1;//向左旋转
    public static final int RotateRight = 2;//向右旋转
    public static final int FlipVertical = 3;//垂直翻转
    public static final int FlipHorizontal = 4;//水平翻转
    public static final int RotateRightFlipVertical = 5;//向右旋转垂直翻转
    public static final int RotateRightFlipHorizontal = 6;//向右旋转水平旋转
    public static final int Rotate180 = 7;//旋转180度

    public long getNativeClassId() {
        return mNativeClassId;
    }

    //-------------------

    //showView
    protected native long nativeInitShowView();

    protected native boolean nativeShowView(int textureId, long showViewClassId, float time, int rotationMode);

    protected native void nativeReleaseShowView(long showViewClassId);
}
