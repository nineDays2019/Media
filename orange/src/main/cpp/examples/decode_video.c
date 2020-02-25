// Video decoding with libavcodec API example
// Created by juhezi on 20-2-23.
//

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include <libavcodec/avcodec.h>

#define INBUF_SIZE 4096

/**
 * save a simple pgm image file
 * @param buf
 * @param wrap
 * @param xsize
 * @param ysize
 * @param filename
 */
static void pgm_save(unsigned char *buf, int wrap, int xsize, int ysize, char *filename) {
    FILE *f;

    f = fopen(filename, "w");

    // 写入 PGM 的头部
    fprintf(f, "P5\n%d %d\n %d\n", xsize, ysize, 255);
    for (int i = 0; i < ysize; ++i) {
        fwrite(buf + i * wrap, 1, xsize, f);
    }
    fclose(f);
}

static void decode(AVCodecContext *dec_ctx, AVFrame *frame, AVPacket *pkt, const char *filename) {
    char filenameBuf[1024];
    /* 传递未解码之前的 packet 数据 给解码器 ctx， pkt 作为输入参数，通常 pkt 是一张 video frame 或者 多个完成的音频帧，解码器不会向pkt中写入数据，
     * 不像之前老的API 会对pkt进行操作，并且如果pkt包含多个音频帧，可能需要你多次调用 avcodec_receive_frame().
     * 可以传递一个空的 pkt（eg：data为null，size 为0，pkt 为null）
     * 这意味着是一个刷新作用的packet，decoder 收到后，就会返回缓存中的frames。
     */
    int ret;
    ret = avcodec_send_packet(dec_ctx, pkt);
    while (ret >= 0) {
        ret = avcodec_receive_frame(dec_ctx, frame);
        if (ret == AVERROR(EAGAIN) || ret == AVERROR_EOF) {
            return;
        } else if (ret < 0) {
            // 出错
            exit(1);
        }
        fflush(stdout);
        /**
         * int snprintf(char *str, size_t size, const char *format, ...)
         * snprintf()：将可变参数按照 format 的格式格式化为字符串。然后再将其拷贝至 str 中。
         * the picture is allocated by the decoder. no need to free it.
         */
        snprintf(filenameBuf, sizeof(filenameBuf), "%s-%d", filename, dec_ctx->frame_number);
        // 把这一帧的数据保存在一张图片中
        pgm_save(frame->data[0], frame->linesize[0], frame->width, frame->height, filenameBuf);
    }
}

int decode_video() {
    const char *filename, *outfilename;
    const AVCodec *codec;
    AVCodecParserContext *parser;
    AVCodecContext *c = NULL;
    FILE *f;
    AVFrame *frame;
    /*
    * AV_INPUT_BUFFER_PADDING_SIZE： 在申请内存时，额外的增加一个size，
    * 原因：在做解码时，一些优化过的解码器往往一次性解析 32bit 或 64bit 的数据，避免读取数据越界或丢失的问题、
    * 注意：如果前23bit 不为0， 可能会导致读取越界，或者段错误。
    */
    uint8_t inbuf[INBUF_SIZE + AV_INPUT_BUFFER_PADDING_SIZE];
    uint8_t *data;
    size_t data_size;
    int ret;
    AVPacket *pkt;

    filename = "input.mp4";
    outfilename = "output";

    pkt = av_packet_alloc();
    if (!pkt) {
        exit(1);
    }
    // set end of buffer to 0 (this ensures that no overreading happens for damaged MPEG streams)
    memset(inbuf + INBUF_SIZE, 0, AV_INPUT_BUFFER_PADDING_SIZE);

    codec = avcodec_find_decoder(AV_CODEC_ID_MPEG1VIDEO);

    parser = av_parser_init(codec->id);

    c = avcodec_alloc_context3(codec);

    /* For some codecs, such as msmpeg4 and mpeg4, width and height
       MUST be initialized there because this information is not
       available in the bitstream. */

    avcodec_open2(c, codec, NULL);
    f = fopen(filename, "rb");

    // 使用 feof 检测文件是否达到了文件尾部
    while (!feof(f)) {
        // read raw data from the input file
        data_size = fread(inbuf, 1, INBUF_SIZE, f);
        if (!data_size) {
            break;
        }
        // use the parser to split the data into frames
        data = inbuf;
        while (data_size > 0) {
            /* 解析得到一个有效的AVPacket ，也就是给pkt 塞数据，
             * pkt->size 会设置成正确的大小，pkt->data 也会指向正确的buff ，返回值为 使用了 输入buff 多个个字节的数据 */
            ret = av_parser_parse2(parser, c, &pkt->data, &pkt->size,
                                   data, data_size, AV_NOPTS_VALUE, AV_NOPTS_VALUE, 0);
            data += ret;
            data_size -= ret;

            if (pkt->size) {
                decode(c, frame, pkt, outfilename);
            }

        }
    }

    /* flush the decoder */
    decode(c, frame, NULL, outfilename);
    fclose(f);
    av_parser_close(parser);
    avcodec_free_context(&c);
    av_frame_free(&frame);
    av_packet_free(&pkt);

    return 0;
}

