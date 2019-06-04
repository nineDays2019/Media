//
// Created by yunrui on 2019/6/3.
//

#include "GLProgram.hpp"

NS_GI_BEGIN

    GPUImage::GLProgram::GLProgram() :
            _initialized(false),
            _program(0),
            _vertexShader(0),
            _fragmentShader(0) {
    }

    GPUImage::GLProgram::~GLProgram() {
        destroy();
    }

    bool
    GPUImage::GLProgram::initWithShaderString(const string &vertexShaderString, const string &fragmentShaderString) {
        _initialized = false;
        _attributes.clear();
        _uniforms.clear();
        _program = glCreateProgram();
        if (!compileShader(_vertexShader, GL_VERTEX_SHADER, vertexShaderString)) {
            return false;
        }
        if (!compileShader(_fragmentShader, GL_FRAGMENT_SHADER, fragmentShaderString)) {
            return false;
        }
        glAttachShader(_program, _vertexShader);
        glAttachShader(_program, _fragmentShader);
        return true;
    }

    bool GPUImage::GLProgram::compileShader(GLuint &shader, GLenum type, const string &shaderString) {
        GLint status;
        const GLchar *source = shaderString.c_str();
        if (!source) {
            return false;
        }
        shader = glCreateShader(type);
        glShaderSource(shader, 1, &source, NULL);
        glCompileShader(shader);
        glGetShaderiv(shader, GL_COMPILE_STATUS, &status);

        if (status != GL_TRUE) {
            GLint logLength;
            glGetShaderiv(shader, GL_INFO_LOG_LENGTH, &logLength);
            if (logLength > 0) {
                GLchar *log = new GLchar[logLength];
                glGetShaderInfoLog(shader, logLength, &logLength, log);
                LOGE("Shader compile log:\n%s", log);
                delete[] log;
            }
        }
        return status == GL_TRUE;
    }

    void GPUImage::GLProgram::addAttribute(const string &attributeName) {
        vector<string>::const_iterator found = find(_attributes.begin(),
                                                    _attributes.end(),
                                                    attributeName);
        if (found == _attributes.end()) {
            _attributes.push_back(attributeName);
            size_t indexOfAttribute = _attributes.size() - 1;
            glBindAttribLocation(_program, indexOfAttribute, attributeName.c_str());
        }
    }

    GLuint GPUImage::GLProgram::getAttributeIndex(const string &attributeName) {
        vector<string>::const_iterator found = find(_attributes.begin(),
                                                    _attributes.end(),
                                                    attributeName);
        return found - _attributes.begin();
    }

    GLuint GPUImage::GLProgram::getUniformIndex(const string &uniformName) {
        return static_cast<GLuint>(glGetUniformLocation(_program, uniformName.c_str()));
    }

    bool GPUImage::GLProgram::link() {
        GLint status;
        glLinkProgram(_program);
        glGetProgramiv(_program, GL_LINK_STATUS, &status);
        if (status == GL_FALSE) {
            return false;
        }
        if (_vertexShader) {
            glDeleteShader(_vertexShader);
            _vertexShader = 0;
        }
        if (_fragmentShader) {
            glDeleteShader(_fragmentShader);
            _fragmentShader = 0;
        }
        _initialized = true;
        return false;
    }

    void GPUImage::GLProgram::use() {
        glUseProgram(_program);
    }

    void GPUImage::GLProgram::validate() {
        GLint logLength;

        glValidateProgram(_program);
        glGetProgramiv(_program, GL_INFO_LOG_LENGTH, &logLength);
        if (logLength > 0) {
            GLchar *log = new GLchar[logLength];
            glGetProgramInfoLog(_program, logLength, &logLength, log);
            LOGE("program validate log:%s", log);
            delete[] log;
        }
    }

    void GPUImage::GLProgram::destroy() {
        if (_vertexShader) {
            glDeleteShader(_vertexShader);
        }
        if (_fragmentShader) {
            glDeleteShader(_fragmentShader);
        }
        if (_program) {
            glDeleteProgram(_program);
        }
    }

    void GPUImage::GLProgram::logForOpenGLObject(string &log, GLuint object, GPUImage::GLInfoFunction infoFunction,
                                                 GPUImage::GLLogFunction logFunction) {
        GLint logLength = 0, charsWritten = 0;

        infoFunction(object, GL_INFO_LOG_LENGTH, &logLength);
        if (logLength < 1)
            return;

        GLchar *logBytes = new GLchar[logLength];
        logFunction(object, logLength, &charsWritten, logBytes);
        log = std::string(logBytes, static_cast<unsigned int>(logLength));
        delete[] logBytes;
    }

    void GPUImage::GLProgram::getVertexShaderLog(string &log) {
        logForOpenGLObject(log, _vertexShader,
                           (GLInfoFunction) &glGetProgramiv,
                           (GLLogFunction) &glGetProgramInfoLog);
    }

    void GPUImage::GLProgram::getFragmentShaderLog(string &log) {
        logForOpenGLObject(log, _fragmentShader,
                           (GLInfoFunction) &glGetProgramiv,
                           (GLLogFunction) &glGetProgramInfoLog);
    }

    void GPUImage::GLProgram::getProgramLog(string &log) {
        logForOpenGLObject(log, _program,
                           (GLInfoFunction) &glGetProgramiv,
                           (GLLogFunction) &glGetProgramInfoLog);
    }

    bool GPUImage::GLProgram::isInitialized() const {
        return _initialized;
    }

NS_GI_END
