//
// Created by juhezi on 19-3-20.
//

#ifndef MEDIA_RGB_TOOLS_H_H
#define MEDIA_RGB_TOOLS_H_H

#include "../common/common_tools.h"

/**
 * 将 RGB24 数据中的 R、G、B 三个分量分离开并保存成三个文件
 *
 * 与 YUV420p 三个分量分开存储不同，RGB24 格式的每个像素的三个分量是连续存储的。
 *
 * @param url
 * @param w
 * @param h
 * @param num
 * @return
 */
int rgb24_split(char *url, int w, int h, int num);

/**
 * 将 RGB24 格式像素数据封装为 BMP 对象
 * BMP 图像内部实际存储的就是 RGB 数据
 *
 * BMP 采用的是小端存储方式。这种存储方式中“RGB24”格式的像素的分量存储的先后顺序是
 * B、G、R。
 *
 * @param rgb_path
 * @param width
 * @param height
 * @param url_out
 * @return
 */
int rgb24_to_bmp(const char *rgb_path, int width, int height, const char *url_out);

/**
 * 将 RGB24 格式像素数据转换为 YUV420P 格式像素数据
 *
 * Y = 0.299 * R + 0.587 * G + 0.114 * B;
 * U = 0.147 * R - 0.289 * G + 0.463 * B;
 * V = 0.615 * R - 0.515 * G - 0.100 * B;
 *
 * U、V 在水平和垂直方向的取样数是 Y 的一半
 *
 * @param rgb_buff
 * @param w
 * @param h
 * @param yuv_buffer
 * @return
 */
bool rgb24_to_yuv420(unsigned char *rgb_buff, int w, int h, unsigned char *yuv_buffer);

#endif //MEDIA_RGB_TOOLS_H_H
