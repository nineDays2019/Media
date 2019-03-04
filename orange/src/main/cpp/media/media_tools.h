//
// Created by yunrui on 2019/3/4.
//

#ifndef LEGEND_MEDIA_TOOLS_H
#define LEGEND_MEDIA_TOOLS_H


#include <stdio.h>

#include "libavcodec/avcodec.h"
#include "libavformat/avformat.h"
#include "libavfilter/avfilter.h"

#endif //LEGEND_MEDIA_TOOLS_H

/**
 * 获取 FFmpeg 支持的协议
 */
char *getUrlProtocolInfo();

char *getAvFormatInfo();