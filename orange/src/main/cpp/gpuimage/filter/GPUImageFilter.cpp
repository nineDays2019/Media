//
// Created by yunrui on 2019/6/3.
//

#include "GPUImageFilter.hpp"
#include "../GPUImageUtil.hpp"
#include "../GPUImageMath.h"
#include <assert.h>

const std::string KIntPropertyType = "int";
const std::string KFloatPropertyType = "float";
const std::string KStringPropertyType = "string";
const std::string KFloatVectorPropertyType = "floatVector";
const std::string KIntVectorPropertyType = "intVector";
const std::string KImagePropertyType = "image";
const std::string KImageVectorPropertyType = "imageVector";

// Hardcode the vertex shader for standard filters, but this can be overridden
const std::string GPUImage::GPUImageFilter::kGPUImageVertexShaderString("\
      attribute vec4 position;\
      attribute vec4 inputTextureCoordinate;\
      \
      varying vec2 textureCoordinate;\
      \
      uniform mat4 modelMatrix; \
      \
      void main()\
      {\
        gl_Position = position*modelMatrix;\
        textureCoordinate = inputTextureCoordinate.xy;\
      }"
);

const std::string GPUImage::GPUImageFilter::kGPUImagePassThroughFragmentShaderString("\
       varying highp vec2 textureCoordinate;\
       \
       uniform sampler2D inputImageTexture;\
       \
       void main()\
       {\
          gl_FragColor = texture2D(inputImageTexture, textureCoordinate);\
       }");

GPUImage::GPUImageFilter::GPUImageFilter()
        : _filterFrameBuffer(NULL),
          _filterProgram(NULL),
          _inputTexture(static_cast<GLuint>(NO_TEXTURE)),
          _filterPositionAttribute(0),
          _filterTextureCoordinateAttribute(0),
          _filterInputTextureUniform(0),
          _callbackOpaque(NULL),
          _errorCallback(NULL),
          _isExtraFrameBuffer(false) {

}

GPUImage::GPUImageFilter::~GPUImageFilter() {
    destroy();
}

void
GPUImage::GPUImageFilter::initWithShaderString(const string &vertexShaderString, const string &fragmentShaderString) {
    if (_filterProgram == NULL) {
        _filterProgram = new GLProgram();
        if (_filterProgram) {
            _filterProgram->initWithShaderString(vertexShaderString, fragmentShaderString);
            _filterProgram->addAttribute("position");
            _filterProgram->addAttribute("inputTextureCoordinate");
            if (!_filterProgram->link()) {
                std::string programLog;
                _filterProgram->getProgramLog(programLog);
                if (programLog.length() > 0 && _errorCallback) {
                    _errorCallback(_callbackOpaque, GPUImageErrorProgramLink,
                                   "getProgramLog: " + programLog);
                }
                std::string fragmentLog;
                _filterProgram->getFragmentShaderLog(fragmentLog);
                if (fragmentLog.length() > 0 && _errorCallback) {
                    _errorCallback(_callbackOpaque, GPUImageErrorProgramLink,
                                   "getFragmentLog: " + programLog);
                }

                std::string vertexLog;
                _filterProgram->getVertexShaderLog(vertexLog);
                if (vertexLog.length() > 0 && _errorCallback) {
                    _errorCallback(_callbackOpaque, GPUImageErrorProgramLink,
                                   "getVertexLog: " + vertexLog);
                }

                assert("Filter shader link failed.");
                destroy();
                return;
            }

            _filterPositionAttribute = _filterProgram->getAttributeIndex("position");
            _filterTextureCoordinateAttribute = _filterProgram->getAttributeIndex("inputTextureCoordinate");
            _filterInputTextureUniform = _filterProgram->getUniformIndex("inputImageTexture");
            _filterModelMatrixUniform = _filterProgram->getUniformIndex("modelMatrix");

            _filterProgram->use();
            setModelMatrix(glm::mat4(1.0));
        }
    }
}

void GPUImage::GPUImageFilter::initWithFragmentShaderFromString(const string &fragmentShaderString) {
    initWithShaderString(kGPUImageVertexShaderString, fragmentShaderString);
}

