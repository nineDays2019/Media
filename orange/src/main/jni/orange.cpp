//
// Created by juhezi on 19-2-26.
//

#include <jni.h>
#include <android/log.h>

extern "C"
{
#include "libavformat/avformat.h"
};

#define TAG "JuheziNative"
#define LOGI(FORMAT, ...) __android_log_print(ANDROID_LOG_INFO,TAG,FORMAT,##__VA_ARGS__);  // 输出到AS的log中
#define LOGE(FORMAT, ...) __android_log_print(ANDROID_LOG_ERROR,TAG,FORMAT,##__VA_ARGS__);

extern "C"
JNIEXPORT void JNICALL
Java_com_juhezi_orange_bridge_OrangeBridge_test(JNIEnv *env, jobject instance, jstring url_) {
    const char *url = env->GetStringUTFChars(url_, NULL);
    LOGI("url: %s", url);
    av_register_all();
    AVCodec *c_temp = av_codec_next(NULL);
    while (c_temp != NULL) {
        switch (c_temp->type) {
            case AVMEDIA_TYPE_VIDEO:
                LOGI("[Video]:%s", c_temp->name);
                break;
            case AVMEDIA_TYPE_AUDIO:
                LOGI("[Audio]:%s", c_temp->name);
                break;
            default:
                LOGI("[Other]:%s", c_temp->name);
                break;
        }
        c_temp = c_temp->next;
    }
    env->ReleaseStringUTFChars(url_, url);
}