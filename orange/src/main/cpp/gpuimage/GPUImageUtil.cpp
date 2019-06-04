//
// Created by yunrui on 2019/6/3.
//

#include "GPUImageUtil.hpp"


GLint maximumTextureSizeForThisDevice() {
    GLint maxTextureSize;
    glGetIntegerv(GL_MAX_TEXTURE_SIZE, &maxTextureSize);
    return maxTextureSize;
}

GLint maximumTextureUnitsForThisDevice() {
    GLint maxTextureUnits;
    glGetIntegerv(GL_MAX_TEXTURE_UNITS, &maxTextureUnits);
    return maxTextureUnits;
}

std::string str_format(const char *fmt, ...) {
    std::string strResult = "";
    if (NULL != fmt) {
        va_list marker;
        va_start(marker, fmt);
        char *buf = 0;
        int result = vasprintf(&buf, fmt, marker);
        if (!buf) {
            va_end(marker);
            return strResult;
        }

        if (result < 0) {
            free(buf);
            va_end(marker);
            return strResult;
        }
        result = strlen(buf);
        strResult.append(buf, static_cast<unsigned int>(result));
        free(buf);
        va_end(marker);
    }
    return strResult;
}