void GPUImage::GPUImageFilter::newFrameReadyAtTime(GPUTime frameTime, GLuint textureIndex) {
    _captureTime = frameTime;

    if (_filterFrameBuffer) _filterFrameBuffer->activateFramebuffer();
    if (_filterProgram) _filterProgram->use();

    glClearColor(0, 0, 0, 0);
    glClear(GL_COLOR_BUFFER_BIT);

    if (_inputTexture != NO_TEXTURE) {
        glActiveTexture(GL_TEXTURE1);
        glBindTexture(GL_TEXTURE_2D, _inputTexture);
        glUniform1f(_filterInputTextureUniform, 1); // 对应于 GL_TEXTURE1
    } else {
        if (_errorCallback) {
            _errorCallback(_callbackOpaque, GPUImageErrorDrawNoTexture,
                           "newFrameReadyAtTime _inputTexture is NO_TEXTURE");
        }
        assert("_inputTexture is NO_TEXTURE");
        return;
    }

    static const GLfloat squareVertices[] = {
            -1.0f, -1.0f,
            1.0f, -1.0f,
            -1.0f, 1.0f,
            1.0f, 1.0f,
    };

    static GLfloat textureCoordinates[] = {
            0.0f, 0.0f,
            1.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 1.0f,
    };

    glEnableVertexAttribArray(static_cast<GLuint>(_filterPositionAttribute));
    glEnableVertexAttribArray(static_cast<GLuint>(_filterTextureCoordinateAttribute));
    glVertexAttribPointer(static_cast<GLuint>(_filterPositionAttribute),
                          2, // 每个顶点有两个分量
                          GL_FLOAT, 0, 0, squareVertices);
    glVertexAttribPointer(static_cast<GLuint>(_filterTextureCoordinateAttribute),
                          2, GL_FLOAT, 0, 0, textureCoordinates);

    newFrameReadyPre();
    glDrawArrays(GL_TRIANGLE_STRIP,
                 0,// 开始
                 4);    // 结束
    newFrameReadyAfter();

    glBindTexture(GL_TEXTURE_2D, 0);
    glDisableVertexAttribArray(static_cast<GLuint>(_filterPositionAttribute));
    glDisableVertexAttribArray(static_cast<GLuint>(_filterTextureCoordinateAttribute));

    if (_filterFrameBuffer) _filterFrameBuffer->inactivateFramebuffer();

    informTargetsAboutNewFrameAtTime();
}

void GPUImage::GPUImageFilter::setInputTexture(GLuint newInputTexture, GLuint textureIndex) {
    _inputTexture = newInputTexture;
}

int GPUImage::GPUImageFilter::nextAvailableTextureIndex() {
    return 0;
}

void GPUImage::GPUImageFilter::setInputSize(GPUSize newSize, GLuint textureIndex) {
    if (!_isExtraFrameBuffer) return;

    if (newSize.width != _outputTextureSize.width ||
        newSize.height != _outputTextureSize.height) {

        if (newSize.width == 0 || newSize.height == 0) {
            if (_errorCallback) {
                _errorCallback(
                        _callbackOpaque,
                        GPUImageErrorInputSizeZero,
                        "setInputSize newSize is zero");
            }
            assert("size is zero");
            return;
        }

        _outputTextureSize = newSize;
        if (_filterFrameBuffer) {
            _filterFrameBuffer->release();
            _filterFrameBuffer = NULL;
        }

        _filterFrameBuffer = new GPUImageFrameBuffer(newSize);
        if (!_filterFrameBuffer->getGenerateFramebufferSuccess()) {
            if (_errorCallback) {
                _errorCallback(
                        _callbackOpaque,
                        GPUImageErrorCheckFramebufferStatus,
                        "glCheckFramebufferStatus error");
            }
        }
        _outputTexture = _filterFrameBuffer->getTexture();

    }
}

void GPUImage::GPUImageFilter::setModelMatrix(glm::mat4 modelMatrix) {
    _modelMatrix = modelMatrix;
    if (_filterProgram) {
        _filterProgram->use();
        glUniformMatrix4fv(_filterModelMatrixUniform, 1, GL_FALSE, (GLfloat *) &modelMatrix);
    }
}

glm::mat4 GPUImage::GPUImageFilter::getModelMatrix() {
    return _modelMatrix;
}

void GPUImage::GPUImageFilter::newFrameReadyPre() {

}

