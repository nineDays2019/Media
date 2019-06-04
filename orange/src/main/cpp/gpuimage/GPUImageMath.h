//
// Created by yunrui on 2019/6/3.
//

#ifndef LEGEND_GPUIMAGEMATH_H
#define LEGEND_GPUIMAGEMATH_H

#include "GPUImageMacros.h"

typedef struct {
    float x;
    float y;
} GPUPoint;

typedef struct {
    float width;
    float height;
} GPUSize;

typedef struct {
    int64_t value;
    int32_t timescale;
} GPUTime;

#define kGPUTimeZero {0,0}

typedef struct {
    GLfloat one;
    GLfloat two;
    GLfloat three;
    GLfloat four;
} GPUVector4;

typedef struct {
    GLfloat one;
    GLfloat two;
    GLfloat three;
} GPUVector3;

#endif //LEGEND_GPUIMAGEMATH_H
