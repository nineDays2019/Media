//
// Created by yunrui on 2019/3/4.
//

#ifndef LEGEND_COMMON_UTILS_H
#define LEGEND_COMMON_UTILS_H

#include <android/log.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <memory.h>
#include <math.h>
#include <stdbool.h>
#include <jni.h>

#define TAG "JuheziNative"
#define LOGI(FORMAT, ...) __android_log_print(ANDROID_LOG_INFO,TAG,FORMAT,##__VA_ARGS__);
#define LOGE(FORMAT, ...) __android_log_print(ANDROID_LOG_ERROR,TAG,FORMAT,##__VA_ARGS__);


char *convertJString2Char(JNIEnv *env, jstring j_str) {
    char *c_str = NULL;
    jclass clsstring = (*env)->FindClass(env, "java/lang/String");
    jstring strencode = (*env)->NewStringUTF(env, "utf-8");
    jmethodID mid = (*env)->GetMethodID(env, clsstring, "getBytes", "(Ljava/lang/String;)[B");
    jbyteArray barr = (jbyteArray) (*env)->CallObjectMethod(env, j_str, mid, strencode);
    jsize alen = (*env)->GetArrayLength(env, barr);
    jbyte *ba = (*env)->GetByteArrayElements(env, barr, JNI_FALSE);
    if (alen > 0) {
        c_str = (char *) malloc(alen + 1);
        memcpy(c_str, ba, alen);
        c_str[alen] = 0;
    }
    (*env)->ReleaseByteArrayElements(env, barr, ba, 0);
    return c_str;
}

#endif //LEGEND_COMMON_UTILS_H
