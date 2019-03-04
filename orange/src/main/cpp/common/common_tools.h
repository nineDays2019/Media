//
// Created by yunrui on 2019/3/4.
//

#ifndef LEGEND_COMMON_UTILS_H
#define LEGEND_COMMON_UTILS_H

#include <android/log.h>

#define TAG "JuheziNative"
#define LOGI(FORMAT, ...) __android_log_print(ANDROID_LOG_INFO,TAG,FORMAT,##__VA_ARGS__);
#define LOGE(FORMAT, ...) __android_log_print(ANDROID_LOG_ERROR,TAG,FORMAT,##__VA_ARGS__);

#endif //LEGEND_COMMON_UTILS_H
