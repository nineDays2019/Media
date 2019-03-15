//
// Created by yunrui on 2019/3/4.
//
#include "media_tools.h"

struct URLProtocol;

char *getUrlProtocolInfo() {
    char info[40000] = {0};
    av_register_all();

    struct URLProtocol *pup = NULL;
    // Input
    struct URLProtocol **p_temp = &pup;
    avio_enum_protocols((void **) p_temp, 0);
    while ((*p_temp) != NULL) {
        sprintf(info, "%s[In ][%10s]\n", info, avio_enum_protocols((void **) p_temp, 0));
    }
    pup = NULL;
    // Output
    avio_enum_protocols((void **) p_temp, 1);
    while ((*p_temp) != NULL) {
        sprintf(info, "%s[Out][%10s]\n", info, avio_enum_protocols((void **) p_temp, 1));
    }
    return info;
}

char *getAvFormatInfo() {
    char info[40000] = {0};
    av_register_all();

    AVInputFormat *if_temp = av_iformat_next(NULL);
    AVOutputFormat *of_temp = av_oformat_next(NULL);

    // Input
    while (if_temp != NULL) {
        sprintf(info, "%s[In ][%10s]\n", info, if_temp->name);
        if_temp = if_temp->next;
    }
    // Output
    while (of_temp != NULL) {
        sprintf(info, "%s[Out][%10s]\n", info, of_temp->name);
        of_temp = of_temp->next;
    }
    return info;
}

char *getAvCodecInfo() {
    char info[40000] = {0};
    av_register_all();

    AVCodec *c_temp = av_codec_next(NULL);
    while (c_temp != NULL) {
        if (c_temp->decode != NULL) {
            sprintf(info, "%s[Dec]", info);
        } else {
            sprintf(info, "%s[Enc]", info);
        }
        switch (c_temp->type) {
            case AVMEDIA_TYPE_VIDEO:
                sprintf(info, "%s[Video]", info);
                break;
            case AVMEDIA_TYPE_AUDIO:
                sprintf(info, "%s[Audio]", info);
                break;
            default:
                sprintf(info, "%s[Other]", info);
                break;
        }
        sprintf(info, "%s[%10s]\n", info, c_temp->name);

        c_temp = c_temp->next;
    }
    return info;
}

char *getAvFilterInfo() {
    char info[40000] = {0};
    avfilter_register_all();
    AVFilter *f_temp = (AVFilter *) avfilter_next(NULL);
    while (f_temp != NULL) {
        sprintf(info, "%s[%10s]\n", info, f_temp->name);
        f_temp = f_temp->next;
    }
    return info;
}

char *getConfigurationInfo() {
    char info[10000] = {0};
    av_register_all();
    sprintf(info, "%s\n", avcodec_configuration());
    return info;
}

void custom_log(void *ptr, int level, const char *format, va_list vaList) {
    FILE *fp = fopen("/storage/emulated/0/av_log.txt", "a+");
    if (fp) {
        vfprintf(fp, format, vaList);
        fflush(fp);
        fclose(fp);
    }
}

