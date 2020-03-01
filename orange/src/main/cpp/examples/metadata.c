//
// Created by juhezi on 20-2-27.
//

#include <stdio.h>

#include <libavformat/avformat.h>
#include <libavutil/dict.h>

static int metadata() {
    AVFormatContext *fmt_ctx = NULL;
    AVDictionaryEntry *tag = NULL;

    int ret;
    if ((ret = avformat_open_input(&fmt_ctx, "input_file", NULL, NULL)))
        return ret;
    if ((ret = avformat_find_stream_info(fmt_ctx, NULL)) < 0)
        return ret;
    while ((tag = av_dict_get(fmt_ctx->metadata, "", tag, AV_DICT_IGNORE_SUFFIX)))
        printf("%s=%s\n", tag->key, tag->value);
    avformat_close_input(&fmt_ctx);
    return 0;
}