// YUV 视频数据的处理方法
// For Exercise
// Demo
// Y - 明亮度
// U - 色度
// V - 明度
// Created by yunrui on 2019/3/20.
//

#ifndef LEGEND_YUV_TOOLS_H
#define LEGEND_YUV_TOOLS_H

#include "../common/common_tools.h"

/**
 * 分离 YUV420p像素数据中的 Y、U、V 分量
 * url - YUV file
 * w   - Width of Input YUV file.
 * h   - Height of Input YUV file.
 * num - Number of frames to process.
 *
 * 分析：
 * 如果视频的宽和高分别是 w 和 h，那么一帧 YUV420p 像素数据一共占有 w * h * 3 / 2 Byte
 * 的数据。
 * 其中前 w * h Byte 存储 Y、
 * 接着的 w * h * 1 / 4 Byte 存储 U
 * 最后的 w * h * 1 / 4 Byte 存储 V
 *
 * 例：
 * 输入：分辨率为 256 x 256 的 yuv420p 像素文件
 * 输出：output_420_y.y，纯 Y 数据，分辨率 256 x 256
 * output_420_u.y，纯 U 数据，分辨率 [128 x 128]
 * output_420_v.y，纯 V 数据，分辨率 [128 x 128]
 */
int yuv420_split(char *url, int w, int h, int num);

/**
 * 分离 YUV440p 像素数据中的 Y、U、V 分量
 * 分析:
 * 如果视频帧的宽和高分别为 w 和 h，那么一帧 YUV 444P 像素数据一共占用 w * h * 3 Byte 的数据。
 * 其中前 w * h Byte 存储 Y
 * 接着的 w * h Byte 存储 U
 * 最后的 w * h Byte 存储 V
 *
 * 例：
 * 输入：分辨率为 256 x 256 的 yuv420p 像素文件
 * 输出：output_420_y.y，纯 Y 数据，分辨率 256 x 256
 * output_420_u.y，纯 U 数据，分辨率 [256 x 256]
 * output_420_v.y，纯 V 数据，分辨率 [256 x 256]
 */
int yuv444_split(char *url, int w, int h, int num);

/**
 * 将 YUV420p格式像素数据的彩色去掉，编程纯粹的灰度图。
 * 分析：
 * 将 U、V分量设置为 128。
 * U、V 是图像中的经过偏置处理的色度分量。色度分量在偏置处理的取值范围是 -128 ~ 127，
 * 这时候的无色对应的是 "0" 值。
 * 经过偏置后色度分量取值变成了 0 至 255，因此此时的无色对应的就是 128 了。
 */
int yuv420_gray(char *url, int w, int h, int num);

/**
 * 通过将 YUV 数据中的亮度分量 Y 的数值减半的方法，降低图像的亮度
 * 图像中的每个 Y 值占用 1 Byte，取值范围是 0 至 255，对应 C 语言中的
 * unsigned char 数据类型。
 */
int yuv420_halfy(char *url, int w, int h, int num);

/**
 * 通过修改 YUV 数据中特定位置的亮度分量 Y 的数值，给图像添加一个边框效果。
 * 将距离图像边缘 border 范围内的像素的亮度分量 Y 的取值设置成了最大亮度值 255
 */
int yuv420_border(char *url, int w, int h, int border, int num);

/**
 * 计算两个 YUV420p 像素数据的 PSNR
 *
 * PSNR 是最基本的视频质量评价方法。
 * 本方法可以对比两张 YUV 图片中亮度分量 Y 的 PSNR
 *
 * PSNR 是 "Peak Signal to Noise Ratio" 的缩写，即峰值信噪比，
 * 是一种评价图像的客观标准。
 *
 * PSNR 取值通常情况下都在 20 - 50 的范围内，取值越高，代表两张图像越接近，反映出受损图像质量越好。
 */
int yuv420_psnr(char *url1, char *url2, int w, int h, int num);

#endif //LEGEND_YUV_TOOLS_H
