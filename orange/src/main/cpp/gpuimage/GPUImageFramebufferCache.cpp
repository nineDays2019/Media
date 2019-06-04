//
// Created by yunrui on 2019/6/3.
//

#include "GPUImageFramebufferCache.hpp"
#include "GPUImageUtil.hpp"
#include "GPUImageMath.h"
#include "GPUImageFramebuffer.hpp"

GPUImage::GPUImageFramebufferCache::GPUImageFramebufferCache() {
}

GPUImage::GPUImageFramebufferCache::~GPUImageFramebufferCache() {
    purge();
}

/**
 * 从缓存中获取数据
 */
GPUImage::GPUImageFrameBuffer *
GPUImage::GPUImageFramebufferCache::fetchFramebuffer(void *eglContext, GPUSize size, bool onlyTexture,
                                                     const GPUImage::TextureAttributes textureAttributes) {
    GPUImageFrameBuffer *framebufferFromCache = 0;
    // 计算 Hash 值
    // key 为 size、onlyTexture、textureAttributes
    string lookupHash = getHash(size, onlyTexture, textureAttributes);
    // 匹配到的数量
    int numberOfMatchingFramebuffer = 0;
    if (_framebufferTypeCounts.find(lookupHash) !=
        _framebufferTypeCounts.end()) {
        numberOfMatchingFramebuffer = _framebufferTypeCounts[lookupHash];
    }
    if (numberOfMatchingFramebuffer < 1) {  // 没有命中缓存
        framebufferFromCache = new GPUImageFrameBuffer(size, onlyTexture, textureAttributes);
    } else {    // 命中缓存
        int currentFramebufferId = numberOfMatchingFramebuffer - 1;
        while (!framebufferFromCache && currentFramebufferId >= 0) {
            string framebufferHash =
                    str_format("%s-%ld", lookupHash.c_str(), currentFramebufferId);
            if (_framebuffers.find(framebufferHash) != _framebuffers.end()) {
                framebufferFromCache = _framebuffers[framebufferHash];
                _framebuffers.erase(framebufferHash);
            } else {
                framebufferFromCache = 0;
            }
            currentFramebufferId--;
        }
        currentFramebufferId++;
        _framebufferTypeCounts[lookupHash] = currentFramebufferId;
        if (!framebufferFromCache) {
            framebufferFromCache = new GPUImageFrameBuffer(size, onlyTexture, textureAttributes);
        }
    }
    framebufferFromCache->resetReferenceCount();
    framebufferFromCache->setFrameBufferWeakCache(this);
    return framebufferFromCache;
}

void GPUImage::GPUImageFramebufferCache::returnFramebuffer(GPUImage::GPUImageFrameBuffer *frameBuffer) {
    if (frameBuffer == 0) return;
    const TextureAttributes &textureAttributes = frameBuffer->getTextureAttributes();
    string lookupHash = getHash(frameBuffer->getSize(),
                                !frameBuffer->hasFramebuffer(),
                                textureAttributes);
    int numberOfMatchingFramebuffers = 0;
    if (_framebufferTypeCounts.find(lookupHash) !=
        _framebufferTypeCounts.end()) {
        numberOfMatchingFramebuffers = _framebufferTypeCounts[lookupHash];
    }
    string framebufferHash = str_format("%s-%ld", lookupHash.c_str(),
                                        numberOfMatchingFramebuffers);
    _framebuffers[framebufferHash] = frameBuffer;
    _framebufferTypeCounts[lookupHash] = numberOfMatchingFramebuffers + 1;
}

void GPUImage::GPUImageFramebufferCache::purge() {
    for (const auto kvp : _framebuffers) {
        delete kvp.second;
    }
    _framebuffers.clear();
    _framebufferTypeCounts.clear();
}

std::string GPUImage::GPUImageFramebufferCache::getHash(GPUSize size, bool onlyTexture,
                                                        const GPUImage::TextureAttributes textureAttributes) const {
    if (onlyTexture) {
        return str_format("%.1fx%.1f-%d:%d:%d:%d:%d:%d:%d-NOFB",
                          size.width,
                          size.height,
                          textureAttributes.minFilter,
                          textureAttributes.magFilter,
                          textureAttributes.wrapS,
                          textureAttributes.wrapT,
                          textureAttributes.internalFormat,
                          textureAttributes.format,
                          textureAttributes.type);
    } else {
        return str_format("%.1fx%.1f-%d:%d:%d:%d:%d:%d:%d",
                          size.width,
                          size.height,
                          textureAttributes.minFilter,
                          textureAttributes.magFilter,
                          textureAttributes.wrapS,
                          textureAttributes.wrapT,
                          textureAttributes.internalFormat,
                          textureAttributes.format,
                          textureAttributes.type);
    }
}
