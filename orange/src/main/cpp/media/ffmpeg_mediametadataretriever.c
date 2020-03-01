//
// Created by juhezi on 20-3-1.
//

#include <unistd.h>
#include "ffmpeg_mediametadataretriever.h"

const int TARGET_IMAGE_FORMAT = AV_PIX_FMT_RGBA;
const int TARGET_IMAGE_CODEC = AV_CODEC_ID_PNG;

// todo 使用新的方法
void convert_image(State *state, AVCodecContext *pCodec_ctx, AVFrame *pFrame, AVPacket *avpkt,
                   int *got_packet, int width, int height);

int is_supported_format(int codec_id, int pix_fmt) {
    if ((codec_id == AV_CODEC_ID_PNG || codec_id == AV_CODEC_ID_MJPEG ||
         codec_id == AV_CODEC_ID_BMP) && pix_fmt == AV_PIX_FMT_RGBA) {
        return 1;
    }
    return 0;
}

int get_scaled_context(State *state, AVCodecContext *codec_ctx, int width, int height) {
    AVCodec *targetCodec = avcodec_find_encoder(TARGET_IMAGE_CODEC);
    if (!targetCodec) {
        LOGE("avcodec_find_encoder failed.")
        return FAILURE;
    }

    state->scaled_codec_ctx = avcodec_alloc_context3(targetCodec);
    if (!state->scaled_codec_ctx) {
        LOGE("avcodec_alloc_context3 failed.")
        return FAILURE;
    }

    state->scaled_codec_ctx->bit_rate = state->video_st->codecpar->bit_rate;
    state->scaled_codec_ctx->width = width;
    state->scaled_codec_ctx->height = height;
    state->scaled_codec_ctx->pix_fmt = TARGET_IMAGE_FORMAT;
    state->scaled_codec_ctx->codec_type = TARGET_IMAGE_CODEC;
    state->scaled_codec_ctx->time_base.num = state->video_st->codec->time_base.num;
    state->scaled_codec_ctx->time_base.den = state->video_st->codec->time_base.den;

    if (avcodec_open2(state->scaled_codec_ctx, targetCodec, NULL) < 0) {
        LOGE("avcodec_open2 failed.")
        return FAILURE;
    }

    state->scaled_sws_ctx = sws_getContext(state->video_st->codecpar->width,
                                           state->video_st->codecpar->height,
                                           state->video_st->codec->pix_fmt,
                                           width,
                                           height,
                                           TARGET_IMAGE_FORMAT,
                                           SWS_BILINEAR,
                                           NULL,
                                           NULL,
                                           NULL);
    if (!state->scaled_sws_ctx) {
        LOGE("sws_getContext failed")
        return FAILURE;
    }
    return SUCCESS;
}

