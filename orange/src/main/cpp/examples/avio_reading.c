// libavformat AVIOContext API example
// 音视频文件读取到内存，如果 FFmpeg 需要使用输入文件的数据，则直接从内存中调用。
// Created by yunrui on 2020-02-21.
//

#include <libavcodec/avcodec.h>
#include <libavformat/avformat.h>
#include <libavformat/avio.h>
#include <libavutil/file.h>

struct buffer_data {
    uint8_t *ptr;
    size_t size;
};

// 这个函数会执行很多次，每次 buf_size 都是指定的 buf_size ，这里是 4K
// 将文件中数据拷贝到缓冲区，同时文件指针位置偏移，数据大小改变
static int read_packet(void *opaque, uint8_t *buf, int buf_size) {
    struct buffer_data *bd = (struct buffer_data *) opaque;
    // 判断可用的数量
    buf_size = FFMIN(buf_size, bd->size);
    if (!buf_size) {
        return AVERROR_EOF;
    }

    // copy internal buffer data to buf
    memcpy(buf, bd->ptr, buf_size);
    bd->ptr += buf_size;
    bd->size -= buf_size;

    return buf_size;
}

int avio_reading() {
    AVFormatContext *fmt_ctx = NULL;
    AVIOContext *avio_ctx = NULL;
    uint8_t *buffer = NULL, *avio_ctx_buffer = NULL;
    size_t buffer_size, avio_ctx_buffer_size = 4096;
    char *input_filename = "input_file_path";   //输入路径
    int ret = 0;
    struct buffer_data bd = {0};
    // 把 input_filename 中的数据读取到 buffer 中。
    // 返回文件开始指针和文件大小，不消耗内存
    ret = av_file_map(input_filename, &buffer, &buffer_size, 0, NULL);
    bd.ptr = buffer;
    bd.size = buffer_size;
    if (!(fmt_ctx = avformat_alloc_context())) {
        ret = AVERROR(ENOMEM);
    }
    // 分配内存，可以自己设置缓冲大小，这里设置的是 4K
    avio_ctx_buffer = av_malloc(avio_ctx_buffer_size);
    // 作用
    // 为缓冲 I/O 分配和初始化 AVIOContext
    avio_ctx = avio_alloc_context(avio_ctx_buffer,
                                  avio_ctx_buffer_size,
                                  0,
                                  &bd,
                                  &read_packet,// 用于往 buffer 填充数据的函数
                                  NULL,
                                  NULL);
    fmt_ctx->pb = avio_ctx;
    // read_packet 回调函数会在这里被调用，它将输入文件的所有数据都先存入缓存中。
    // 如果后面有需要用到数据，那么它就从缓冲中直接调用数据。
    ret = avformat_open_input(&fmt_ctx, NULL, NULL, NULL);
    ret = avformat_find_stream_info(fmt_ctx, NULL);
    // 输出基本信息
    av_dump_format(fmt_ctx, 0, input_filename, 0);

    // close
    avformat_close_input(&fmt_ctx);

    if (avio_ctx) {
        av_freep(&avio_ctx->buffer);
    }
    avio_context_free(&avio_ctx);
    av_file_unmap(buffer, buffer_size);
//    av_err2str(1)
    return 0;
}




