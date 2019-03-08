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

    int ret = decode("/storage/emulated/0/demo.mp4", "/storage/emulated/0/output.yuv");
    LOGI("result is %d.", ret)

}