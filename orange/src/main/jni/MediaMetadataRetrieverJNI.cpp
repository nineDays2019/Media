#include <jni.h>
#include <cstring>
#include <omp.h>
#include <assert.h>
#include "../cpp/common/common_tools.h"

//
// Created by juhezi on 20-2-27.
//

static JavaVM *m_vm;

// common Method
static jstring NewStringUTF(JNIEnv *env, const char *data) {
    jstring str = NULL;
    int size = strlen(data);
    jbyteArray array = NULL;
    array = env->NewByteArray(size);
    if (!array) {
        LOGE("NewStringUTF: OOM")
    } else {
        jbyte *bytes = env->GetByteArrayElements(array, NULL);
        if (bytes != NULL) {
            memcpy(bytes, data, size);
            env->ReleaseByteArrayElements(array, bytes, 0);

            jclass string_Clazz = env->FindClass("java/lang/String");
            jmethodID string_initMethodID = env->GetMethodID(string_Clazz, "<init>",
                                                             "([BLjava/lang/String;)V");
            jstring utf = env->NewStringUTF("UTF-8");
            str = static_cast<jstring>(env->NewObject(string_Clazz, string_initMethodID, array,
                                                      utf));
            env->DeleteLocalRef(utf);
        }
    }
    env->DeleteLocalRef(array);
    return str;
}

void jniThrowException(JNIEnv *env, const char *className, const char *message) {
    jclass exception = env->FindClass(className);
    env->ThrowNew(exception, message);
}

static JNINativeMethod nativeMethods[] = {
        /*{"setDataSource",              "(Ljava/lang/String;)V",                      (void *) wseemann_media_FFmpegMediaMetadataRetriever_setDataSource},

        {
         "_setDataSource",
                                       "(Ljava/lang/String;[Ljava/lang/String;[Ljava/lang/String;)V",
                                                                                     (void *) wseemann_media_FFmpegMediaMetadataRetriever_setDataSourceAndHeaders
        },

        {"setDataSource",              "(Ljava/io/FileDescriptor;JJ)V",              (void *) wseemann_media_FFmpegMediaMetadataRetriever_setDataSourceFD},
        {"_getFrameAtTime",            "(JI)[B",                                     (void *) wseemann_media_FFmpegMediaMetadataRetriever_getFrameAtTime},
        {"_getScaledFrameAtTime",      "(JIII)[B",                                   (void *) wseemann_media_FFmpegMediaMetadataRetriever_getScaledFrameAtTime},
        {"extractMetadata",            "(Ljava/lang/String;)Ljava/lang/String;",     (void *) wseemann_media_FFmpegMediaMetadataRetriever_extractMetadata},
        {"extractMetadataFromChapter", "(Ljava/lang/String;I)Ljava/lang/String;",    (void *) wseemann_media_FFmpegMediaMetadataRetriever_extractMetadataFromChapter},
        {"native_getMetadata",         "(ZZLjava/util/HashMap;)Ljava/util/HashMap;", (void *) wseemann_media_FFmpegMediaMetadataRetriever_getMetadata},
        {"getEmbeddedPicture",         "()[B",                                       (void *) wseemann_media_FFmpegMediaMetadataRetriever_getEmbeddedPicture},
        {"release",                    "()V",                                        (void *) wseemann_media_FFmpegMediaMetadataRetriever_release},
        {"setSurface",                 "(Ljava/lang/Object;)V",                      (void *) wseemann_media_FFmpegMediaMetadataRetriever_setSurface},
        {"native_finalize",            "()V",                                        (void *) wseemann_media_FFmpegMediaMetadataRetriever_native_finalize},
        {"native_setup",               "()V",                                        (void *) wseemann_media_FFmpegMediaMetadataRetriever_native_setup},
        {"native_init",                "()V",                                        (void *) wseemann_media_FFmpegMediaMetadataRetriever_native_init},*/
};

jint JNI_OnLoad(JavaVM *vm, void *reserved) {
    m_vm = vm;
    JNIEnv *env = NULL;
    jint result = -1;

    if (vm->GetEnv(reinterpret_cast<void **>(&env), JNI_VERSION_1_6) != JNI_OK) {
        LOGE("ERROR: GetEnv failed")
        return result;
    }
    assert(env != NULL);

    /*if (register_wseemann_media_FFmpegMediaMetadataRetriever(env) < 0) {
        LOGE("ERROR: FFmpegMediaMetadataRetriever native registration failed")
        return result;
    }*/
    // success -- return valid version number
    return JNI_VERSION_1_6;


}