int stream_component_open(State *state, int stream_index) {
    AVFormatContext *fmt_ctx = state->fmt_ctx;
    AVCodecContext *codec_ctx;
    AVCodec *codec;
    if (stream_index < 0 || stream_index >= fmt_ctx->nb_streams) {
        return FAILURE;
    }

    codec_ctx = fmt_ctx->streams[stream_index]->codec;
    const AVCodecDescriptor *codesc = avcodec_descriptor_get(codec_ctx->codec_id);
    if (!codesc) {
        LOGE("avcodec_descriptor_get %s\n", codesc->name)
        return FAILURE;
    }
    codec = avcodec_find_decoder(codec_ctx->codec_id);
    if (!codec) {
        LOGE("avcodec_find_decoder %s\n", codesc->name)
        return FAILURE;
    }

    if (avcodec_open2(codec_ctx, codec, NULL) < 0) {
        LOGE("avcodec_open2 filed")
        return FAILURE;
    }

    switch (codec_ctx->codec_type) {
        case AVMEDIA_TYPE_AUDIO:
            state->audio_stream = stream_index;
            state->audio_st = fmt_ctx->streams[stream_index];
            break;
        case AVMEDIA_TYPE_VIDEO:
            state->video_stream = stream_index;
            state->video_st = fmt_ctx->streams[stream_index];

            AVCodec *target_codec = avcodec_find_encoder(TARGET_IMAGE_CODEC);
            if (!target_codec) {
                LOGE("avcodec_find_encoder failed")
                return FAILURE;
            }
            state->codec_ctx = avcodec_alloc_context3(target_codec);
            if (!state->codec_ctx) {
                LOGE("avcodec_alloc_context3 failed")
                return FAILURE;
            }

            state->codec_ctx->bit_rate = state->video_st->codecpar->bit_rate;
            state->codec_ctx->width = state->video_st->codecpar->width;
            state->codec_ctx->height = state->video_st->codecpar->height;
            state->codec_ctx->pix_fmt = TARGET_IMAGE_FORMAT;
            state->codec_ctx->codec_type = TARGET_IMAGE_CODEC;
            state->codec_ctx->time_base.num = state->video_st->codec->time_base.num;
            state->codec_ctx->time_base.den = state->video_st->codec->time_base.den;

            if (avcodec_open2(state->codec_ctx, target_codec, NULL) < 0) {
                LOGE("avcodec_open2 failed")
                return FAILURE;
            }

            state->sws_ctx = sws_getContext(state->video_st->codecpar->width,
                                            state->video_st->codecpar->height,
                                            state->video_st->codec->pix_fmt,
                                            state->video_st->codecpar->width,
                                            state->video_st->codecpar->height,
                                            TARGET_IMAGE_FORMAT,
                                            SWS_BILINEAR,
                                            NULL,
                                            NULL,
                                            NULL);
            if (!state->sws_ctx) {
                LOGE("sws_getContext failed")
                return FAILURE;
            }
            break;
        default:
            return FAILURE;
    }
    return SUCCESS;
}

int set_data_source_1(State **pState, const char *path) {
    int audio_index = -1;
    int video_index = -1;
    int i;
    State *state = *pState;
    LOGI("Path: %s\n", path)
    AVDictionary *options = NULL;
    av_dict_set(&options, "icy", "1", 0);   // 作用是什么？
    av_dict_set(&options, "user_agent", "FFmpegMediaMetadataRetriever", 0);
    if (state->headers) {
        av_dict_set(&options, "headers", state->headers, 0);
    }

    if (state->offset > 0) {
        state->fmt_ctx = avformat_alloc_context();
        state->fmt_ctx->skip_initial_bytes = state->offset;
    }

    if (avformat_open_input(&state->fmt_ctx, path, NULL, &options) != 0) {
        LOGE("Metadata could not be retrieved")
        return FAILURE;
    }

    if (avformat_find_stream_info(state->fmt_ctx, NULL) < 0) {
        LOGE("Metadata could not be retrieved")
        avformat_close_input(&state->fmt_ctx);
        return FAILURE;
    }

    set_duration(state->fmt_ctx);

    set_shoutcast_metadata(state->fmt_ctx);

    for (i = 0; i < state->fmt_ctx->nb_streams; i++) {
        if (state->fmt_ctx->streams[i]->codecpar->codec_type == AVMEDIA_TYPE_VIDEO &&
            video_index < 0) {
            video_index = i;
        }
        if (state->fmt_ctx->streams[i]->codecpar->codec_type == AVMEDIA_TYPE_AUDIO &&
            audio_index < 0) {
            audio_index = i;
        }

        set_codec(state->fmt_ctx, i);

    }

    if (audio_index >= 0) {
        stream_component_open(state, audio_index);
    }

    if (video_index >= 0) {
        stream_component_open(state, video_index);
    }

    set_rotation(state->fmt_ctx, state->audio_st, state->video_st);
    set_framerate(state->fmt_ctx, state->audio_st, state->video_st);
    set_filesize(state->fmt_ctx);
    set_chapter_count(state->fmt_ctx);
    set_video_dimensions(state->fmt_ctx, state->video_st);
    *pState = state;
    return SUCCESS;
}

