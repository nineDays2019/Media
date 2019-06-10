//
// Created by yunrui on 2019/6/3.
//

#ifndef LEGEND_GPUIMAGEFRAMEBUFFER_HPP
#define LEGEND_GPUIMAGEFRAMEBUFFER_HPP

#include <stdio.h>
#include <vector>
#include "GPUImageMacros.h"
#include "GPUObject.hpp"
#include "GPUImageMath.h"
#include "filter/GPUImageFilter.hpp"

NS_GI_BEGIN

    typedef struct {
        GLenum minFilter;
        GLenum magFilter;
        GLenum wrapS;
        GLenum wrapT;
        GLenum internalFormat;
        GLenum format;
        GLenum type;
    } TextureAttributes;

    class GPUImageFrameBufferCache;

    class GPUImageFrameBuffer : public GPUObject {
    public:
        GPUImageFrameBuffer(GPUSize size,
                            bool onlyGenerateTexture = false,
                            const TextureAttributes = defaultTextureAttributes);

//        GPUImageFrameBuffer(void *textureCache,
//                            GPUSize size,
//                            bool onlyGenerateTexture = false,
//                            const TextureAttributes textureAttributes = defaultTextureAttributes);

        ~GPUImageFrameBuffer();

        virtual void release(bool returnToCache = true);

        GLuint getTexture() const;

        GLuint getFramebuffer() const;

        GPUSize getSize() const;

        const TextureAttributes &getTextureAttributes() const;

        bool hasFramebuffer();

        bool getGenerateFramebufferSuccess();

        void activateFramebuffer();

        void inactivateFramebuffer();

        void setFrameBufferWeakCache(GPUImageFrameBufferCache *frameBufferWeakCache);

        static TextureAttributes defaultTextureAttributes;

    private:
        GPUSize _size;
        TextureAttributes _textureAttributes;
        bool _hasFB;
        bool _generateFramebufferSuccess;
        GLuint _texture;
        GLuint _framebuffer;
        GPUImageFrameBufferCache *_frameBufferWeakCache;

        void _generateTexture();

        void _generateFramebuffer();

        static std::vector<GPUImageFrameBuffer *> _framebuffers;

    };

NS_GI_END

#endif //LEGEND_GPUIMAGEFRAMEBUFFER_HPP
