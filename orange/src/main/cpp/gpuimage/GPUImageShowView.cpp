//
// Created by yunrui on 2019/6/11.
//

#include "GPUImageShowView.hpp"

GPUImage::GPUImageShowView::GPUImageShowView() :
        _width(0),
        _height(0),
        _positionAttributeLocation(0),
        _textureCoordinateAttributeLocation(0),
        _colorMapUniformLocation(0),
        _gpuFrameBuffer(0) {
}

GPUImage::GPUImageShowView::~GPUImageShowView() {

}

void GPUImage::GPUImageShowView::release() {
    if (_displayProgram) {
        delete _displayProgram;
        _displayProgram = 0;
    }
}

void GPUImage::GPUImageShowView::init() {
    _displayProgram = new GLProgram();
    _displayProgram->initWithShaderString(kDefaultVertexShader, kDefaultFragmentShader);
    _displayProgram->link();


    _displayProgram->addAttribute("position");
    _displayProgram->addAttribute("texCoord");

    _positionAttributeLocation = _displayProgram->getAttributeIndex("position");
    _textureCoordinateAttributeLocation = _displayProgram->getAttributeIndex("texCoord");
    _colorMapUniformLocation = _displayProgram->getUniformIndex("colorMap");
    _displayProgram->use();
    glEnableVertexAttribArray(_positionAttributeLocation);
    glEnableVertexAttribArray(_textureCoordinateAttributeLocation);
}

void GPUImage::GPUImageShowView::setFrameBuffer(GPUImage::GPUFrameBuffer *wbgpuFrameBuffer) {
    _gpuFrameBuffer = wbgpuFrameBuffer;
}

void GPUImage::GPUImageShowView::setSizeChanged(int width, int height) {
    _width = width;
    _height = height;
}

void GPUImage::GPUImageShowView::updata(float time, GLuint texture, int rotationMode) {

    static const GLfloat squareVertices[] = {
            -1.0f, -1.0f,
            1.0f, -1.0f,
            -1.0f, 1.0f,
            1.0f, 1.0f,
    };


    glBindBuffer(GL_FRAMEBUFFER, 0);
    if (_displayProgram == NULL)
        return;
    _displayProgram->use();
    glClearColor(0, 0, 0, 1);

    glActiveTexture(GL_TEXTURE0);
    glBindTexture(GL_TEXTURE_2D, texture);

    glVertexAttribPointer(_positionAttributeLocation, 2, GL_FLOAT, GL_FALSE, 0, squareVertices);
    glVertexAttribPointer(_textureCoordinateAttributeLocation, 2, GL_FLOAT, GL_FALSE, 0,
                          _getTextureCoordinate((RotationMode) rotationMode));
    glEnableVertexAttribArray(_positionAttributeLocation);
    glEnableVertexAttribArray(_textureCoordinateAttributeLocation);
    glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
    glBindTexture(GL_TEXTURE_2D, 0);
    glUseProgram(0);
}

void GPUImage::GPUImageShowView::_updataDisplayVertices() {

}

const GLfloat *GPUImage::GPUImageShowView::_getTextureCoordinate(GPUImage::RotationMode rotationMode) {
    static const GLfloat noRotationTextureCoordinates[] = {
            0.0f, 0.0f,
            1.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 1.0f,
    };

    static const GLfloat rotateRightTextureCoordinates[] = {
            1.0f, 1.0f,
            1.0f, 0.0f,
            0.0f, 1.0f,
            0.0f, 0.0f,
    };

    static const GLfloat rotateLeftTextureCoordinates[] = {
            0.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 0.0f,
            1.0f, 1.0f,
    };

    static const GLfloat verticalFlipTextureCoordinates[] = {
            0.0f, 1.0f,
            1.0f, 1.0f,
            0.0f, 0.0f,
            1.0f, 0.0f,
    };

    static const GLfloat horizontalFlipTextureCoordinates[] = {
            1.0f, 1.0f,
            0.0f, 1.0f,
            1.0f, 0.0f,
            0.0f, 0.0f,
    };

    static const GLfloat rotateRightVerticalFlipTextureCoordinates[] = {
            1.0f, 0.0f,
            1.0f, 1.0f,
            0.0f, 0.0f,
            0.0f, 1.0f,
    };

    static const GLfloat rotateRightHorizontalFlipTextureCoordinates[] = {
            0.0f, 1.0f,
            0.0f, 0.0f,
            1.0f, 1.0f,
            1.0f, 0.0f,
    };

    static const GLfloat rotate180TextureCoordinates[] = {
            1.0f, 0.0f,
            0.0f, 0.0f,
            1.0f, 1.0f,
            0.0f, 1.0f,
    };

    switch (rotationMode) {
        case NoRotation:
            return noRotationTextureCoordinates;
        case RotateLeft:
            return rotateLeftTextureCoordinates;
        case RotateRight:
            return rotateRightTextureCoordinates;
        case FlipVertical:
            return verticalFlipTextureCoordinates;
        case FlipHorizontal:
            return horizontalFlipTextureCoordinates;
        case RotateRightFlipVertical:
            return rotateRightVerticalFlipTextureCoordinates;
        case RotateRightFlipHorizontal:
            return rotateRightHorizontalFlipTextureCoordinates;
        case Rotate180:
            return rotate180TextureCoordinates;
    }
}
