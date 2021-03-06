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
#include "../cpp/media/pcm_tools.h"
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

//------------------------
extern "C"
JNIEXPORT jint JNICALL
Java_com_juhezi_orange_bridge_OrangeBridge_decode2Pcm(JNIEnv *env, jclass clazz, jstring audio_path,
                                                      jstring pcm_path) {
    const char *temp_audio = env->GetStringUTFChars(audio_path, nullptr);
    const char *temp_pcm = env->GetStringUTFChars(pcm_path, nullptr);
    char *c_audio_path = new char[strlen(temp_audio) + 1];
    char *c_pcm_path = new char[strlen(temp_pcm) + 1];
    strcpy(c_audio_path, temp_audio);
    strcpy(c_pcm_path, temp_pcm);
    int ret = decode_to_pcm(c_audio_path, c_pcm_path);
    env->ReleaseStringUTFChars(audio_path, temp_audio);
    env->ReleaseStringUTFChars(pcm_path, temp_pcm);
    return ret;
}