//
// Created by juhezi on 20-3-1.
//

#include "ffmpeg_utils.h"

void set_shoutcast_metadata(AVFormatContext *fmt_ctx) {

    char *value = NULL;

    if (av_opt_get(fmt_ctx, "ic_metadata_packet", 1, (uint8_t **) &value) < 0) {
        value = NULL;
    }

    if (value && value[0]) {
        av_dict_set(&fmt_ctx->metadata, ICY_METADATA, value, 0);
    }

}

void set_duration(AVFormatContext *fmt_ctx) {
    char value[30] = {0};
    int duration = 0;
    if (fmt_ctx) {
        if (fmt_ctx->duration != AV_NOPTS_VALUE) {
            duration = (fmt_ctx->duration / AV_TIME_BASE) * 1000;
        }
    }
    sprintf(value, "%d", duration);
    av_dict_set(&fmt_ctx->metadata, DURATION, value, 0);
}

void set_codec(AVFormatContext *fmt_ctx, int i) {
    const char *codec_type = av_get_media_type_string(fmt_ctx->streams[i]->codecpar->codec_type);
    if (!codec_type) {
        return;
    }
    const char *codec_name = avcodec_get_name(fmt_ctx->streams[i]->codecpar->codec_type);

    if (strcmp(codec_type, "audio") == 0) {
        av_dict_set(&fmt_ctx->metadata, AUDIO_CODEC, codec_name, 0);
    } else if (strcmp(codec_type, "video") == 0) {
        av_dict_set(&fmt_ctx->metadata, VIDEO_CODEC, codec_name, 0);
    }
}

void set_rotation(AVFormatContext *fmt_ctx, AVStream *audio_st, AVStream *video_st) {
    if (!extract_metadata_internal(fmt_ctx, audio_st, video_st, ROTATE) && video_st && audio_st) {
        AVDictionaryEntry *entry = av_dict_get(video_st->metadata, ROTATE, NULL,
                                               AV_DICT_MATCH_CASE);
        if (entry && entry->value) {
            av_dict_set(&fmt_ctx->metadata, ROTATE, entry->value, 0);
        } else {
            av_dict_set(&fmt_ctx->metadata, ROTATE, "0", 0);
        }
    }
}

void set_framerate(AVFormatContext *fmt_ctx, AVStream *audio_st, AVStream *video_st) {
    char value[30] = {0};
    if (video_st && video_st->avg_frame_rate.den && video_st->avg_frame_rate.num) {
        double d = av_q2d(video_st->avg_frame_rate);
        uint64_t v = lrintf(d * 100);
        if (v % 100) {
            sprintf(value, "%3.2f", d);
        } else if (v % (100 * 1000)) {
            sprintf(value, "%1.0f", d);
        } else {
            sprintf(value, "%1.0fk", d / 1000);
        }
        av_dict_set(&fmt_ctx->metadata, FRAMERATE, value, 0);
    }

}

void set_filesize(AVFormatContext *fmt_ctx) {
    char value[30] = {0};
    int64_t size = fmt_ctx ? avio_size(fmt_ctx->pb) : -1;
    sprintf(value, "%"PRId64, size);
    av_dict_set(&fmt_ctx->metadata, FILESIZE, value, 0);
}

void set_chapter_count(AVFormatContext *fmt_ctx) {
    char value[30] = {0};
    int count = 0;
    if (fmt_ctx) {
        if (fmt_ctx->nb_chapters) {
            count = fmt_ctx->nb_chapters;
        }
    }
    sprintf(value, "%d", count);
    av_dict_set(&fmt_ctx->metadata, CHAPTER_COUNT, value, 0);
}

void set_video_dimensions(AVFormatContext *fmt_ctx, AVStream *video_st) {
    char value[30] = {0};
    if (video_st) {
        sprintf(value, "%d", video_st->codecpar->width);
        av_dict_set(&fmt_ctx->metadata, VIDEO_WIDTH, value, 0);
        sprintf(value, "%d", video_st->codecpar->height);
        av_dict_set(&fmt_ctx->metadata, VIDEO_HEIGHT, value, 0);
    }
}

const char *
extract_metadata_internal(AVFormatContext *fmt_ctx, AVStream *audio_st, AVStream *video_st,
                          const char *key) {
    char *value = NULL;
    if (!fmt_ctx) {
        return value;
    }
    if (key) {
        if (av_dict_get(fmt_ctx->metadata, key, NULL, AV_DICT_MATCH_CASE)) {
            value = av_dict_get(fmt_ctx->metadata, key, NULL, AV_DICT_MATCH_CASE)->value;
        } else if (audio_st && av_dict_get(audio_st->metadata, key, NULL, AV_DICT_MATCH_CASE)) {
            value = av_dict_get(audio_st->metadata, key, NULL, AV_DICT_MATCH_CASE)->value;
        } else if (video_st && av_dict_get(video_st->metadata, key, NULL, AV_DICT_MATCH_CASE)) {
            value = av_dict_get(video_st->metadata, key, NULL, AV_DICT_MATCH_CASE)->value;
        }
    }
    return value;
}

int get_metadata_internal(AVFormatContext *fmt_ctx, AVDictionary **metadata) {
    if (!fmt_ctx) {
        return FAILURE;
    }

    set_shoutcast_metadata(fmt_ctx);
    av_dict_copy(metadata, fmt_ctx->metadata, 0);

    return SUCCESS;
}


const char *extract_metadata_from_chapter_internal(AVFormatContext *fmt_ctx, AVStream *audio_st,
                                                   AVStream *video_st, const char *key,
                                                   int chapter) {

    char *value = NULL;
    if (!fmt_ctx || fmt_ctx->nb_chapters <= 0) {
        return value;
    }

    if (chapter < 0 || chapter >= fmt_ctx->nb_chapters) {
        return value;
    }

    AVChapter *ch = fmt_ctx->chapters[chapter];
    LOGI("Found metadata")
    AVDictionaryEntry *tag = NULL;
    while ((tag = av_dict_get(ch->metadata, "", tag, AV_DICT_MATCH_CASE))) {
        LOGI("key %s: \n", tag->key)
        LOGI("Value %s: \n", tag->value)
    }

    if (strcmp(key, CHAPTER_START_TIME) == 0) {
        char time[30];
        int start_time = ch->start * av_q2d(ch->time_base) * 1000;
        sprintf(time, "%d", start_time);
        value = malloc(strlen(time));
        sprintf(value, "%s", time);
    } else if (strcmp(key, CHAPTER_END_TIME) == 0) {
        char time[30];
        int end_time = ch->end * av_q2d(ch->time_base) * 1000;
        sprintf(time, "%d", end_time);
        value = malloc(strlen(time));
        asprintf(value, "%s", time);
    } else if (av_dict_get(ch->metadata, key, NULL, AV_DICT_MATCH_CASE)) {
        value = av_dict_get(ch->metadata, key, NULL, AV_DICT_MATCH_CASE)->value;
    }

    return value;

}