void init(State **pState) {
    State *state = *pState;
    if (state && state->fmt_ctx) {
        avformat_close_input(&state->fmt_ctx);
    }
    if (state && state->fd != -1) {
        close(state->fd);
    }

    if (!state) {
        state = av_mallocz(sizeof(State));
    }

    state->fmt_ctx = NULL;
    state->audio_stream = -1;
    state->video_stream = -1;
    state->audio_st = NULL;
    state->video_st = NULL;
    state->fd = -1;
    state->offset = 0;
    state->headers = NULL;

    *pState = state;

}

int set_data_source_uri(State **pState, const char *path, const char *headers) {
    State *state = *pState;
    ANativeWindow *native_window = NULL;
    if (state && state->nativeWindow) {
        native_window = state->nativeWindow;
    }
    init(&state);
    state->nativeWindow = native_window;
    state->headers = headers;
    *pState = state;
    return set_data_source_1(pState, path);
}

int set_data_source_fd(State **pState, int fd, int64_t offset, int64_t length) {
    char path[256] = "";
    State *state = *pState;
    ANativeWindow *native_window = NULL;
    if (state && state->nativeWindow) {
        native_window = state->nativeWindow;
    }
    init(&state);
    state->nativeWindow = native_window;
    int myfd = dup(fd); // 复制文件描述符
    char str[20];
    sprintf(str, "pipe:%d", myfd);
    strcat(path, str);  // 连接字符串

    state->fd = myfd;
    state->offset = offset;
    *pState = state;

    return set_data_source_1(pState, path);
}

const char *extract_metadata(State **pState, const char *key) {
    char *value = NULL;
    State *state = *pState;
    if (!state || !state->fmt_ctx) {
        return value;
    }
    return extract_metadata_internal(state->fmt_ctx, state->audio_st, state->video_st, key);
}

const char *extract_metadata_from_chapter(State **pState, const char *key, int chapter) {
    char *value = NULL;
    State *state = *pState;
    if (!state || !state->fmt_ctx || state->fmt_ctx->nb_chapters <= 0) {
        return value;
    }
    if (chapter < 0 || chapter >= state->fmt_ctx->nb_chapters) {
        return value;
    }
    return extract_metadata_from_chapter_internal(state->fmt_ctx, state->audio_st, state->video_st,
                                                  key, chapter);
}

int get_metadata(State **pState, struct AVDictionary **metadata) {
    State *state = *pState;
    if (!state || !state->fmt_ctx) {
        return FAILURE;
    }
    return get_metadata_internal(state->fmt_ctx, metadata);
}

int get_embedded_picture(State **pState, AVPacket *pkt) {
    int i = 0;
    int got_packet = 0;
    AVFrame *frame = NULL;
    State *state = *pState;
    if (!state || !state->fmt_ctx) {
        return FAILURE;
    }
    // find the first attached picture, if available
    for (i = 0; i < state->fmt_ctx->nb_streams; i++) {
        if (state->fmt_ctx->streams[i]->disposition & AV_DISPOSITION_ATTACHED_PIC) {
            if (pkt) {
                av_packet_unref(pkt);
                av_init_packet(pkt);
            }
            av_copy_packet(pkt, &state->fmt_ctx->streams[i]->attached_pic);
            got_packet = 1;
            if (pkt->stream_index == state->video_stream) {
                int codec_id = state->video_st->codecpar->codec_id;
                int pix_fmt = state->video_st->codec->pix_fmt;

                // If the image isn't already in a supported format convert it to one
                if (!is_supported_format(codec_id, pix_fmt)) {
                    int got_frame = 0;

                    frame = av_frame_alloc();
                    if (!frame) {
                        break;
                    }

                    // todo 需要换成新的 API
                    if (avcodec_decode_video2(state->video_st->codec, frame, &got_frame, pkt) <=
                        0) {
                        break;
                    }

                    if (got_frame) {
                        AVPacket convertedPkt;
                        av_init_packet(&convertedPkt);
                        convertedPkt.size = 0;
                        convertedPkt.data = NULL;

                        convert_image(state, state->video_st->codec, frame, &convertedPkt,
                                      &got_packet, -1, -1);

                        av_packet_unref(pkt);
                        av_init_packet(pkt);
                        av_copy_packet(pkt, &convertedPkt);
                        av_packet_unref(&convertedPkt);
                        break;
                    }

                } else {
                    av_packet_unref(pkt);
                    av_init_packet(pkt);
                    av_copy_packet(pkt, &state->fmt_ctx->streams[i]->attached_pic);
                    got_packet = 1;
                    break;
                }

            }
        }
    }

    av_frame_free(&frame);
    if (got_packet) {
        return SUCCESS;
    } else {
        return FAILURE;
    }

}