void GPUImage::GPUImageFilter::newFrameReadyAfter() {

}

void GPUImage::GPUImageFilter::reset() {

}

void GPUImage::GPUImageFilter::removeTargetReset() {

}

void GPUImage::GPUImageFilter::setInteger(GLint newInteger, const std::string &uniformName) {
    if (_filterProgram) {
        GLint uniformIndex = _filterProgram->getUniformIndex(uniformName);
        setInteger(newInteger, uniformIndex);
    }
}

void GPUImage::GPUImageFilter::setFloat(GLfloat newFloat, const std::string &uniformName) {
    if (_filterProgram) {
        GLint uniformIndex = _filterProgram->getUniformIndex(uniformName);
        setFloat(newFloat, uniformIndex);
    }
}

void GPUImage::GPUImageFilter::setSize(GPUSize newSize, const std::string &uniformName) {
    if (_filterProgram) {
        GLint uniformIndex = _filterProgram->getUniformIndex(uniformName);
        setSize(newSize, uniformIndex);
    }
}

void GPUImage::GPUImageFilter::setPoint(GPUPoint newPoint, const std::string &uniformName) {
    if (_filterProgram) {
        GLint uniformIndex = _filterProgram->getUniformIndex(uniformName);
        setPoint(newPoint, uniformIndex);
    }
}

void GPUImage::GPUImageFilter::setFloatVec3(GPUVector3 newVec3, const std::string &uniformName) {
    if (_filterProgram) {
        GLint uniformIndex = _filterProgram->getUniformIndex(uniformName);
        setFloatVec3(newVec3, uniformIndex);
    }
}

void GPUImage::GPUImageFilter::setFloatVec4(GPUVector4 newVec4, const std::string &uniformName) {
    if (_filterProgram) {
        GLint uniformIndex = _filterProgram->getUniformIndex(uniformName);
        setFloatVec4(newVec4, uniformIndex);
    }
}

void GPUImage::GPUImageFilter::setFloatArray(GLfloat *floatArray, GLsizei count, const std::string &uniformName) {
    if (_filterProgram) {
        GLint uniformIndex = _filterProgram->getUniformIndex(uniformName);
        setFloatArray(floatArray, count, uniformIndex);
    }
}

void GPUImage::GPUImageFilter::setInteger(GLint intValue, GLint uniform) {
    if (_filterProgram) {
        _filterProgram->use();
        glUniform1i(uniform, intValue);
    }
}

void GPUImage::GPUImageFilter::setFloat(GLfloat floatValue, GLint uniform) {
    if (_filterProgram) {
        _filterProgram->use();
        glUniform1f(uniform, floatValue);
    }
}

void GPUImage::GPUImageFilter::setSize(GPUSize newSize, GLint uniform) {
    if (_filterProgram) {
        _filterProgram->use();
        GLfloat sizeUniform[2];
        sizeUniform[0] = newSize.width;
        sizeUniform[1] = newSize.height;
        glUniform2fv(uniform, 1, sizeUniform);
    }
}

void GPUImage::GPUImageFilter::setPoint(GPUPoint newPoint, GLint uniform) {
    if (_filterProgram) {
        _filterProgram->use();
        GLfloat pointUniform[2];
        pointUniform[0] = newPoint.x;
        pointUniform[1] = newPoint.y;
        glUniform2fv(uniform, 1, pointUniform);
    }
}

void GPUImage::GPUImageFilter::setFloatVec3(GPUVector3 newVec3, GLint uniform) {
    if (_filterProgram) {
        _filterProgram->use();
        glUniform3fv(uniform, 1, (GLfloat *) &newVec3);
    }
}

void GPUImage::GPUImageFilter::setFloatVec4(GPUVector4 newVec4, GLint uniform) {
    if (_filterProgram) {
        _filterProgram->use();
        glUniform4fv(uniform, 1, (GLfloat *) &newVec4);
    }
}

void GPUImage::GPUImageFilter::setFloatArray(GLfloat *arrayValue, GLsizei arrayLength, GLint uniform) {
    if (_filterProgram) {
        _filterProgram->use();
        glUniform1fv(uniform, arrayLength, arrayValue);
    }
}

