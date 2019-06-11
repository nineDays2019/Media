//
// Created by yunrui on 2019/6/11.
//

#include "GPUFrameBuffer.hpp"
#include <assert.h>


#define PUSHSTATE() GLint restoreId; glGetIntegerv( GL_FRAMEBUFFER_BINDING, &restoreId );
#define POPSTATE() glBindFramebuffer( GL_FRAMEBUFFER, restoreId );

GPUImage::TextureOptions GPUImage::GPUFrameBuffer::defaultTextureOptions = {
        .minFilter = GL_LINEAR,
        .magFilter = GL_LINEAR,
        .wrapS = GL_CLAMP_TO_EDGE,
        .wrapT = GL_CLAMP_TO_EDGE,
        .internalFormat = GL_RGBA,
        .format = GL_RGBA,
        .type = GL_UNSIGNED_BYTE
};

GPUImage::GPUFrameBuffer::GPUFrameBuffer() :
        _size(kGPUSizeZero),
        _textureOptions(defaultTextureOptions),
        _texture(static_cast<GLuint>(NO_TEXTURE)),
        _framebuffer(static_cast<GLuint>(NO_FRAMEBUFFER)),
        _generateFramebufferSuccess(false) {
    generateFramebuffer();
}

GPUImage::GPUFrameBuffer::GPUFrameBuffer(GPUSize size, GPUImage::TextureOptions options)
        : _size(size),
          _textureOptions(options),
          _texture(static_cast<GLuint>(NO_TEXTURE)),
          _framebuffer(static_cast<GLuint>(NO_FRAMEBUFFER)),
          _generateFramebufferSuccess(false) {
    generateFramebuffer();
}

GPUImage::GPUFrameBuffer::~GPUFrameBuffer() {
    destroy();
}

GLuint GPUImage::GPUFrameBuffer::getTexture() {
    return _texture;
}

GPUSize GPUImage::GPUFrameBuffer::getSize() {
    return _size;
}

GPUImage::TextureOptions GPUImage::GPUFrameBuffer::getTextureOptions() {
    return _textureOptions;
}

void GPUImage::GPUFrameBuffer::activateFramebuffer() {
    if (_framebuffer != NO_FRAMEBUFFER) {
        glBindFramebuffer(GL_FRAMEBUFFER, _framebuffer);
        glViewport(0, 0,
                   static_cast<GLsizei>(_size.width),
                   static_cast<GLsizei>(_size.height));
    }
}

void GPUImage::GPUFrameBuffer::inactivateFramebuffer() {
    glBindFramebuffer(GL_FRAMEBUFFER, 0);
}

void GPUImage::GPUFrameBuffer::destroy() {
    if (_framebuffer != NO_FRAMEBUFFER) {
        glDeleteFramebuffers(1, &_framebuffer);
        _framebuffer = static_cast<GLuint>(NO_FRAMEBUFFER);
    }

    if (_texture != NO_TEXTURE) {
        glDeleteTextures(1, &_texture);
        _texture = static_cast<GLuint>(NO_TEXTURE);
    }
}

void GPUImage::GPUFrameBuffer::generateTexture() {
    if (_texture == NO_TEXTURE) {
        glGenTextures(1, &_texture);
        glBindTexture(GL_TEXTURE_2D, _texture);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, _textureOptions.minFilter);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, _textureOptions.magFilter);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, _textureOptions.wrapS);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, _textureOptions.wrapT);
        glBindTexture(GL_TEXTURE_2D, 0);
    }
}

void GPUImage::GPUFrameBuffer::generateFramebuffer() {
    PUSHSTATE();
    if (_framebuffer == NO_FRAMEBUFFER) {
        glGenFramebuffers(1, &_framebuffer);
        glBindFramebuffer(GL_FRAMEBUFFER, _framebuffer);
        generateTexture();
        if (_texture != NO_TEXTURE) {
            glBindTexture(GL_TEXTURE_2D, _texture);
            glTexImage2D(GL_TEXTURE_2D, 0,
                         _textureOptions.internalFormat,
                         static_cast<GLsizei>(_size.width),
                         static_cast<GLsizei>(_size.height),
                         0,
                         _textureOptions.format,
                         _textureOptions.type, 0);
            glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, _texture, 0);
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
    POPSTATE();
}

bool GPUImage::GPUFrameBuffer::getGenerateFramebufferSuccess() {
    return _generateFramebufferSuccess;
}
