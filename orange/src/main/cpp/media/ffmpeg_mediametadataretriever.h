//
// Created by juhezi on 20-3-1.
//

#ifndef MEDIA_FFMPEG_MEDIAMETADATARETRIEVER_H
#define MEDIA_FFMPEG_MEDIAMETADATARETRIEVER_H

#include "../common/common_tools.h"
#include "ffmpeg_utils.h"
#include <libavcodec/avcodec.h>
#include <libavformat/avformat.h>
#include <libavutil/dict.h>
#include <libswscale/swscale.h>
#include <libavutil/opt.h>

#include <android/native_window_jni.h>

typedef enum {
    OPTION_PREVIOUS_SYNC = 0,
    OPTION_NEXT_SYNC = 1,
    OPTION_CLOSEST_SYNC = 2,
    OPTION_CLOSEST = 3,
} Options;

typedef struct State {
    AVFormatContext *fmt_ctx;
    int audio_stream;
    int video_stream;

    AVStream *audio_st;
    AVStream *video_st;

    int fd; // 什么意思 文件描述符
    int64_t offset; // 不能为 0
    const char *headers;
    struct SwsContext *sws_ctx;
    AVCodecContext *codec_ctx;

    struct SwsContext *scaled_sws_ctx;
    AVCodecContext *scaled_codec_ctx;
    ANativeWindow *nativeWindow;
} State;

struct AVDictionary {
    int count;
    AVDictionaryEntry *elements;
};

int set_data_source_uri(State **ps, const char *path, const char *headers);

int set_data_source_fd(State **ps, int fd, int64_t offset, int64_t length);

const char *extract_metadata(State **ps, const char *key);

const char *extract_metadata_from_chapter(State **ps, const char *key, int chapter);

int get_metadata(State **ps, AVDictionary **metadata);

int get_embedded_picture(State **ps, AVPacket *pkt);

int get_frame_at_time(State **ps, int64_t timeUs, int option, AVPacket *pkt);

int get_scaled_frame_at_time(State **ps, int64_t timeUs, int option, AVPacket *pkt, int width,
                             int height);

int set_native_window(State **ps, ANativeWindow *native_window);

void release(State **ps);


#endif //MEDIA_FFMPEG_MEDIAMETADATARETRIEVER_H