bool GPUImage::GPUImageFilter::registerIntProperty(const std::string &name, int defaultValue, const string &comment,
                                                   function<void(int &)> setCallback) {
    if (hasProperty(name)) {
        if (_errorCallback) {
            _errorCallback(_callbackOpaque, GPUImageErrorRegisterPropertyRepeat,
                           "registerIntProperty " + name + " repeat");
        }
        assert("registerIntProperty hasProperty name");
        return false;
    }
    IntProperty property;
    property.type = KIntPropertyType;
    property.value = defaultValue;
    property.comment = comment;
    if (setCallback) {
        property.setCallback = setCallback;
    }
    _intProperties[name] = property;
    return true;
}

bool GPUImage::GPUImageFilter::registerFloatProperty(const std::string &name, float defaultValue, const string &comment,
                                                     function<void(float &)> setCallback) {
    if (hasProperty(name)) {
        if (_errorCallback) {
            _errorCallback(_callbackOpaque, GPUImageErrorRegisterPropertyRepeat,
                           "registerFloatProperty " + name + " repeat");
        }
        assert("registerFloatProperty hasProperty name");
        return false;
    }
    FloatProperty property;
    property.type = KFloatPropertyType;
    property.value = defaultValue;
    property.comment = comment;
    if (setCallback) {
        property.setCallback = setCallback;
    }
    _floatProperties[name] = property;
    return true;
}

bool GPUImage::GPUImageFilter::registerStringProperty(const std::string &name, const std::string &defaultValue,
                                                      const string &comment, function<void(string &)> setCallback) {
    if (hasProperty(name)) {
        if (_errorCallback) {
            _errorCallback(_callbackOpaque, GPUImageErrorRegisterPropertyRepeat,
                           "registerStringProperty " + name + " repeat");
        }
        assert("registerStringProperty hasProperty name");
        return false;
    }
    StringProperty property;
    property.type = KStringPropertyType;
    property.value = defaultValue;
    property.comment = comment;
    if (setCallback) {
        property.setCallback = setCallback;
    }
    _stringProperties[name] = property;
    return true;
}

bool
GPUImage::GPUImageFilter::registerFloatVectorProperty(const std::string &name, const std::vector<float> &defaultValue,
                                                      const string &comment,
                                                      function<void(vector<float> &)> setCallback) {
    if (hasProperty(name)) {
        if (_errorCallback) {
            _errorCallback(_callbackOpaque, GPUImageErrorRegisterPropertyRepeat,
                           "registerFloatVectorProperty " + name + " repeat");
        }
        assert("registerFloatVectorProperty hasProperty name");
        return false;
    }
    FloatVectorProperty property;
    property.type = KFloatVectorPropertyType;
    property.value = defaultValue;
    property.comment = comment;
    if (setCallback) {
        property.setCallback = setCallback;
    }
    _floatVectorProperties[name] = property;
    return true;
}

bool GPUImage::GPUImageFilter::registerIntVectorProperty(const std::string &name, const std::vector<int> &defaultValue,
                                                         const string &comment,
                                                         function<void(vector<int> &)> setCallback) {
    if (hasProperty(name)) {
        if (_errorCallback) {
            _errorCallback(_callbackOpaque, GPUImageErrorRegisterPropertyRepeat,
                           "registerIntVectorProperty " + name + " repeat");
        }
        assert("registerIntVectorProperty hasProperty name");
        return false;
    }
    IntVectorProperty property;
    property.type = KIntVectorPropertyType;
    property.value = defaultValue;
    property.comment = comment;
    if (setCallback) {
        property.setCallback = setCallback;
    }
    _intVectorProperties[name] = property;
    return true;
}

bool GPUImage::GPUImageFilter::setIntProperty(const std::string &name, int value) {
    Property *rawProperty = _getProperty(name);
    if (!rawProperty) {
        if (_errorCallback) {
            _errorCallback(_callbackOpaque, GPUImageErrorSetPropertyFail, "setIntProperty " + name + " fail");
        }
        assert("setIntProperty rawProperty nil");
        return false;
    } else if (rawProperty->type != KIntPropertyType) {
        return false;
    }
    IntProperty *property = ((IntProperty *) rawProperty);
    property->value = value;
    if (property->setCallback)
        property->setCallback(value);
    return true;
}

