// Demuxing and decoding example.
// Show how to use the libavformat and libavcodec API to demux and
// decode audio and video data.
// Created by juhezi on 20-2-25.
// 解复用和解码

#include <libavutil/imgutils.h>
#include <libavutil/samplefmt.h>
#include <libavutil/timestamp.h>
#include <libavformat/avformat.h>

static AVFormatContext *fmt_ctx = NULL;
static AVCodecContext *video_dec_ctx = NULL, *audio_dec_ctx;
static int width, height;
static enum AVPixelFormat pix_fmt;
static AVStream *video_stream = NULL, *audio_stream = NULL;
static const char *src_filename = NULL;
static const char *video_dst_filename = NULL;
static const char *audio_dst_filename = NULL;

static FILE *video_dst_file = NULL;
static FILE *audio_dst_file = NULL;

static uint8_t *video_dst_data[4] = {NULL};
static int video_dst_linesize[4];
static int video_dst_bufsize;

static int video_stream_idx = -1, audio_stream_idx = -1;
static AVFrame *frame = NULL;
static AVPacket pkt;
static int video_frame_count = 0;
static int audio_frame_count = 0;

/* Enable or disable frame reference counting. You are not supposed to support
 * both paths in your application but pick the one most appropriate to your
 * needs. Look for the use of refcount in this example to see what are the
 * differences of API usage between them.
 * */
static int refcount = 0;

static int decode_packet(int *got_frame, int cached) {
    int ret = 0;
    int decoded = pkt.size;

    *got_frame = 0;
    if (pkt.stream_index == video_stream_idx) {
        // decode video stream
        // 应该使用 avcodec_send_packet 和 avcodec_receive_frame
        // todo 全部写完以后进行改造
        ret = avcodec_decode_video2(video_dec_ctx, frame, got_frame, &pkt);
        if (*got_frame) {
            if (frame->width != width || frame->height != height || frame->format != pix_fmt) {
                /* To handle this change, one could call av_image_alloc again and
                 * decode the following frames into another rawvideo file. */
                return -1;
            }
            /* copy decoded frame to destination buffer:
            * this is required since rawvideo expects non aligned data */
            av_image_copy(video_dst_data, video_dst_linesize,
                          (const uint8_t **) frame->data, frame->linesize,
                          pix_fmt, width, height);
            fwrite(video_dst_data[0], 1, video_dst_bufsize, video_dst_file);
        }

    } else if (pkt.stream_index == audio_stream_idx) {
        // todo 改成新版的
        ret = avcodec_decode_audio4(audio_dec_ctx, frame, got_frame, &pkt);
        /* Some audio decoders decode only part of the packet, and have to be
         * called again with the remainder of the packet data.
         * Sample: fate-suite/lossless-audio/luckynight-partial.shn
         * Also, some decoders might over-read the packet. */
        decoded = FFMIN(ret, pkt.size);
        if (*got_frame) {
            size_t unpadded_linesize = frame->nb_samples * av_get_bytes_per_sample(frame->format);
            /* Write the raw audio data samples of the first plane. This works
            * fine for packed formats (e.g. AV_SAMPLE_FMT_S16). However,
            * most audio decoders output planar audio, which uses a separate
            * plane of audio samples for each channel (e.g. AV_SAMPLE_FMT_S16P).
            * In other words, this code will write only the first audio channel
            * in these cases.
            * You should use libswresample or libavfilter to convert the frame
            * to packed data.
            */
            fwrite(frame->extended_data[0], 1, unpadded_linesize, audio_dst_file);
        }
    }
    /* If we use frame reference counting, we own the data and need
     * to de-reference it when we don't use it anymore */
    if (*got_frame && refcount) {
        av_frame_unref(frame);
    }

    return decoded;

}

