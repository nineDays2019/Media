// video encoding with libavcodec API example
// Created by juhezi on 20-2-27.
//

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include <libavcodec/avcodec.h>
#include <libavutil/opt.h>
#include <libavutil/imgutils.h>

static void encode(AVCodecContext *enc_ctx, AVFrame *frame, AVPacket *pkt, FILE *outfile) {
    int ret;
    if (frame)
        printf("Send frame %3" PRId64 "\n", frame->pts);
    ret = avcodec_send_frame(enc_ctx, frame);
    while (ret >= 0) {
        ret = avcodec_receive_packet(enc_ctx, pkt);
        if (ret == AVERROR(EAGAIN) || ret == AVERROR_EOF) {
            return;
        } else if (ret < 0) {
            exit(1);
        }
        fwrite(pkt->data, 1, pkt->size, outfile);
        av_packet_unref(pkt);
    }
}

static int encode_video() {
    const char *filename, *codec_name;
    const AVCodec *codec;
    AVCodecContext *c = NULL;
    int i, ret, x, y;
    FILE *f;
    AVFrame *frame;
    AVPacket *pkt;
    uint8_t endcode[] = {0, 0, 1, 0xb7};
    filename = "12138";
    codec_name = "12138";

    codec = avcodec_find_decoder_by_name(codec_name);
    c = avcodec_alloc_context3(codec);
    pkt = av_packet_alloc();
    c->bit_rate = 400000;
    c->width = 352;
    c->height = 288;
    /* frames per second */
    c->time_base = (AVRational) {1, 25};
    c->framerate = (AVRational) {25, 1};
    /* emit one intra frame every ten frames
     * check frame pict_type before passing frame
     * to encoder, if frame->pict_type is AV_PICTURE_TYPE_I
     * then gop_size is ignored and the output of encoder
     * will always be I frame irrespective to gop_size
     * 每十帧发射一帧
     * 在传递帧之前检查帧pict_type
     * 编码器，如果frame-> pict_type 为 AV_PICTURE_TYPE_I
     * 然后gop_size被忽略，编码器的输出
     * 与 gop_size无关，始终为I帧
     */
    c->gop_size = 10;
    c->max_b_frames = 1;
    c->pix_fmt = AV_PIX_FMT_YUV420P;

    if (codec->id == AV_CODEC_ID_H264) {
        av_opt_set(c->priv_data, "preset", "slow", 0);
    }
    ret = avcodec_open2(c, codec, NULL);
    f = fopen(filename, "wb");
    frame = av_frame_alloc();
    frame->format = c->pix_fmt;
    frame->width = c->width;
    frame->height = c->height;

    ret = av_frame_get_buffer(frame, 32);

    // encode 1 second of video
    for (i = 0; i < 25; ++i) {
        fflush(stdout);
        ret = av_frame_make_writable(frame);
        // prepare a dummy image
        // Y
        for (y = 0; y < c->height; y++) {
            for (x = 0; x < c->width; x++) {
                frame->data[0][y * frame->linesize[0] + x] = x + y + i * 3;
            }
        }
        for (y = 0; y < c->height / 2; y++) {
            for (x = 0; x < c->width / 2; x++) {
                frame->data[1][y * frame->linesize[1] + x] = 128 + y + i * 2;
                frame->data[2][y * frame->linesize[2] + x] = 64 + x + i * 5;
            }
        }
        frame->pts = i;
        encode(c, frame, pkt, f);
    }
    encode(c, NULL, pkt, f);
    fclose(f);
    avcodec_free_context(&c);
    av_frame_free(&frame);
    av_packet_free(&pkt);
    return 0;
}