bool GPUImage::GPUImageFilter::setFloatProperty(const std::string &name, float value) {
    Property *rawProperty = _getProperty(name);
    if (!rawProperty) {
        if (_errorCallback) {
            _errorCallback(_callbackOpaque, GPUImageErrorSetPropertyFail, "setFloatProperty " + name + " fail");
        }
        assert("setFloatProperty rawProperty nil");
        return false;
    } else if (rawProperty->type != KFloatPropertyType) {
        return false;
    }
    FloatProperty *property = ((FloatProperty *) rawProperty);
    if (property->setCallback)
        property->setCallback(value);
    property->value = value;
    return true;
}

bool GPUImage::GPUImageFilter::setStringProperty(const std::string &name, std::string value) {
    Property *rawProperty = _getProperty(name);
    if (!rawProperty) {
        if (_errorCallback) {
            _errorCallback(_callbackOpaque, GPUImageErrorSetPropertyFail, "setStringProperty " + name + " fail");
        }
        assert("setStringProperty rawProperty nil");
        return false;
    } else if (rawProperty->type != KStringPropertyType) {
        return false;
    }
    StringProperty *property = ((StringProperty *) rawProperty);
    property->value = value;
    if (property->setCallback)
        property->setCallback(value);
    return true;
}

bool GPUImage::GPUImageFilter::setFloatVectorProperty(const std::string &name, std::vector<float> &value) {
    Property *rawProperty = _getProperty(name);
    if (!rawProperty) {
        if (_errorCallback) {
            _errorCallback(_callbackOpaque, GPUImageErrorSetPropertyFail, "setFloatVectorProperty " + name + " fail");
        }
        assert("setFloatVectorProperty rawProperty nil");
        return false;
    } else if (rawProperty->type != KFloatVectorPropertyType) {
        return false;
    }
    FloatVectorProperty *property = ((FloatVectorProperty *) rawProperty);
    std::vector<float> vectorValues;
    for (int i = 0; i < value.size(); i++) {
        vectorValues.push_back(value[i]);
    }
    property->value = vectorValues;
    if (property->setCallback)
        property->setCallback(value);
    return true;
}

bool GPUImage::GPUImageFilter::setIntVectorProperty(const std::string &name, std::vector<int> &value) {
    Property *rawProperty = _getProperty(name);
    if (!rawProperty) {
        if (_errorCallback) {
            _errorCallback(_callbackOpaque, GPUImageErrorSetPropertyFail, "setIntVectorProperty " + name + " fail");
        }
        assert("setIntVectorProperty rawProperty nil");
        return false;
    } else if (rawProperty->type != KIntVectorPropertyType) {
        return false;
    }
    IntVectorProperty *property = ((IntVectorProperty *) rawProperty);
    std::vector<int> vectorValues;
    for (int i = 0; i < value.size(); i++) {
        vectorValues.push_back(value[i]);
    }
    property->value = vectorValues;
    if (property->setCallback)
        property->setCallback(value);
    return true;
}

bool GPUImage::GPUImageFilter::getIntProperty(const std::string &name, int &retValue) {
    Property *property = _getProperty(name);
    if (!property) {
        if (_errorCallback) {
            _errorCallback(_callbackOpaque, GPUImageErrorGetPropertyFail, "getIntProperty " + name + " fail");
        }
        assert("getIntProperty rawProperty nil");
        return false;
    }
    retValue = ((IntProperty *) property)->value;
    return true;
}

bool GPUImage::GPUImageFilter::getFloatProperty(const std::string &name, float &retValue) {
    Property *property = _getProperty(name);
    if (!property) {
        if (_errorCallback) {
            _errorCallback(_callbackOpaque, GPUImageErrorGetPropertyFail, "getFloatProperty " + name + " fail");
        }
        assert("getFloatProperty rawProperty nil");
        return false;
    }
    retValue = ((FloatProperty *) property)->value;
    return true;
}

bool GPUImage::GPUImageFilter::getStringProperty(const std::string &name, std::string &retValue) {
    Property *property = _getProperty(name);
    if (!property) {
        if (_errorCallback) {
            _errorCallback(_callbackOpaque, GPUImageErrorGetPropertyFail, "getStringProperty " + name + " fail");
        }
        assert("getStringProperty rawProperty nil");
        return false;
    }
    retValue = ((StringProperty *) property)->value;
    return true;
}