// done
static int open_codec_context(int *stream_idx, AVCodecContext **dec_ctx, AVFormatContext *fmt_ctx,
                              enum AVMediaType type) {
    int ret, stream_index;
    AVStream *st;
    AVCodec *dec = NULL;
    AVDictionary *opts = NULL;  // 键值对

    ret = av_find_best_stream(fmt_ctx, type, -1, -1, NULL, 0);
    stream_index = ret;
    st = fmt_ctx->streams[stream_index];

    dec = avcodec_find_decoder(st->codecpar->codec_id);
    *dec_ctx = avcodec_alloc_context3(dec);
    /* Copy codec parameters from input stream to output codec context */
    if ((ret = avcodec_parameters_to_context(*dec_ctx, st->codecpar)) < 0) {
        return ret;
    }
    /* Init the decoders, with or without reference counting */
    // todo 什么意思，没看懂
    av_dict_set(&opts, "refcounted_frames", refcount ? "1" : "0", 0);
    avcodec_open2(*dec_ctx, dec, &opts);
    *stream_idx = stream_index;
    return 0;
}

static int get_format_from_sample_fmt(const char **fmt,
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

int demuxing_decoding() {
    int ret = 0, got_frame;
    src_filename = "input";
    video_dst_filename = "video_output";
    audio_dst_filename = "audio_output";
    /* open input file, and allocate format context */
    avformat_open_input(&fmt_ctx, src_filename, NULL, NULL);
    /* retrieve stream information */
    avformat_find_stream_info(fmt_ctx, NULL);
    if (open_codec_context(&video_stream_idx, &video_dec_ctx, fmt_ctx, AVMEDIA_TYPE_VIDEO) >= 0) {
        video_stream = fmt_ctx->streams[video_stream_idx];

        video_dst_file = fopen(video_dst_filename, "wb");

        width = video_dec_ctx->width;
        height = video_dec_ctx->height;
        pix_fmt = video_dec_ctx->pix_fmt;
        ret = av_image_alloc(video_dst_data, video_dst_linesize, width, height, pix_fmt, 1);
        video_dst_bufsize = ret;
    }

    if (open_codec_context(&audio_stream_idx, &audio_dec_ctx, fmt_ctx, AVMEDIA_TYPE_AUDIO) >= 0) {
        audio_stream = fmt_ctx->streams[audio_stream_idx];
        audio_dst_file = fopen(audio_dst_filename, "wb");
    }
    frame = av_frame_alloc();
    /* initialize packet, set data to NULL, let the demuxer fill it */
    av_init_packet(&pkt);
    pkt.data = NULL;
    pkt.size = 0;
    /* read frames from the file */
    // 读取码流中音频若干帧或者视频一帧。
    // 例如，解码视频的时候，每解码一个视频帧，需要先调用 av_read_frame()获得一帧视频的压缩数据，
    // 然后才能对该数据进行解码（例如H.264中一帧压缩数据通常对应一个NAL）
    while (av_read_frame(fmt_ctx, &pkt) >= 0) {
        AVPacket orig_pkt = pkt;
        do {
            ret = decode_packet(&got_frame, 0);
            if (ret < 0)
                break;
            pkt.data += ret;
            pkt.size -= ret;
        } while (pkt.size > 0);
        av_packet_unref(&orig_pkt);
    }
    // flush cached frames
    pkt.data = NULL;
    pkt.size = 0;
    do {
        decode_packet(&got_frame, 1);
    } while (got_frame);

    if (audio_stream) {
        enum AVSampleFormat sfmt = audio_dec_ctx->sample_fmt;
        int n_channels = audio_dec_ctx->channels;
        const char *fmt;
        if (av_sample_fmt_is_planar(sfmt)) {
            const char *packed = av_get_sample_fmt_name(sfmt);
            printf("Warning: the sample format the decoder produced is planar "
                   "(%s). This example will output the first channel only.\n",
                   packed ? packed : "?");
            sfmt = av_get_packed_sample_fmt(sfmt);
            n_channels = 1;
        }
        if ((ret = get_format_from_sample_fmt(&fmt, sfmt)) < 0)
            printf("Play the output audio file with the command:\n"
                   "ffplay -f %s -ac %d -ar %d %s\n",
                   fmt, n_channels, audio_dec_ctx->sample_rate,
                   audio_dst_filename);
    }
    return 0;
}

