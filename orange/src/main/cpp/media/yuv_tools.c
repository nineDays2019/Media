//
// Created by yunrui on 2019/3/20.
//

#include "yuv_tools.h"

int yuv420_split(char *url, int w, int h, int num) {
    FILE *fp = fopen(url, "rb+");
    FILE *fp1 = fopen("output_420_y.y", "wb+");
    FILE *fp2 = fopen("output_420_u.y", "wb+");
    FILE *fp3 = fopen("output_420_v.y", "wb+");

    // 使用 unsigned char 就可以用来装一帧的数据
    unsigned char *pic = malloc((size_t) (w * h * 3 / 2));

    for (int i = 0; i < num; i++) {
        fread(pic, 1, (size_t) (w * h * 3 / 2), fp);
        // Y
        fwrite(pic, 1, (size_t) (w * h), fp1);
        // U
        fwrite(pic + w * h, 1, (size_t) (w * h / 4), fp2);
        // V
        fwrite(pic + w * h * 5 / 4, 1, (size_t) (w * h / 4), fp3);
    }

    free(pic);
    fclose(fp);
    fclose(fp1);
    fclose(fp2);
    fclose(fp3);

    return 0;
}

int yuv444_split(char *url, int w, int h, int num) {
    FILE *fp = fopen(url, "rb+");
    FILE *fp1 = fopen("output_444_y.y", "wb+");
    FILE *fp2 = fopen("output_444_u.y", "wb+");
    FILE *fp3 = fopen("output_444_v.y", "wb+");

    unsigned char *pic = malloc((size_t) (w * h * 3));

    for (int i = 0; i < num; i++) {
        fread(pic, 1, (size_t) (w * h * 3), fp);
        // Y
        fwrite(pic, 1, (size_t) (w * h), fp1);
        // U
        fwrite(pic + w * h, 1, (size_t) (w * h), fp2);
        // V
        fwrite(pic + w * h * 2, 1, (size_t) (w * h), fp3);

    }

    free(pic);
    fclose(fp);
    fclose(fp1);
    fclose(fp2);
    fclose(fp3);

    return 0;
}

int yuv420_gray(char *url, int w, int h, int num) {
    FILE *fp = fopen(url, "rb+");
    FILE *fp1 = fopen("output_gray.yuv", "wb+");
    unsigned char *pic = malloc((size_t) (w * h * 3 / 2));

    for (int i = 0; i < num; i++) {
        fread(pic, 1, (size_t) (w * h * 3 / 2), fp);
        // Grey
        memset(pic + w * h, 128, (size_t) (w * h / 2));
        fwrite(pic, 1, (size_t) (w * h * 3 / 2), fp1);
    }
    free(pic);
    fclose(fp);
    fclose(fp1);
    return 0;
}

int yuv420_halfy(char *url, int w, int h, int num) {
    FILE *fp = fopen(url, "rb+");
    FILE *fp1 = fopen("output_half.yuv", "wb+");

    unsigned char *pic = malloc((size_t) (w * h * 3 / 2));

    for (int i = 0; i < num; i++) {
        fread(pic, 1, (size_t) (w * h * 3 / 2), fp);
        // Half
        for (int j = 0; j < w * h; j++) {
            unsigned char temp = (unsigned char) (pic[j] / 2);
            pic[j] = temp;
        }
        fwrite(pic, 1, (size_t) (w * h * 3 / 2), fp1);
    }

    free(pic);
    fclose(fp);
    fclose(fp1);

    return 0;
}


int yuv420_border(char *url, int w, int h,
                  int border, int num) {
    FILE *fp = fopen(url, "rb+");
    FILE *fp1 = fopen("output_border.yuv", "wb+");
    unsigned char *pic = malloc((size_t) (w * h * 3 / 2));

    for (int i = 0; i < num; i++) {
        fread(pic, 1, (size_t) (w * h * 3 / 2), fp);
        // Y
        for (int j = 0; j < h; j++) {
            for (int k = 0; k < w; k++) {
                if (k < border || k > (w - border) ||
                    j < border || j > (h - border)) {
                    pic[j * w + k] = 255;
                }
            }
        }
        fwrite(pic, 1, (size_t) (w * h * 3 / 2), fp1);
    }

    free(pic);
    fclose(fp);
    fclose(fp1);

    return 0;
}

int yuv420_psnr(char *url1, char *url2, int w, int h, int num) {
    FILE *fp1 = fopen(url1, "rb+");
    FILE *fp2 = fopen(url2, "rb+");
    unsigned char *pic1 = malloc((size_t) (w * h));
    unsigned char *pic2 = malloc((size_t) (w * h));

    for (int i = 0; i < num; i++) {
        fread(pic1, 1, (size_t) (w * h), fp1);
        fread(pic2, 1, (size_t) (w * h), fp2);

        double mse_sum = 0, mse = 0, psnr = 0;
        for (int j = 0; j < w * h; j++) {
            mse_sum += pow(pic1[j] - pic2[j], 2);
        }
        mse = mse_sum / (w * h);
        psnr = 10 * log10(255.0 * 255.0 / mse);
        LOGI("%5.3f\n", psnr);

        fseek(fp1, w * h / 2, SEEK_CUR);
        fseek(fp2, w * h / 2, SEEK_CUR);
    }

    free(pic1);
    free(pic2);
    fclose(fp1);
    fclose(fp2);

    return 0;
}