void convert_image(State *state, AVCodecContext *pCodec_ctx, AVFrame *pFrame, AVPacket *avpkt,
                   int *got_packet, int width, int height) {
    AVCodecContext *codecCtx;
    struct SwsContext *scaleCtx;
    AVFrame *frame;

    *got_packet = 0;

    if (width != -1 && height != -1) {  // 需要缩放
        if (state->scaled_codec_ctx == NULL) {
            get_scaled_context(state, pCodec_ctx, width, height);
        }

        codecCtx = state->scaled_codec_ctx;
        scaleCtx = state->scaled_sws_ctx;
    } else {
        codecCtx = state->codec_ctx;
        scaleCtx = state->sws_ctx;
    }

    if (width == -1) {
        width = pCodec_ctx->width;
    }
    if (height == -1) {
        height = pCodec_ctx->height;
    }
    frame = av_frame_alloc();

    int numBytes = avpicture_get_size(TARGET_IMAGE_FORMAT, codecCtx->width, codecCtx->height);
    void *buffer = (uint8_t *) av_malloc(numBytes * sizeof(uint8_t));
    frame->format = TARGET_IMAGE_FORMAT;
    frame->width = codecCtx->width;
    frame->height = codecCtx->height;

    avpicture_fill((AVPicture *) frame, buffer, TARGET_IMAGE_FORMAT, codecCtx->width,
                   codecCtx->height);

    sws_scale(scaleCtx, (const uint8_t *const *) pFrame->data, pFrame->linesize, 0, pFrame->height,
              frame->data,
              frame->linesize);
    // todo 需要换成新的 API
    int ret = avcodec_encode_video2(codecCtx, avpkt, frame, got_packet);

    if (ret >= 0 && state->nativeWindow) {
        ANativeWindow_setBuffersGeometry(state->nativeWindow, width, height,
                                         WINDOW_FORMAT_RGBA_8888);
        ANativeWindow_Buffer windowBuffer;
        if (ANativeWindow_lock(state->nativeWindow, &windowBuffer, NULL) == 0) {
            for (int h = 0; h < height; ++h) {
                memcpy(windowBuffer.bits + h * windowBuffer.stride * 4,
                       buffer + h * frame->linesize[0],
                       width * 4);  // todo 没看懂
            }
            ANativeWindow_unlockAndPost(state->nativeWindow);
        }
    }

    if (ret < 0) {
        *got_packet = 0;
    }

    av_frame_free(&frame);

    if (buffer) {
        free(buffer);
    }

    if (ret < 0 || !*got_packet) {
        av_packet_unref(avpkt);
    }
}

void
decode_frame(State *state, AVPacket *pkt, int *got_frame, int64_t desired_frame_number, int width,
             int height) {
    AVFrame *frame = av_frame_alloc();
    *got_frame = 0;
    if (!frame) {
        return;
    }

    // Read frames and return first one found
    while (av_read_frame(state->fmt_ctx, pkt) >= 0) {
        if (pkt->stream_index == state->video_stream) {
            int codec_id = state->video_st->codecpar->codec_id;
            int pix_fmt = state->video_st->codec->pix_fmt;

            // If the image isn't already in a supported format convert it to one
            if (!is_supported_format(codec_id, pix_fmt)) {
                *got_frame = 0;
                if (avcodec_decode_video2(state->video_st->codec, frame, got_frame, pkt) <= 0) {
                    *got_frame = 0;
                    break;
                }

                if (*got_frame) {
                    if (desired_frame_number == -1 ||
                        (desired_frame_number != -1 && frame->pkt_pts >= desired_frame_number)) {
                        if (pkt->data) {
                            av_packet_unref(pkt);
                        }
                        av_init_packet(pkt);
                        convert_image(state, state->video_st->codec, frame, pkt, got_frame, width,
                                      height);
                        break;
                    }
                }

            } else {
                *got_frame = 1;
                break;
            }
        }
    }
    av_frame_free(&frame);
}

