//
// Created by yunrui on 2019/3/24.
//

#ifndef LEGEND_PCM_UTILS_H
#define LEGEND_PCM_UTILS_H

#include "../common/common_tools.h"

/**
 * 分离 PCM16LE 双声道音频采样数据的左声道和右声道
 *
 * PCM16LE 双声道数据中左声道和右声道的采样值是间隔存储的。每个采样值
 * 占用 2Byte 空间。
 *
 * 注：本函数中声音样值的采样频率一律是 44100 HZ，采样率一律为 16LE。
 * "16" 代表采样位数是 16 bit。由于 1Byte=8bit，所以一个声道占用 2Byte。
 * "LE" 代表 Little Endian，代表 2 Byte 采样值的存储方式为高位存在高地址。
 */
int pcm16le_split(char *url);

/**
 * 将 PCM16LE 双声道音频采样数据中左声道的音量降一半
 *
 * 本程序在读出左声道的 2 Byte 的取样值之后，
 * 将其当成了 C 中的一个 short 类型的变量。
 * 将该值➗2之后写回到了 PCM 文件中。
 */
int pcm16le_halfvolumeleft(char *url);

/**
 * 将 PCM16LE 双声道音频采样数据的声音速度提高一倍
 * 本程序值采样了每个声道奇数点的样值。
 */
int pcm16le_doublespeed(char *url);

/**
 * 将 PCM16LE 双声道音频采样数据转换为 PCM8 音频采样数据
 *
 *
 * PCM16LE 格式的采样数据的取值范围是 -32768 到 32767，而 PCM8 格式的采样数据的取值范围是 0 到 255。
 * 所以 PCM16LE 转换到 PCM8 需要经过两个步骤：第一步将 -32768到32767的16bit
 * 有符号数值转换为-128到127的8bit有符号数值，第二步是将-128到127的8bit有符号数值转换为0到255的8bit无符号
 * 数值。
 */
int pcm16le_to_pcm8(char *url);

/**
 * 从 PCM16LE 单声道音频采样数据中截取一部分数据
 *
 *
 */
int pcm16le_cut_singlechannel(char *url, int start_num, int dur_num);

/**
 * 将 PCM16LE 双声道音频采样数据转换为WAVE格式音频数据
 *
 * WAVE 格式音频（扩展名为".wav"）是 Windows 系统中最常见的一种音频。
 * 该格式的实质就是在 PCM 文件的前面加了一个文件头。
 */
int pcm16le_to_wave(const char *pcmpath,
                    int channels, int sample_rate,
                    const char *wavepath);

#endif //LEGEND_PCM_UTILS_H