bool GPUImage::GPUImageFilter::getFloatVectorProperty(const std::string &name, std::vector<float> &retValue) {
    Property *property = _getProperty(name);
    if (!property) {
        if (_errorCallback) {
            _errorCallback(_callbackOpaque, GPUImageErrorGetPropertyFail, "getFloatVectorProperty " + name + " fail");
        }
        assert("getFloatVectorProperty rawProperty nil");
        return false;
    }
    retValue = ((FloatVectorProperty *) property)->value;
    return true;
}

bool GPUImage::GPUImageFilter::getIntVectorProperty(const std::string &name, std::vector<int> &retValue) {
    Property *property = _getProperty(name);
    if (!property) {
        if (_errorCallback) {
            _errorCallback(_callbackOpaque, GPUImageErrorGetPropertyFail, "getIntVectorProperty " + name + " fail");
        }
        assert("getIntVectorProperty rawProperty nil");
        return false;
    }
    retValue = ((IntVectorProperty *) property)->value;
    return true;
}

bool GPUImage::GPUImageFilter::hasProperty(const std::string &name) {
    Property *property = _getProperty(name);
    return property && property->type == name;
}

bool GPUImage::GPUImageFilter::hasProperty(const std::string &name, const std::string type) {
    return _getProperty(name) != nullptr;
}

bool GPUImage::GPUImageFilter::getPropertyComment(const std::string &name, std::string &retComment) {
    Property *property = _getProperty(name);
    if (!property) return false;
    retComment = std::string("[") + property->type + "] " + property->comment;
    return true;
}

void GPUImage::GPUImageFilter::setErrorCallback(void *opaque, gpuimage_error_callback callback) {
    _callbackOpaque = opaque;
    _errorCallback = callback;
}

bool GPUImage::GPUImageFilter::getPropertyType(const std::string &name, std::string &retType) {
    Property *property = _getProperty(name);
    if (!property) return false;
    retType = property->type;
    return true;
}

const std::string GPUImage::GPUImageFilter::getVertexShaderString() {
    return kGPUImageVertexShaderString;
}

const std::string GPUImage::GPUImageFilter::getFragmentShaderString() {
    return kGPUImagePassThroughFragmentShaderString;
}

GPUImage::GPUImageFrameBuffer *GPUImage::GPUImageFilter::outputFrameBuffer() {
    return _filterFrameBuffer;
}

void GPUImage::GPUImageFilter::setOutputFrameBuffer(GPUImage::GPUImageFrameBuffer *frameBuffer) {
    if (!frameBuffer || frameBuffer == _filterFrameBuffer) return;
    if (!_isExtraFrameBuffer && _filterFrameBuffer) {
        _filterFrameBuffer->release();
        _filterFrameBuffer = NULL;
    }
    _filterFrameBuffer = frameBuffer;
    _isExtraFrameBuffer = true;
    _outputTexture = _filterFrameBuffer->getTexture();
    _outputTextureSize = _filterFrameBuffer->getSize();
}

GPUImage::GPUImageFilter::Property *GPUImage::GPUImageFilter::_getProperty(const string &name) {
    if (_intProperties.find(name) != _intProperties.end()) {
        return &_intProperties[name];
    }
    if (_floatProperties.find(name) != _floatProperties.end()) {
        return &_floatProperties[name];
    }
    if (_stringProperties.find(name) != _stringProperties.end()) {
        return &_stringProperties[name];
    }
    if (_floatVectorProperties.find(name) != _floatVectorProperties.end()) {
        return &_floatVectorProperties[name];
    }
    if (_intVectorProperties.find(name) != _intVectorProperties.end()) {
        return &_intVectorProperties[name];
    }
    return 0;
}

void GPUImage::GPUImageFilter::destroy() {
    if (_filterProgram) {
        _filterProgram->release();
        _filterProgram = NULL;
    }

    if (!_isExtraFrameBuffer && _filterFrameBuffer) {
        _filterFrameBuffer->release();
        _filterFrameBuffer = NULL;
    }

    _floatProperties.clear();
    _intProperties.clear();
    _stringProperties.clear();
    _intVectorProperties.clear();
    _floatVectorProperties.clear();
}
