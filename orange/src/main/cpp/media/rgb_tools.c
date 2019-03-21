//
// Created by juhezi on 19-3-20.
//

#include "rgb_tools.h"

int rgb24_split(char *url, int w, int h, int num) {
    FILE *fp = fopen(url, "rb+");
    FILE *fp1 = fopen("output_r.y", "wb+");
    FILE *fp2 = fopen("output_g.y", "wb+");
    FILE *fp3 = fopen("output_b.y", "wb+");

    unsigned char *pic = malloc((size_t) (w * h * 3));

    for (int i = 0; i < num; i++) {
        fread(pic, 1, (size_t) (w * h * 3), fp);
        for (int j = 0; j < w * h * 3; j = j + 3) {
            // R
            fwrite(pic + j, 1, 1, fp1);
            // G
            fwrite(pic + j + 1, 1, 1, fp2);
            // B
            fwrite(pic + j + 2, 1, 1, fp3);
        }
    }
    free(pic);
    fclose(fp);
    fclose(fp1);
    fclose(fp2);
    fclose(fp3);

    return 0;
}

int rgb24_to_bmp(const char *rgb_path, int width, int height, const char *url_out) {

    typedef struct {
        long imageSize;
        long blank;
        long startPosition;
    } BmpHead;

    typedef struct {
        long length;
        long width;
        long height;
        unsigned short colorPlane;
        unsigned short bitColor;
        long zipFormat;
        long realSize;
        long xPels;
        long yPels;
        long colorUse;
        long colorImportant;
    } InfoHead;

    int i = 0, j = 0;
    BmpHead m_BMPHeader = {0};
    InfoHead m_BMPInfoHeader = {0};
    char bfType[2] = {'B', 'M'};
    int header_size = sizeof(bfType) + sizeof(BmpHead) + sizeof(InfoHead);
    unsigned char *rgb24_buffer = NULL;
    FILE *fp_rgb24 = NULL, *fp_bmp = NULL;

    if ((fp_rgb24 = fopen(rgb_path, "rb")) == NULL) {
        LOGE("Error: Can not open input RGB24 file.\n");
        return -1;
    }
    if ((fp_bmp = fopen(url_out, "wb")) == NULL) {
        LOGE("Error: Can not open output BMP file.\n");
        return -1;
    }
    rgb24_buffer = malloc((size_t) (width * height * 3));
    fread(rgb24_buffer, 1, (size_t) (width * height * 3), fp_rgb24);

    m_BMPHeader.imageSize = 3 * width * height + header_size;
    m_BMPHeader.startPosition = header_size;

    m_BMPInfoHeader.length = sizeof(InfoHead);
    m_BMPInfoHeader.width = width;
    // 反向
    // BMP storage pixel data in opposite direction
    // of Y-axis (from bottom to top)
    m_BMPInfoHeader.height = -height;
    m_BMPInfoHeader.colorPlane = 1;
    m_BMPInfoHeader.bitColor = 24;
    m_BMPInfoHeader.realSize = 3 * width * height;

    fwrite(bfType, 1, sizeof(bfType), fp_bmp);
    fwrite(&m_BMPHeader, 1, sizeof(m_BMPHeader), fp_bmp);
    fwrite(&m_BMPInfoHeader, 1, sizeof(m_BMPInfoHeader), fp_bmp);

    // BMP save R1|G1|B1,R2|G2|B2 as B1|G1|R1,B2|G2|R2
    // So change 'R' and 'B'
    for (j = 0; j < height; j++) {
        for (i = 0; i < width; i++) {
            unsigned char temp = rgb24_buffer[(j * width + i) * 3 + 2];
            rgb24_buffer[(j * width + i) * 3 + 2] = rgb24_buffer[(j * width + i) * 3 + 0];
            rgb24_buffer[(j * width + i) * 3 + 0] = temp;
        }
    }
    fwrite(rgb24_buffer, (size_t) (3 * width * height), 1, fp_bmp);
    fclose(fp_rgb24);
    fclose(fp_bmp);
    free(rgb24_buffer);
    LOGI("Finish generate %s!\n", url_out);
    return 0;
}

unsigned char clip_value(unsigned char x, unsigned char min_val,
                         unsigned char max_val) {
    if (x > max_val) {
        return max_val;
    } else if (x < min_val) {
        return min_val;
    } else {
        return x;
    }
}


bool rgb24_to_yuv420(unsigned char *rgb_buff, int w, int h, unsigned char *yuv_buffer) {
    unsigned char *ptrY, *ptrU, *ptrV, *ptrRGB;
    memset(yuv_buffer, 0, (size_t) (w * h * 3 / 2));
    ptrY = yuv_buffer;
    ptrU = yuv_buffer + w * h;
    ptrV = ptrU + (w * h * 1 / 4);
    unsigned char y, u, v, r, g, b;
    for (int j = 0; j < h; j++) {
        ptrRGB = rgb_buff + w * j * 3;

        for (int i = 0; i < w; i++) {
            r = *(ptrRGB++);
            g = *(ptrRGB++);
            b = *(ptrRGB++);
            y = (unsigned char) (((66 * r + 129 * g + 25 * b + 128) >> 8) + 16);
            u = (unsigned char) (((-38 * r - 74 * g + 112 * b + 128) >> 8) + 128);
            v = (unsigned char) (((112 * r - 94 * g - 18 * b + 128) >> 8) + 128);
            *(ptrY++) = clip_value(y, 0, 255);
            if (j % 2 == 0 && i % 2 == 0) {
                *(ptrU++) = clip_value(u, 0, 255);
            } else {
                if (i % 2 == 0) {
                    *(ptrV++) = clip_value(v, 0, 255);
                }
            }

        }

    }
}