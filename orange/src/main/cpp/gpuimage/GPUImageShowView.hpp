//
// Created by yunrui on 2019/6/11.
//

#ifndef LEGEND_GPUIMAGESHOWVIEW_HPP
#define LEGEND_GPUIMAGESHOWVIEW_HPP

#include "GPUObject.hpp"
#include "GLProgram.hpp"
#include "GPUFrameBuffer.hpp"

NS_GI_BEGIN

    // 默认的 2D 着色器
    const std::string kDefaultVertexShader = ""
                                             "attribute vec4 position;"
                                             "attribute vec4 texCoord;"
                                             "varying vec2 vTexCoord;"
                                             "void main()"
                                             "{"
                                             "gl_Position = position;"
                                             "vTexCoord = texCoord.xy;"
                                             "}";

    const std::string kDefaultFragmentShader = ""
                                               "varying highp vec2 vTexCoord;"
                                               "uniform sampler2D colorMap;"

                                               "void main()"
                                               "{"
                                               "gl_FragColor = texture2D(colorMap, vTexCoord);"
                                               "}";

    enum RotationMode {
        NoRotation = 0,
        RotateLeft,//向左旋转
        RotateRight,//向右旋转
        FlipVertical,//垂直翻转
        FlipHorizontal,//水平翻转
        RotateRightFlipVertical,//向右旋转垂直翻转
        RotateRightFlipHorizontal,//向右旋转水平旋转
        Rotate180//旋转180度
    };

    class GPUImageShowView : public GPUObject {
    public:
        GPUImageShowView();

        ~GPUImageShowView();

        void release();

        void init();

        void setFrameBuffer(GPUFrameBuffer *wbgpuFrameBuffer);

        void setSizeChanged(int width, int height);

        void updata(float time, GLuint texture, int rotationMode);

    private:
        int _width;
        int _height;
        GLProgram *_displayProgram;
        GLuint _positionAttributeLocation;
        GLuint _textureCoordinateAttributeLocation;
        GLuint _colorMapUniformLocation;
        GLfloat _displayVertices[8];
        GPUFrameBuffer *_gpuFrameBuffer;

        void _updataDisplayVertices();

        const GLfloat *_getTextureCoordinate(RotationMode rotationMode);
    };

NS_GI_END

#endif //LEGEND_GPUIMAGESHOWVIEW_HPP
