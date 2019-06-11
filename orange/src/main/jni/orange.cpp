//
// Created by juhezi on 19-2-26.
//

#include <jni.h>
#include <iostream>
#include "../cpp/common/common_tools.h"
#include "../cpp/rapidjson/document.h"
#include "../cpp/rapidjson/writer.h"

extern "C"
{
#include "../cpp/media/media_tools.h"
};

using namespace rapidjson;

extern "C"
JNIEXPORT void JNICALL
Java_com_juhezi_orange_bridge_OrangeBridge_test(JNIEnv *env, jclass type) {
    LOGI("Start")
//    int ret = decode("/storage/emulated/0/demo.mp4", "/storage/emulated/0/output.yuv");
    int ret = encode(const_cast<char *>("/storage/emulated/0/output.yuv"),
                     const_cast<char *>("720x480"),
                     const_cast<char *>(""),
                     const_cast<char *>("/storage/emulated/0/nice.ts"));
    LOGI("result is %d.", ret)

}

extern "C"
JNIEXPORT void JNICALL
Java_com_juhezi_orange_bridge_OrangeBridge_testJson(JNIEnv *env, jclass type) {
    const char *json = "{\"project\":\"rapidjson\",\"stars\":10}";
    Document d;
    d.Parse(json);
    if (d.IsObject()) {
        LOGI("is object")
    }
    if (d.HasMember("stars")) {
        // 要用这个语句进行判断，不然会 Crash
    }
    Value &s = d["stars"];
    LOGI("value is %d", d["stars"].GetInt())
    s.SetInt(s.GetInt() + 1);
    StringBuffer buffer;
    Writer<StringBuffer> writer(buffer);
    d.Accept(writer);
    std::cout << buffer.GetString() << std::endl;
    LOGI("buffer = %s", buffer.GetString())
    std::string strTemp = std::string(buffer.GetString());
    LOGE("buffer = %s", strTemp.c_str())
}

//------------------

#include "../cpp/gpuimage/GPUImageMacros.h"
#include "../cpp/gpuimage/GPUImageShowView.hpp"

USING_NS_GI

enum GPUImageEffectType {
    WBGPUImageEffect_None = 0,
    WBGPUImageEffect_BlurMirror,
    WBGPUImageEffect_ElectricShock,
    WBGPUImageEffect_SoulOut,
    WBGPUImageEffect_Fake3D,
    WBGPUImageBlackMagic,
    WBGPUImage70S,
    WBGPUImageXSignal,
    WBGPUImageTwoInput,
    WBGPUImageOverlay
};

extern "C" {

//------------ShowView Start

JNIEXPORT jlong JNICALL
Java_com_juhezi_orange_bridge_OrangeBridge_nativeInitShowView(JNIEnv *env, jobject instance) {
    GPUImageShowView *gpuImageShowView = new(std::nothrow)GPUImageShowView();
    gpuImageShowView->init();
    return (uintptr_t) gpuImageShowView;
}

JNIEXPORT jboolean JNICALL
Java_com_juhezi_orange_bridge_OrangeBridge_nativeShowView(JNIEnv *env, jobject instance, jint textureId,
                                                          jlong showViewClassId, jfloat time, jint rotationMode) {
    GPUImageShowView *gpuImageShowView = ((GPUImageShowView *) showViewClassId);
    if (gpuImageShowView == NULL) {
        return static_cast<jboolean>(false);
    }

    gpuImageShowView->updata(time, static_cast<GLuint>(textureId), rotationMode);
    return static_cast<jboolean>(true);
}

JNIEXPORT void JNICALL
Java_com_juhezi_orange_bridge_OrangeBridge_nativeReleaseShowView(JNIEnv *env, jobject instance, jlong showViewClassId) {
    GPUImageShowView *gpuImageShowView = ((GPUImageShowView *) showViewClassId);
    if (gpuImageShowView == NULL) {
        return;
    }
    gpuImageShowView->release();
}

//------------ShowView End

}