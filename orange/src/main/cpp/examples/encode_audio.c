// audio encoding with libavcodec API example.
// Created by juhezi on 20-2-27.
//

#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>

#include <libavcodec/avcodec.h>
#include <libavutil/common.h>
#include <libavutil/frame.h>
#include <libavutil/samplefmt.h>

// check that a given sample format is supported by the encoder
static int check_sample_fmt(const AVCodec *codec, enum AVSampleFormat sample_fmt) {
    const enum AVSampleFormat *p = codec->sample_fmts;
    while (*p != AV_SAMPLE_FMT_NONE) {
        if (*p == sample_fmt) {
            return 1;
        }
        p++;
    }
    return 0;
}

// just pick the highest supported sample rate
static int select_sample_rate(const AVCodec *codec) {
    const int *p;
    int best_samplerate = 0;
    if (!codec->supported_samplerates) {
        return 44100;
    }
    p = codec->supported_samplerates;
    // 看哪个更接近 44100
    while (*p) {
        if (!best_samplerate || abs(44100 - *p) < abs(44100 - best_samplerate)) {
            best_samplerate = *p;
        }
        p++;
    }
    return best_samplerate;
}

// select layout with the highest channel count
static int select_channel_layout(const AVCodec *codec) {
    const uint64_t *p;
    uint64_t best_ch_layout = 0;
    int best_nb_channels;
    if (!codec->channel_layouts) {
        return AV_CH_LAYOUT_STEREO;
    }
    p = codec->channel_layouts;
    while (*p) {
        int nb_channels = av_get_channel_layout_nb_channels(*p);
        if (nb_channels > best_nb_channels) {
            best_ch_layout = *p;
            best_nb_channels = nb_channels;
        }
        p++;
    }
    return best_ch_layout;
}

static void encode(AVCodecContext *ctx, AVFrame *frame, AVPacket *pkt, FILE *output) {
    int ret;
    ret = avcodec_send_frame(ctx, frame);
    if (ret < 0) {
        exit(1);
    }
    while (ret >= 0) {
        ret = avcodec_receive_packet(ctx, pkt);
        if (ret == AVERROR(EAGAIN) || ret == AVERROR_EOF) {
            return;
        } else if (ret < 0) {
            exit(1);
        }
        // size - 被写入的每个元素的大小
        fwrite(pkt->data, 1, pkt->size, output);
        av_packet_unref(pkt);   // ?
    }
}

static int encode_audio() {
    const char *filename;
    const AVCodec *codec;
    AVCodecContext *c = NULL;
    AVFrame *frame;
    AVPacket *pkt;
    int i, j, k, ret;
    FILE *f;
    uint16_t *samples;
    float t, tincr;

    filename = "";

    codec = avcodec_find_encoder(AV_CODEC_ID_MP2);
    c = avcodec_alloc_context3(codec);
    c->bit_rate = 64000;
    c->sample_fmt = AV_SAMPLE_FMT_S16;
    // check that the encoder supports s16 pcm input
    if (!check_sample_fmt(codec, c->sample_fmt)) {
        exit(1);
    }
    c->sample_rate = select_sample_rate(codec);
    c->channel_layout = select_channel_layout(codec);
    c->channels = av_get_channel_layout_nb_channels(c->channel_layout);

    avcodec_open2(c, codec, NULL);
    f = fopen(filename, "wb");
    // packet for holding encoded output
    pkt = av_packet_alloc();
    // frame containing input raw audio
    // 此函数只分配 AVFrame 对象本身，不分配 AVFrame 中的数据缓冲区
    frame = av_frame_alloc();
    frame->nb_samples = c->frame_size;
    frame->format = c->sample_fmt;
    frame->channel_layout = c->channel_layout;
    // 分配 AVFrame 的缓冲区
    // 这个方法分配到的数据结构是可复用的，即内部有引用计数，本次对 frame data 使用完成，可以解除引用
    // 调用 av_frame_unref 后，引用计数减 1，如果引用计数变为 0，则释放 data 空间、、
    ret = av_frame_get_buffer(frame, 0);
    t = 0;
    tincr = 2 * M_PI * 440.0 / c->sample_rate;
    for (i = 0; i < 200; ++i) {
        // make sure the frame is writable -- makes a copy if encoder kept（保持） a reference internally
        ret = av_frame_make_writable(frame);
        samples = frame->data[0];

        for (j = 0; j < c->frame_size; ++j) {
            samples[2 * j] = sin(t) * 10000;
            for (k = 1; k < c->channels; k++) {
                samples[2 * j + k] = samples[2 * j];
            }
            t += tincr;
        }
        encode(c, frame, pkt, f);
    }
    encode(c, NULL, pkt, f);
    fclose(f);

    av_frame_free(&frame);
    av_packet_free(&pkt);
    avcodec_free_context(&c);

    return 0;
}