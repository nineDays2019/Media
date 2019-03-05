//
// Created by juhezi on 19-2-26.
//

#include <jni.h>
#include "../cpp/common/common_tools.h"

extern "C"
{
#include "../cpp/media/media_tools.h"
#include "libavformat/avformat.h"
};

extern "C"
JNIEXPORT jstring JNICALL
Java_com_juhezi_orange_bridge_OrangeBridge_getUrlProtocolInfo(JNIEnv *env, jclass type) {
    return env->NewStringUTF(getUrlProtocolInfo());
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_juhezi_orange_bridge_OrangeBridge_getAvFormationInfo(JNIEnv *env, jclass type) {
    return env->NewStringUTF(getAvFormatInfo());
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_juhezi_orange_bridge_OrangeBridge_getAvCodecInfo(JNIEnv *env, jclass type) {
    return env->NewStringUTF(getAvCodecInfo());
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_juhezi_orange_bridge_OrangeBridge_getAvFilterInfo(JNIEnv *env, jclass type) {
    return env->NewStringUTF(getAvFilterInfo());
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_juhezi_orange_bridge_OrangeBridge_getConfigurationInfo(JNIEnv *env, jclass type) {
    return env->NewStringUTF(getConfigurationInfo());
}