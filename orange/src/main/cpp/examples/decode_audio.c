//
// Created by yunrui on 2020-02-21.
//

#include <stdio.h>
#include <libavutil/frame.h>
#include <libavcodec/avcodec.h>

#define AUDIO_INBUF_SIZE 20480
#define AUDIO_REFILL_THRESH 4096

static int get_format_sample_fmt(const char **fmt,
                                 enum AVSampleFormat sample_fmt) {
    int i;
    struct sample_fmt_entry {
        enum AVSampleFormat sample_fmt;
        const char *fmt_be, *fmt_le;
    } sample_fmt_entries[] = {
            {AV_SAMPLE_FMT_U8,  "u8",    "u8"},
            {AV_SAMPLE_FMT_S16, "s16be", "s16le"},
            {AV_SAMPLE_FMT_S32, "s32be", "s32le"},
            {AV_SAMPLE_FMT_FLT, "f32be", "f32le"},
            {AV_SAMPLE_FMT_DBL, "f64be", "f64le"},
    };
    *fmt = NULL;
    for (i = 0; i < FF_ARRAY_ELEMS(sample_fmt_entries); i++) {
        struct sample_fmt_entry *entry = &sample_fmt_entries[i];
        if (sample_fmt == entry->sample_fmt) {
            *fmt = AV_NE(entry->fmt_be, entry->fmt_le);
            return 0;
        }
    }
    fprintf(stderr,
            "sample format %s is not supported as output format\n",
            av_get_sample_fmt_name(sample_fmt));
    return -1;
}

static void decode(AVCodecContext *dec_ctx,
                   AVPacket *pkt,
                   AVFrame *frame,
                   FILE *outfile) {
    int i, ch;
    int ret, data_size;
    // 向解码器发送数据
    ret = avcodec_send_packet(dec_ctx, pkt);

    // 读取所有解码后的帧（一个 packet 对应于多个 frame）
    while (ret >= 0) {
        ret = avcodec_receive_frame(dec_ctx, frame);
        if (ret == AVERROR(EAGAIN) || ret == AVERROR_EOF)
            // 没有 Frame 了
            return;
        else if (ret < 0) {
            // 解码错误
            exit(1);
        }
        // 每次采样多少字节
        data_size = av_get_bytes_per_sample(dec_ctx->sample_fmt);
        // 对每个采样进行循环
        for (i = 0; i < frame->nb_samples; i++) {
            for (ch = 0; ch < dec_ctx->channels; ch++) {
                // 把 data 中的数据读取到 outfile 中
                fwrite(frame->data[ch] + data_size * i,
                       1,
                       data_size,
                       outfile);
            }
        }
    }
}

int start() {
    const char *outfilename, *filename;
    const AVCodec *codec;
    AVCodecContext *c = NULL;
    AVCodecParserContext *parser = NULL;    // 用于解封装，解封装后的数据放到 AVPacket 中
    int len, ret;
    FILE *f, *outfile;
    uint8_t inbuf[AUDIO_INBUF_SIZE + AV_INPUT_BUFFER_MIN_SIZE];
    uint8_t *data;
    size_t data_size;
    AVPacket *pkt;
    AVFrame *decoded_frame = NULL;
    enum AVSampleFormat sfmt;
    int n_channels = 0;
    const char *fmt;
    filename = "input.mp3";
    outfilename = "output.pcm";

    pkt = av_packet_alloc();
    codec = avcodec_find_decoder(AV_CODEC_ID_MP2);  // 直接指定编码器
    parser = av_parser_init(codec->id);
    c = avcodec_alloc_context3(codec);
    avcodec_open2(c, codec, NULL);
    f = fopen(filename, "rb");
    outfile = fopen(outfilename, "wb");

    // decode util eof
    data = inbuf;
    // 从文件中读取内容
    data_size = fread(inbuf, 1, AUDIO_INBUF_SIZE, f);
    while (data_size > 0) {
        if (!decoded_frame) {
            if (!(decoded_frame = av_frame_alloc())) {
                exit(1);
            }
        }
        ret = av_parser_parse2(parser,
                               c,
                               &pkt->data,
                               &pkt->size,
                               data,
                               data_size,
                               AV_NOPTS_VALUE,
                               AV_NOPTS_VALUE,
                               0);

        data += ret;
        data_size -= ret;

        if (pkt->size) {
            decode(c, pkt, decoded_frame, outfile);
        }
        // 避免解码不完整的帧。 具体还不是很清楚，需要测试一下
        /* Refill the input buffer, to avoid trying to decode
            * incomplete frames. Instead of this, one could also use
            * a parser, or use a proper container format through
        */
        if (data_size < AUDIO_REFILL_THRESH) {
            memmove(inbuf, data, data_size);
            data = inbuf;
            len = fread(data + data_size,
                        1,
                        AUDIO_INBUF_SIZE - data_size,
                        f);
            if (len > 0) {
                data_size += len;
            }
        }
    }
    // 某些 decoder 在 input 和 ouput 有 delay ，这表示一些 packet 并不是
    // 立即经由 decoder 解码输出，而需要 decode 结束时 flush，从而获得所有的
    // 解码数据。对于没有 delay 的 decoder，flush 也是安全的。
    // flush 是通过调用该函数，并将 pkt->data = NULL，pkt->size = 0.
    // flush the decoder
    pkt->data = NULL;
    pkt->size = 0;
    decode(c, pkt, decoded_frame, outfile);

    // 下面就是获取 pcm 相关数据了，因为 pcm 没有 metadata
    sfmt = c->sample_fmt;
    if (av_sample_fmt_is_planar(sfmt)) {
        const char *packed = av_get_sample_fmt_name(sfmt);
        sfmt = av_get_packed_sample_fmt(sfmt);
    }
    n_channels = c->channels;   // 声道数
    if ((ret = get_format_sample_fmt(&fmt, sfmt)) < 0)
        goto end;


    end:
    fclose(outfile);
    fclose(f);
    avcodec_free_context(&c);
    av_parser_close(parser);
    av_frame_free(&decoded_frame);
    av_packet_free(&pkt);
}

// 注意：音频中一个 AVFrame 可能包含多个音频帧，而视频一个 AVFrame 对应一帧图像
