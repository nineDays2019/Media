//
// Created by yunrui on 2019/6/3.
//

#ifndef LEGEND_GPUIMAGEUTIL_HPP
#define LEGEND_GPUIMAGEUTIL_HPP

#include <stdlib.h>
#include <string>
#include "GPUImageMacros.h"

GLint maximumTextureSizeForThisDevice();

GLint maximumTextureUnitsForThisDevice();

std::string str_format(const char *fmt, ...);

#endif //LEGEND_GPUIMAGEUTIL_HPP
