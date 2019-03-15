//
// Created by juhezi on 19-2-26.
//

#include <jni.h>
#include "../cpp/common/common_tools.h"

extern "C"
{
#include "../cpp/media/media_tools.h"
};

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