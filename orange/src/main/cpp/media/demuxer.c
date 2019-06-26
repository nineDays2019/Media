//
// Created by juhezi on 19-6-25.
//

#include "../common/common_tools.h"
#include <stdio.h>
#include "libavformat/avformat.h"

int demo() {
    AVOutputFormat *ofmt_a = NULL, *ofmt_v = NULL;
    AVFormatContext *ifmt_ctx = NULL, *ofmt_ctx_a = NULL, *ofmt_ctx_v = NULL;
    AVPacket pkt;
    int ret, i;
    int videoindex = -1, audioindex = -1;
    int frame_index = 0;

    const char *in_filename = "source.mp4";
    const char *out_filename_v = "output.h264";
    const char *out_filename_a = "output.aac";

    av_register_all();

    // Input
    if ((ret = avformat_open_input(&ifmt_ctx, in_filename, 0, 0)) < 0) {
        LOGE("Could not open input file.");
        goto end;
    }
    if ((ret = avformat_find_stream_info(ifmt_ctx, 0)) < 0) {
        LOGE("Failed to retrieve input stream information.");
        goto end;
    }

    // output
    avformat_alloc_output_context2(&ofmt_ctx_v, NULL, NULL, out_filename_v);
    if (!ofmt_ctx_v) {
        LOGE("Could not create output context");
        ret = AVERROR_UNKNOWN;
        goto end;
    }
    ofmt_v = ofmt_ctx_v->oformat;

    avformat_alloc_output_context2(&ofmt_ctx_a, NULL, NULL, out_filename_a);
    if (!ofmt_ctx_a) {
        LOGE("Could not create output context");
        ret = AVERROR_UNKNOWN;
        goto end;
    }
    ofmt_a = ofmt_ctx_a->oformat;

    /**
     * 对所有帧进行循环
     * nb_streams 帧数，命令行也用到了这个
     */
    for (i = 0; i < ifmt_ctx->nb_streams; i++) {
        // Create output  AvStream according to input AVStream
        AVFormatContext *ofmt_ctx;
        AVStream *in_stream = ifmt_ctx->streams[i];
        AVStream *out_stream = NULL;

        if (ifmt_ctx->streams[i]->codec->codec_type == AVMEDIA_TYPE_VIDEO) {
            videoindex = i;
            out_stream = avformat_new_stream(ofmt_ctx_v, in_stream->codec->codec);
            ofmt_ctx = ofmt_ctx_v;
        } else if (ifmt_ctx->streams[i]->codec->codec_type == AVMEDIA_TYPE_AUDIO) {
            audioindex = i;
            out_stream = avformat_new_stream(ofmt_ctx_a, in_stream->codec->codec);
            ofmt_ctx = ofmt_ctx_a;
        } else {
            break;
        }
        if (!out_stream) {
            LOGE("Failed allocating output stream.");
            ret = AVERROR_UNKNOWN;
            goto end;
        }
        // Copy the settings of AVCodecContext
        if (avcodec_copy_context(out_stream->codec, in_stream->codec) < 0) {
            LOGE("Failed to copy context from input to output stream codec context.");
            goto end;
        }
        out_stream->codec->codec_tag = 0;
        if (ofmt_ctx->oformat->flags & AVFMT_GLOBALHEADER) {
            out_stream->codec->flags |= AV_CODEC_FLAG_GLOBAL_HEADER;
        }
    }

    // 这个方法的作用是什么
    av_dump_format(ifmt_ctx, 0, in_filename, 0);

    av_dump_format(ofmt_ctx_v, 0, out_filename_v, 1);

    av_dump_format(ofmt_ctx_a, 0, out_filename_a, 1);

    if (!(ofmt_v->flags & AVFMT_NOFILE)) {
        if (avio_open(&ofmt_ctx_v->pb, out_filename_v, AVIO_FLAG_WRITE) < 0) {
            LOGE("Could not open output file '%s'", out_filename_v);
            goto end;
        }
    }

    if (!(ofmt_a->flags & AVFMT_NOFILE)) {
        if (avio_open(&ofmt_ctx_a->pb, out_filename_a, AVIO_FLAG_WRITE) < 0) {
            LOGE("Could not open output file '%s'", out_filename_a);
            goto end;
        }
    }

    // Write file Header
    if (avformat_write_header(ofmt_ctx_v, NULL) < 0) {
        LOGE("Error occurred when opening video output file.");
        goto end;
    }

    if (avformat_write_header(ofmt_ctx_a, NULL) < 0) {
        LOGE("Error occurred when opening audio output file.");
        goto end;
    }

    while (1) {
        AVFormatContext *ofmt_ctx;
        AVStream *in_stream, *out_stream;
        // Get an AVPacket
        if (av_read_frame(ifmt_ctx, &pkt) < 0) {
            break;
        }
        in_stream = ifmt_ctx->streams[pkt.stream_index];

        if (pkt.stream_index == videoindex) {
            out_stream = ofmt_ctx_v->streams[0];
            ofmt_ctx = ofmt_ctx_v;
            LOGI("Write Video Packet. size: %d\tpts: %lld\n", pkt.size, pkt.pts);
        } else if (pkt.stream_index == audioindex) {
            out_stream = ofmt_ctx_a->streams[0];
            ofmt_ctx = ofmt_ctx_a;
            LOGI("Write Audio Packet. size: %d\tpts: %lld\n", pkt.size, pkt.pts);
        } else {
            continue;
        }

        // Convert PTS/DTS

        pkt.pts = av_rescale_q_rnd(pkt.pts, in_stream->time_base, out_stream->time_base,
                                   AV_ROUND_NEAR_INF | AV_ROUND_PASS_MINMAX);
        pkt.dts = av_rescale_q_rnd(pkt.dts, in_stream->time_base, out_stream->time_base,
                                   AV_ROUND_NEAR_INF | AV_ROUND_PASS_MINMAX);
        pkt.duration = av_rescale_q(pkt.duration, in_stream->time_base,
                                    out_stream->time_base);
        pkt.pos = -1;
        pkt.stream_index = 0;
        // Write
        if (av_interleaved_write_frame(ofmt_ctx, &pkt) < 0) {
            LOGE("Error muxing packet.");
            break;
        }
        av_free_packet(&pkt);
        frame_index++;
        // Write file trailer
        av_write_trailer(ofmt_ctx_v);
        av_write_trailer(ofmt_ctx_a);

        end:
        avformat_close_input(&ifmt_ctx);
        // close output
        if (ofmt_ctx_v && !(ofmt_ctx_v->flags & AVFMT_NOFILE)) {
            avio_close(ofmt_ctx_v->pb);
        }

        if (ofmt_ctx_a && !(ofmt_ctx_a->flags & AVFMT_NOFILE)) {
            avio_close(ofmt_ctx_a->pb);
        }

        avformat_free_context(ofmt_ctx_v);
        avformat_free_context(ofmt_ctx_a);

        if (ret < 0 && ret != AVERROR_EOF) {
            LOGE("Error occurred.");
            return -1;
        }
        return 0;
    }

}