int get_frame_at_time(State **pState, int64_t timeUs, int option, AVPacket *pkt) {
    return get_scaled_frame_at_time(pState, timeUs, option, pkt, -1, -1);
}

int get_scaled_frame_at_time(State **pState, int64_t timeUs, int option, AVPacket *pkt, int width,
                             int height) {

    int got_packet = 0;
    int64_t desired_frame_number = -1;
    State *state = *pState;
    Options opt = option;

    if (!state || !state->fmt_ctx || state->video_stream < 0) {
        return FAILURE;
    }

    if (timeUs > -1) {
        int stream_index = state->video_stream;
        int64_t seek_time = av_rescale_q(timeUs, AV_TIME_BASE_Q,
                                         state->fmt_ctx->streams[stream_index]->time_base); // todo 什么意思
        int64_t seek_stream_duration = state->fmt_ctx->streams[stream_index]->duration;

        int flags = 0;
        int ret = -1;

        if (seek_stream_duration > 0 && seek_time > seek_stream_duration) {
            seek_time = seek_stream_duration;
        }

        if (seek_time < 0) {
            return FAILURE;
        }

        if (opt == OPTION_CLOSEST) {
            desired_frame_number = seek_time;
        } else if (opt == OPTION_CLOSEST_SYNC) {
            flags = 0;
        } else if (opt == OPTION_NEXT_SYNC) {
            flags = 0;
        } else if (opt == OPTION_PREVIOUS_SYNC) {
            flags = AVSEEK_FLAG_BACKWARD;
        }

        ret = av_seek_frame(state->fmt_ctx, stream_index, seek_time, flags);

        if (ret < 0) {
            return FAILURE;
        } else {
            if (state->audio_stream >= 0) {
                avcodec_flush_buffers(state->audio_st->codec);  // ?
            }
            if (state->video_stream >= 0) {
                avcodec_flush_buffers(state->video_st->codec);  // ?
            }
        }

    }

    decode_frame(state, pkt, &got_packet, desired_frame_number, width, height);

    if (got_packet) {
        return SUCCESS;
    } else {
        return FAILURE;
    }

}

int set_native_window(State **pState, ANativeWindow *native_window) {
    State *state = *pState;
    if (native_window == NULL) {
        return FAILURE;
    }
    if (!state) {
        init(&state);
    }
    state->nativeWindow = native_window;
    *pState = state;
    return SUCCESS;
}

void release(State **pState) {
    State *state = *pState;

    if (state) {
        if (state->audio_st && state->audio_st->codec) {
            avcodec_close(state->audio_st->codec);
        }

        if (state->video_st && state->video_st->codec) {
            avcodec_close(state->video_st->codec);
        }

        if (state->fmt_ctx) {
            avformat_close_input(&state->fmt_ctx);
        }

        if (state->fd != -1) {
            close(state->fd);
        }

        if (state->sws_ctx) {
            sws_freeContext(state->sws_ctx);
            state->sws_ctx = NULL;
        }

        if (state->codec_ctx) {
            avcodec_close(state->codec_ctx);
            av_free(state->codec_ctx);
        }

        if (state->sws_ctx) {
            sws_freeContext(state->sws_ctx);
        }

        if (state->scaled_codec_ctx) {
            avcodec_close(state->scaled_codec_ctx);
            av_free(state->scaled_codec_ctx);
        }

        if (state->scaled_sws_ctx) {
            sws_freeContext(state->scaled_sws_ctx);
        }

        // make sure we don't leak native windows
        if (state->nativeWindow != NULL) {
            ANativeWindow_release(state->nativeWindow);
            state->nativeWindow = NULL;
        }

        av_freep(&state);
        pState = NULL;
    }
}