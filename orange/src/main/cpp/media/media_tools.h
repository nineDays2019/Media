//
// Created by yunrui on 2019/3/4.
//

#ifndef LEGEND_MEDIA_TOOLS_H
#define LEGEND_MEDIA_TOOLS_H


#include <stdio.h>

#include "libavcodec/avcodec.h"
#include "libavformat/avformat.h"
#include "libavfilter/avfilter.h"
#include "libswscale/swscale.h"
#include "libavutil/imgutils.h"
#include "libavutil/log.h"
#include "libavutil/opt.h"
#include "libavutil/parseutils.h"

#include "../common/common_tools.h"


#endif //LEGEND_MEDIA_TOOLS_H

/**
 * 获取 FFmpeg 支持的协议
 */
char *getUrlProtocolInfo();

/**
 * 获取 FFmpeg 支持的封装格式
 * @return
 */
char *getAvFormatInfo();

/**
 * 获取 FFmpeg 支持的编解码器
 * @return
 */
char *getAvCodecInfo();

/**
 * 获取 FFmpeg 支持的滤镜
 * @return
 */
char *getAvFilterInfo();

/**
 * 获取 FFmpeg 的配置信息
 * @return
 */
char *getConfigurationInfo();

/**
 * 将视频文件解码成 yuv 文件
 * @return
 */
int decode(char *input, char *output);

/**
 * 编码
 */
int encode(char *input, char *resolution, char *setting, char *output);

/**
 * Output FFmpeg's av_log()
 * @param ptr
 * @param level
 * @param format
 * @param vaList
 */
void custom_log(void *ptr, int level, const char *format, va_list vaList);