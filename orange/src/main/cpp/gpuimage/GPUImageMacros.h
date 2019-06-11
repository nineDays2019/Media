//
// Created by yunrui on 2019/6/3.
//

#ifndef LEGEND_GPUIMAGEMACROS_H
#define LEGEND_GPUIMAGEMACROS_H

#define PLATFORM_UNKNOWN 0
#define PLATFORM_ANDROID 1
#define PLATFORM_IOS 2

#define PLATFORM PLATFORM_UNKNOWN

#if defined(__ANDROID__) || defined(ANDROID)

#undef PLATFORM
#define PLATFORM PLATFORM_ANDROID

#elif defined(__APPLE__)

#undef PLATFORM
#define PLATFORM PLATFORM_IOS

#endif

#define NS_GI_BEGIN     namespace GPUImage {
#define NS_GI_END       }
#define USING_NS_GI     using namespace GPUImage;

#if PLATFORM == PLATFORM_ANDROID

#include <GLES/gl.h>
#include <GLES2/gl2.h>
#include <GLES2/gl2ext.h>
#include <android/log.h>

#define TAG "JuheziNative"
#define LOGI(FORMAT, ...) __android_log_print(ANDROID_LOG_INFO,TAG,FORMAT,##__VA_ARGS__);
#define LOGE(FORMAT, ...) __android_log_print(ANDROID_LOG_ERROR,TAG,FORMAT,##__VA_ARGS__);

#elif PLATFORM == PLATFORM_IOS

#include <OpenGLES/ES2/gl.h>
#include <OpenGLES/ES2/glext.h>
#include <OpenGLES/ES3/gl.h>
#include <OpenGLES/ES3/glext.h>

#endif

#define NO_TEXTURE -1
#define NO_FRAMEBUFFER -1

#define STRINGIZE(x) #x
#define STRINGIZE2(x) STRINGIZE(x)
#define SHADER_STRING(text) @ STRINGIZE2(text)

#endif //LEGEND_GPUIMAGEMACROS_H
