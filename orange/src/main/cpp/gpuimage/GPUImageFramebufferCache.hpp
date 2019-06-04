//
// Created by yunrui on 2019/6/3.
//

#ifndef LEGEND_GPUIMAGEFRAMEBUFFERCACHE_HPP
#define LEGEND_GPUIMAGEFRAMEBUFFERCACHE_HPP

#include <stdio.h>
#include <string>
#include <map>
#include "GPUImageMacros.h"
#include "GPUObject.hpp"
#include "GPUImageMath.h"
#include "GPUImageFramebuffer.hpp"

NS_GI_BEGIN

    using namespace std;

    class GPUImageFramebufferCache : public GPUObject {
    public:
        GPUImageFramebufferCache();

        virtual ~GPUImageFramebufferCache();


        GPUImageFrameBuffer *fetchFramebuffer(void *eglContext,
                                              GPUSize size,
                                              bool onlyTexture = false,
                                              const TextureAttributes textureAttributes =
                                              GPUImageFrameBuffer::defaultTextureAttributes);

        void returnFramebuffer(GPUImageFrameBuffer *frameBuffer);

        void purge();

        string getHash(GPUSize size, bool onlyTexture,
                       const TextureAttributes textureAttributes) const;

    private:
        map<string, GPUImageFrameBuffer *> _framebuffers;   // 整体缓存，[id、FrameBuffer]
        map<string, int> _framebufferTypeCounts;    // 类型缓存、[Hash、count]
    };

NS_GI_END

#endif //LEGEND_GPUIMAGEFRAMEBUFFERCACHE_HPP