int decode(char *input, char *output) {
    AVFormatContext *pFormatContext;
    int i, video_index;
    AVCodecContext *pCodecContext;
    AVCodec *pCodec;
    AVFrame *pFrame, *pFrameYUV;
    uint8_t *out_buffer;
    AVPacket *packet;

    int y_size;
    int ret, got_picture;
    struct SwsContext *img_convert_ctx;
    FILE *fp_yuv;
    int frame_cnt;
    clock_t time_start, time_finish;
    double time_duration = 0.0;

    char info[1000] = {0};


    av_log_set_callback(custom_log);

    av_register_all();
    avformat_network_init();

    pFormatContext = avformat_alloc_context();

    if (avformat_open_input(&pFormatContext, input, NULL, NULL) != 0) {
        LOGE("Couldn't open input stream.");
        return -1;
    }
    if (avformat_find_stream_info(pFormatContext, NULL) < 0) {
        LOGE("Couldn't find stream information");
        return -1;
    }

    video_index = -1;
    // 寻找视频轨
    for (i = 0; i < pFormatContext->nb_streams; i++) {
        if (pFormatContext->streams[i]->codec->codec_type == AVMEDIA_TYPE_VIDEO) {
            video_index = i;
            break;
        }
    }
    if (video_index == -1) {
        LOGE("Couldn't find a video stream.\n");
        return -1;
    }
    pCodecContext = pFormatContext->streams[video_index]->codec;
    // 获取对应的解码器
    pCodec = avcodec_find_decoder(pCodecContext->codec_id);
    if (pCodec == NULL) {
        LOGE("Couldn't find Codec.\n");
        return -1;
    }
    // 打开解码器
    if (avcodec_open2(pCodecContext, pCodec, NULL) < 0) {
        LOGE("Couldn't open codec.\n");
        return -1;
    }

    pFrame = av_frame_alloc();
    pFrameYUV = av_frame_alloc();
    out_buffer = (unsigned char *) av_malloc(
            (size_t) av_image_get_buffer_size(
                    AV_PIX_FMT_YUV420P, pCodecContext->width, pCodecContext->height, 1));
    av_image_fill_arrays(pFrameYUV->data, pFrameYUV->linesize, out_buffer, AV_PIX_FMT_YUV420P,
                         pCodecContext->width, pCodecContext->height, 1);

    packet = av_malloc(sizeof(AVPacket));

    img_convert_ctx = sws_getContext(pCodecContext->width, pCodecContext->height,
                                     pCodecContext->pix_fmt,
                                     pCodecContext->width, pCodecContext->height,
                                     AV_PIX_FMT_YUV420P, SWS_BICUBIC,
                                     NULL, NULL, NULL);

    sprintf(info, "%s[Input     ]%s\n", "", input);
    sprintf(info, "%s[Output    ]%s\n", info, output);
    sprintf(info, "%s[Format    ]%s\n", info, pFormatContext->iformat->name);
    sprintf(info, "%s[Codec     ]%s\n", info, pCodecContext->codec->name);
    sprintf(info, "%s[Resolution]%dx%d\n", info, pCodecContext->width, pCodecContext->height);

    fp_yuv = fopen(output, "wb+");
    if (fp_yuv == NULL) {
        LOGE("Can not open output file.\n");
        return -1;
    }

    frame_cnt = 0;
    time_start = clock();

    while (av_read_frame(pFormatContext, packet) >= 0) {
        if (packet->stream_index == video_index) {
            ret = avcodec_decode_video2(pCodecContext, pFrame, &got_picture, packet);
            if (ret < 0) {
                LOGE("Decode Error.\n");
                return -1;
            }
            if (got_picture) {
                sws_scale(img_convert_ctx, pFrame->data, pFrame->linesize,
                          0, pCodecContext->height, pFrameYUV->data, pFrameYUV->linesize);
                y_size = pCodecContext->width * pCodecContext->height;
                fwrite(pFrameYUV->data[0], 1, (size_t) y_size, fp_yuv);         // Y
                fwrite(pFrameYUV->data[1], 1, (size_t) (y_size / 4), fp_yuv);   // U
                fwrite(pFrameYUV->data[2], 1, (size_t) (y_size / 4), fp_yuv);   // V

                // Output Info
                char pictype_str[10] = {10};
                switch (pFrame->pict_type) {
                    case AV_PICTURE_TYPE_I:
                        sprintf(pictype_str, "I");
                        break;
                    case AV_PICTURE_TYPE_P:
                        sprintf(pictype_str, "P");
                        break;
                    case AV_PICTURE_TYPE_B:
                        sprintf(pictype_str, "B");
                        break;
                    default:
                        sprintf(pictype_str, "Other");
                        break;
                }

                LOGI("Frame Index: %5d. Type:%s", frame_cnt, pictype_str);
                frame_cnt++;
            }

        }
        av_free_packet(packet);
    }

    // flush decoder
    while (1) {
        ret = avcodec_decode_video2(pCodecContext, pFrame, &got_picture, packet);
        if (ret < 0) {
            break;
        }
        if (!got_picture) {
            break;
        }
        sws_scale(img_convert_ctx, pFrame->data, pFrame->linesize,
                  0, pCodecContext->height,
                  pFrameYUV->data, pFrameYUV->linesize);
        int y_size = pCodecContext->width * pCodecContext->height;
        fwrite(pFrameYUV->data[0], 1, (size_t) y_size, fp_yuv);      // Y
        fwrite(pFrameYUV->data[1], 1, (size_t) y_size / 4, fp_yuv);  // U
        fwrite(pFrameYUV->data[2], 1, (size_t) y_size / 4, fp_yuv);  // V

        char pictype_str[10] = {0};
        switch (pFrame->pict_type) {
            case AV_PICTURE_TYPE_I:
                sprintf(pictype_str, "I");
                break;
            case AV_PICTURE_TYPE_P:
                sprintf(pictype_str, "P");
                break;
            case AV_PICTURE_TYPE_B:
                sprintf(pictype_str, "B");
                break;
            default:
                sprintf(pictype_str, "Other");
                break;
        }
        LOGI("Frame Index: %5d. Type:%s", frame_cnt, pictype_str);
        frame_cnt++;
    }
    time_finish = clock();
    time_duration = time_finish - time_start;

    sprintf(info, "%s[Time      ]%fms\n", info, time_duration);
    sprintf(info, "%s[Count     ]%d\n", info, frame_cnt);

    sws_freeContext(img_convert_ctx);

    fclose(fp_yuv);

    av_frame_free(&pFrameYUV);
    av_frame_free(&pFrame);
    avcodec_close(pCodecContext);
    avformat_close_input(&pFormatContext);

    return 0;
}

