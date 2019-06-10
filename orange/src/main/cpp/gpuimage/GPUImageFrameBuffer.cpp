//
// Created by yunrui on 2019/6/3.
//

#include "GPUImageFrameBuffer.hpp"
#include "GPUImageFrameBufferCache.hpp"
#include "GPUImageMath.h"

#define PUSHSTATE() GLint restoreId; glGetIntegerv(GL_FRAMEBUFFER_BINDING, &restoreId);
#define POPSTATE() glBindFramebuffer(GL_FRAMEBUFFER, restoreId);

std::vector<GPUImage::GPUImageFrameBuffer *>GPUImage::GPUImageFrameBuffer::_framebuffers;

GPUImage::TextureAttributes GPUImage::GPUImageFrameBuffer::defaultTextureAttributes = {
        .minFilter = GL_LINEAR,
        .magFilter = GL_LINEAR,
        .wrapS = GL_CLAMP_TO_EDGE,
        .wrapT = GL_CLAMP_TO_EDGE,
        .internalFormat = GL_RGBA,
        .format = GL_RGBA,
        .type = GL_UNSIGNED_BYTE
};

GPUImage::GPUImageFrameBuffer::GPUImageFrameBuffer(GPUSize size, bool onlyGenerateTexture,
                                                   const GPUImage::TextureAttributes textureAttributes) :
        _texture(static_cast<GLuint>(NO_TEXTURE)),
        _framebuffer(static_cast<GLuint>(NO_FRAMEBUFFER)),
        _frameBufferWeakCache(NULL),
        _generateFramebufferSuccess(false) {
    _size = size;
    _textureAttributes = textureAttributes;
    _hasFB = !onlyGenerateTexture;

    if (_hasFB) {
        _generateFramebuffer();
    } else {
        _generateTexture();
    }

    _framebuffers.push_back(this);

}

GPUImage::GPUImageFrameBuffer::~GPUImageFrameBuffer() {
    std::vector<GPUImageFrameBuffer *>::iterator itr =
            std::find(_framebuffers.begin(), _framebuffers.end(), this);
    if (itr != _framebuffers.end()) {
        _framebuffers.erase(itr);
    }

    bool bDeleteTex = (_texture != NO_TEXTURE);
    bool bDeleteFB = (_framebuffer != NO_FRAMEBUFFER);

    for (auto const &framebuffer : _framebuffers) {
        if (bDeleteTex) {
            if (_texture == framebuffer->getTexture()) {
                bDeleteTex = false;
            }
        }
        if (bDeleteFB) {
            if (framebuffer->hasFramebuffer() &&
                _framebuffer == framebuffer->getFramebuffer()) {
                bDeleteFB = false;
            }
        }
    }

    if (bDeleteTex) {
        glDeleteTextures(1, &_framebuffer);
        _framebuffer = static_cast<GLuint>(NO_FRAMEBUFFER);
    }
    if (bDeleteFB) {
        glDeleteFramebuffers(1, &_framebuffer);
        _framebuffer = static_cast<GLuint>(NO_FRAMEBUFFER);
    }
}

void GPUImage::GPUImageFrameBuffer::release(bool returnToCache) {
    if (returnToCache) {
        assert(_referenceCount > 0);
        --_referenceCount;
        if (_referenceCount == 0) {
            if (_frameBufferWeakCache) {
                _frameBufferWeakCache->returnFramebuffer(this);
            }
        }
    } else {
        GPUObject::release();
    }
}

GLuint GPUImage::GPUImageFrameBuffer::getTexture() const {
    return _texture;
}

GLuint GPUImage::GPUImageFrameBuffer::getFramebuffer() const {
    return _framebuffer;
}

GPUSize GPUImage::GPUImageFrameBuffer::getSize() const {
    return _size;
}

const GPUImage::TextureAttributes &GPUImage::GPUImageFrameBuffer::getTextureAttributes() const {
    return _textureAttributes;
}

bool GPUImage::GPUImageFrameBuffer::hasFramebuffer() {
    return _hasFB;
}

bool GPUImage::GPUImageFrameBuffer::getGenerateFramebufferSuccess() {
    return _generateFramebufferSuccess;
}

void GPUImage::GPUImageFrameBuffer::activateFramebuffer() {
    if (_framebuffer != NO_FRAMEBUFFER) {
        glBindFramebuffer(GL_FRAMEBUFFER, _framebuffer);
        glViewport(0, 0,
                   static_cast<GLsizei>(_size.width),
                   static_cast<GLsizei>(_size.height));
    }
}

void GPUImage::GPUImageFrameBuffer::inactivateFramebuffer() {
    glBindFramebuffer(GL_FRAMEBUFFER, 0);
}

void GPUImage::GPUImageFrameBuffer::setFrameBufferWeakCache(GPUImage::GPUImageFrameBufferCache *frameBufferWeakCache) {
    _frameBufferWeakCache = frameBufferWeakCache;
}

void GPUImage::GPUImageFrameBuffer::_generateTexture() {
    if (_texture == NO_TEXTURE) {
        glGenTextures(1, &_texture);
        glBindTexture(GL_TEXTURE_2D, _texture);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, _textureAttributes.minFilter);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, _textureAttributes.magFilter);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, _textureAttributes.wrapS);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, _textureAttributes.wrapT);
        glBindTexture(GL_TEXTURE_2D, 0);

    }
}

void GPUImage::GPUImageFrameBuffer::_generateFramebuffer() {
    PUSHSTATE()
    if (_framebuffer == NO_FRAMEBUFFER) {
        glGenFramebuffers(1, &_framebuffer);
        glBindFramebuffer(GL_FRAMEBUFFER, _framebuffer);
        _generateTexture();

        if (_texture != NO_TEXTURE) {
            glBindTexture(GL_TEXTURE_2D, _texture);
            glTexImage2D(GL_TEXTURE_2D,
                         0,
                         _textureAttributes.internalFormat,
                         static_cast<GLsizei>(_size.width),
                         static_cast<GLsizei>(_size.height),
                         0,
                         _textureAttributes.format,
                         _textureAttributes.type,
                         0);    // 生成一个 2d 纹理
            glFramebufferTexture2D(GL_FRAMEBUFFER,
                                   GL_COLOR_ATTACHMENT0,
                                   GL_TEXTURE_2D,
                                   _texture,
                                   0);  // 把纹理图像附加到帧缓冲对象
            GLenum status = glCheckFramebufferStatus(GL_FRAMEBUFFER);
            if (status != GL_FRAMEBUFFER_COMPLETE) {
                assert("glCheckFramebufferStatus error");
                _generateFramebufferSuccess = false;
            } else {
                _generateFramebufferSuccess = true;
            }
        }
        glBindTexture(GL_TEXTURE_2D, 0);
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }
    POPSTATE()
}
