//
// Created by yunrui on 2019/6/11.
//

#ifndef LEGEND_GPUFRAMEBUFFER_HPP
#define LEGEND_GPUFRAMEBUFFER_HPP

#include <stdio.h>
#include <vector>
#include "GPUImageMacros.h"
#include "GPUObject.hpp"
#include "GPUImageMath.h"

NS_GI_BEGIN

    typedef struct {
        GLenum minFilter;
        GLenum magFilter;
        GLenum wrapS;
        GLenum wrapT;
        GLenum internalFormat;
        GLenum format;
        GLenum type;
    } TextureOptions;

    class GPUFrameBuffer : public GPUObject {
    public:
        GPUFrameBuffer();

        GPUFrameBuffer(GPUSize
                       size,
                       TextureOptions options
        );

        virtual ~GPUFrameBuffer();

        GLuint getTexture();

        GPUSize getSize();

        TextureOptions getTextureOptions();

        void activateFramebuffer();

        void inactivateFramebuffer();

        static TextureOptions defaultTextureOptions;

        void destroy();

        void generateTexture();

        void generateFramebuffer();

        bool getGenerateFramebufferSuccess();

    private:
        GPUSize _size;
        TextureOptions _textureOptions;
        GLuint _texture;
        GLuint _framebuffer;
        bool _generateFramebufferSuccess;
    };

NS_GI_END

#endif //LEGEND_GPUFRAMEBUFFER_HPP