int flush_encoder(AVFormatContext *fmt_ctx, unsigned int stream_index) {
    int ret;
    int got_frame;
    AVPacket enc_pkt;
    if (!(fmt_ctx->streams[stream_index]->codec->codec->capabilities &
          AV_CODEC_CAP_DELAY)) {
        return 0;
    }
    while (1) {
        enc_pkt.data = NULL;
        enc_pkt.size = 0;
        av_init_packet(&enc_pkt);
        ret = avcodec_encode_video2(fmt_ctx->streams[stream_index]->codec,
                                    &enc_pkt, NULL, &got_frame);
        av_frame_free(NULL);
        if (ret < 0) {
            break;
        }
        if (!got_frame) {
            ret = 0;
            break;
        }
        LOGE("Flush Encoder: Succeed to encode 1 frame!\t size: %5d\n", enc_pkt.size);
        // Write a packet to an output media file.
        ret = av_write_frame(fmt_ctx, &enc_pkt);
        if (ret < 0) {
            break;
        }
    }
    return ret;
}

int encode(char *input, char *resolution, char *setting, char *output) {
    AVFormatContext *pFormatCtx;
    AVOutputFormat *fmt;
    AVStream *video_st;
    AVCodecContext *pCodecCtx;
    AVCodec *pCodec;
    AVPacket pkt;
    uint8_t *picture_buf;
    AVFrame *pFrame;
    int picture_size;
    int y_size;
    int framecnt = 0;
    int i;
    int ret = 0;
    char *parsed_key, *value;

    FILE *in_file = fopen(input, "rb"); // Input raw YUV data

    int in_w = 0, in_h = 0; // Input data's width and height
    av_parse_video_size(&in_w, &in_h, resolution);  // convert resolution => width and height
    int framenum = 1000;
    av_log_set_callback(custom_log);

    // Method 1
    av_register_all();
    pFormatCtx = avformat_alloc_context();
    // 根据路径猜 format
    fmt = av_guess_format(NULL, output, NULL);
    LOGE("Output Format is %s\n", fmt->name)
    pFormatCtx->oformat = fmt;

    // Method 2
    if (avio_open(&pFormatCtx->pb, output, AVIO_FLAG_READ_WRITE) < 0) {
        LOGE("Failed to open output file!\n");
        return -1;
    }

    video_st = avformat_new_stream(pFormatCtx, 0);

    if (video_st == NULL) {
        return -1;
    }

    video_st->time_base.num = 1;
    video_st->time_base.den = 25;   // fps 25 ? 只是猜测

    pCodecCtx = video_st->codec;
    pCodecCtx->codec_id = fmt->video_codec;
    pCodecCtx->codec_type = AVMEDIA_TYPE_VIDEO;
    pCodecCtx->pix_fmt = AV_PIX_FMT_YUV420P;    // 像素格式
    pCodecCtx->width = in_w;
    pCodecCtx->height = in_h;
    pCodecCtx->time_base.num = 1;
    pCodecCtx->time_base.den = 25;
    pCodecCtx->bit_rate = 400000;   // 比特率
    pCodecCtx->gop_size = 250;  // 250 frame in a gop
    pCodecCtx->qmin = 10;
    pCodecCtx->qmax = 51;

    pCodecCtx->max_b_frames = 3;

    AVDictionary *param = 0;
    while (*setting) {
        // key-value 解析
        // pointer to the options string, will be updated to point to the [rest] of the string
        ret = av_opt_get_key_value((const char **) &setting, "=", ":", 0, &parsed_key, &value);
        if (ret < 0) {
            break;
        }
        av_dict_set(&param, parsed_key, value, 0);
        LOGE("key: %s value: %s\n", parsed_key, value);
        if (*setting) {
            setting++;
        }
        av_free(parsed_key);
        av_free(value);
    }

    // AV_CODEC_ID_MPEG2VIDEO
    pCodec = avcodec_find_encoder(pCodecCtx->codec_id);
    if (!pCodec) {
        LOGE("Can not find encoder!");
        return -1;
    }
    if (avcodec_open2(pCodecCtx, pCodec, &param) < 0) {
        LOGE("Failed to open encoder!\n");
        return -1;
    }

    pFrame = av_frame_alloc();
    picture_size = avpicture_get_size(pCodecCtx->pix_fmt, pCodecCtx->width, pCodecCtx->height);
    picture_buf = av_malloc((size_t) picture_size);
    av_image_fill_arrays(pFrame->data, pFrame->linesize,
                         picture_buf, pCodecCtx->pix_fmt,
                         pCodecCtx->width, pCodecCtx->height, 1);

    // Write File Header
    avformat_write_header(pFormatCtx, NULL);

    av_new_packet(&pkt, picture_size);

    y_size = pCodecCtx->width * pCodecCtx->height;

    for (i = 0; i < framenum; i++) {
        // Read raw YUV data
        if (fread(picture_buf, 1, (size_t) (y_size * 3 / 2), in_file) <= 0) {
            LOGE("Failed to read raw data!\n");
            break;
        } else if (feof(in_file)) {
            break;
        }
        pFrame->data[0] = picture_buf;                      // Y
        pFrame->data[1] = picture_buf + y_size;             // U
        pFrame->data[2] = picture_buf + y_size * 5 / 4;     // V

        //PTS
        pFrame->pts = i;
        int got_picture = 0;
        // Encode
        int ret = avcodec_encode_video2(pCodecCtx, &pkt, pFrame, &got_picture);
        if (ret < 0) {
            LOGE("Failed to encode! \n");
            return -1;
        }
        if (got_picture == 1) {
            LOGE("Succeed to encode frame: %5d\tsize:%5d\n", framecnt, pkt.size);
            framecnt++;
            pkt.stream_index = video_st->index;
            ret = av_write_frame(pFormatCtx, &pkt);
            av_free_packet(&pkt);
        }
    }
    // Flush Encoder
    ret = flush_encoder(pFormatCtx, 0);
    if (ret < 0) {
        LOGE("Flushing encoder failed.\n");
        return -1;
    }

    // Write file trailer
    av_write_trailer(pFormatCtx);

    // Clean
    if (video_st) {
        avcodec_close(video_st->codec);
        av_free(pFrame);
        av_free(picture_buf);
    }
    avio_close(pFormatCtx->pb);
    avformat_free_context(pFormatCtx);

    fclose(in_file);

    return 0;
}