// av_parser_parse2() 拿到 AVPacket 数据，将一个个 AVPacket 数据解析组成完整的一帧未解码的压缩数据
// 和 av_read_frame 类似
// 输入必须是只包含视频编码数据“裸流”（例如 H264、HEVC 码流文件），而不能是包含封装格式的媒体数据（例如 AVI、MKV、MP4）
// av_parse_init()： 初始化 AVCodecParserContext， 参数是 codec_id，所以同时只能解析一种
// AVCodecParser用于解析输入的数据流并把它们分成一帧一帧的压缩编码数据。
// 核心函数是av_parser_parse2()：
// av_parser_parse2()：解析数据获得一个Packet， 从输入的数据流中分离出一帧一帧的压缩编码数据。

/*
 * Parse a packet.
 *
 * @param s             parser context.
 * @param avctx         codec context.
 * @param poutbuf       set to pointer to parsed buffer or NULL if not yet finished.
 * @param poutbuf_size  set to size of parsed buffer or zero if not yet finished.
 * @param buf           input buffer.
 * @param buf_size      input length, to signal EOF, this should be 0 (so that the last frame can be output).
 * @param pts           input presentation timestamp.
 * @param dts           input decoding timestamp.
 * @param pos           input byte position in stream.
 * @return the number of bytes of the input bitstream used.
 *
 * Example:
 * @code
 *   while(in_len){
 *       len = av_parser_parse2(myparser, AVCodecContext, &data, &size,
 *                                        in_data, in_len,
 *                                        pts, dts, pos);
 *       in_data += len;
 *       in_len  -= len;
 *
 *       if(size)
 *          decode_frame(data, size);
 *   }
 * @endcode
 * int av_parser_parse2(AVCodecParserContext *s,
                     AVCodecContext *avctx,
                     uint8_t **poutbuf, int *poutbuf_size,
                     const uint8_t *buf, int buf_size,
                     int64_t pts, int64_t dts,
                     int64_t pos);

 * poutbuf 指向解析后输出的亚索编码数据帧，buf 指向输入的压缩编码数据
 *
 * 如果函数执行完后，输出数据为空（poutbuf_size 为 0），则代表解析还没有完成，
 * 还需要再次调用 av_parse_parse2() 解析一部分数据才可以得到解析后的数据帧
 * 当函数执行完后输出数据不为空的时候，代表解析完成，可以将 poutbuf 中的这帧数据取出来做后续处理。
 *
 * 数据结构初始化流程
 *
 * avformat_open_input() -> avformat_new_stream() -> avcodec_alloc_context3() 创建 AVCodecContext
 * av_read_frame() -> av_parser_parse2()
 *
 */

