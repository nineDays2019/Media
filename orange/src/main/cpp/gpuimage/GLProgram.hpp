//
// Created by yunrui on 2019/6/3.
//

#ifndef LEGEND_GLPROGRAM_HPP
#define LEGEND_GLPROGRAM_HPP

#include <stdio.h>
#include <string>
#include <vector>
#include "GPUImageMacros.h"
#include "GPUObject.hpp"

using namespace std;

NS_GI_BEGIN

    typedef void (GL_APIENTRY *GLInfoFunction)(GLuint program, GLenum pname, GLint *params);

    typedef void (GL_APIENTRY *GLLogFunction)(GLuint program, GLsizei bufsize, GLsizei *length, GLchar *infolog);

    class GLProgram : public GPUObject {
    public:
        GLProgram();

        ~GLProgram();

        bool initWithShaderString(const string &vertexShaderString,
                                  const string &fragmentShaderString);

        bool compileShader(GLuint &shader, GLenum type, const string &shaderString);

        void addAttribute(const string &attributeName);

        GLuint getAttributeIndex(const string &attributeName);

        GLuint getUniformIndex(const string &uniformName);

        bool link();

        void use();

        void validate();

        void destroy();

        void logForOpenGLObject(string &log, GLuint object,
                                GLInfoFunction infoFunction, GLLogFunction logFunction);

        void getVertexShaderLog(string &log);

        void getFragmentShaderLog(string &log);

        void getProgramLog(string &log);

        bool isInitialized() const;

    private:
        bool _initialized;
        GLuint _program;
        GLuint _vertexShader;
        GLuint _fragmentShader;
        vector<string> _attributes;
        vector<string> _uniforms;
    };

NS_GI_END

#endif //LEGEND_GLPROGRAM_HPP
