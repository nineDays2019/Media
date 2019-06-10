//
// Created by yunrui on 2019/6/3.
//

#ifndef LEGEND_WBGPUIMAGEFILTER_HPP
#define LEGEND_WBGPUIMAGEFILTER_HPP

#include <stdio.h>
#include <functional>
#include <map>
#include <vector>

#include "../GPUImageMacros.h"
#include "../GPUImageInput.hpp"
#include "../GPUImageOutput.hpp"
#include "../GPUImageFrameBuffer.hpp"
#include "../GLProgram.hpp"

using namespace std;

extern const string KIntPropertyType;
extern const string KFloatPropertyType;
extern const string KStringPropertyType;
extern const string KFloatVectorPropertyType;
extern const string KIntVectorPropertyType;
extern const string KImagePropertyType;
extern const string KImageVectorPropertyType;

typedef enum {
    GPUImageErrorProgramLink = 0,
    GPUImageErrorCheckFramebufferStatus = 1,
    GPUImageErrorDrawNoTexture = 2,
    GPUImageErrorInputSizeZero = 3,
    GPUImageErrorRegisterPropertyRepeat = 4,
    GPUImageErrorSetPropertyFail = 5,
    GPUImageErrorGetPropertyFail = 6,
} GPUImageErrorCode;

typedef void (*gpuimage_error_callback)(void *opaque, GPUImageErrorCode code, const string &message);

NS_GI_BEGIN

    class GPUImageFilter : public GPUImageInput, public GPUImageOutput {
    public:
        GPUImageFilter();

        ~GPUImageFilter();

        virtual void initWithShaderString(
                const string &vertexShaderString,
                const string &fragmentShaderString);

        virtual void initWithFragmentShaderFromString(const string &fragmentShaderString);

        // WBGPUImageInput protocol
        void newFrameReadyAtTime(GPUTime frameTime, GLuint textureIndex);

        void setInputTexture(GLuint newInputTexture, GLuint textureIndex);

        int nextAvailableTextureIndex();

        void setInputSize(GPUSize newSize, GLuint textureIndex);

        void setModelMatrix(glm::mat4 modelMatrix);

        glm::mat4 getModelMatrix();

        void newFrameReadyPre();

        void newFrameReadyAfter();

        void reset();

        void removeTargetReset();


        /// @name Input parameters
        void setInteger(GLint newInteger, const std::string &uniformName);

        void setFloat(GLfloat newFloat, const std::string &uniformName);

        void setSize(GPUSize newSize, const std::string &uniformName);

        void setPoint(GPUPoint newPoint, const std::string &uniformName);

        void setFloatVec3(GPUVector3 newVec3, const std::string &uniformName);

        void setFloatVec4(GPUVector4 newVec4, const std::string &uniformName);

        void setFloatArray(GLfloat *floatArray, GLsizei count, const std::string &uniformName);


        void setInteger(GLint intValue, GLint uniform);

        void setFloat(GLfloat floatValue, GLint uniform);

        void setSize(GPUSize newSize, GLint uniform);

        void setPoint(GPUPoint newPoint, GLint uniform);

        void setFloatVec3(GPUVector3 newVec3, GLint uniform);

        void setFloatVec4(GPUVector4 newVec4, GLint uniform);

        void setFloatArray(GLfloat *arrayValue, GLsizei arrayLength, GLint uniform);


        bool registerIntProperty(const std::string &name, int defaultValue, const std::string &comment = "",
                                 std::function<void(int &)> setCallback = 0);

        bool registerFloatProperty(const std::string &name, float defaultValue, const std::string &comment = "",
                                   std::function<void(float &)> setCallback = 0);

        bool registerStringProperty(const std::string &name, const std::string &defaultValue,
                                    const std::string &comment = "",
                                    std::function<void(std::string &)> setCallback = 0);

        bool registerFloatVectorProperty(const std::string &name, const std::vector<float> &defaultValue,
                                         const std::string &comment = "",
                                         std::function<void(std::vector<float> &)> setCallback = 0);

        bool registerIntVectorProperty(const std::string &name, const std::vector<int> &defaultValue,
                                       const std::string &comment = "",
                                       std::function<void(std::vector<int> &)> setCallback = 0);

        bool setIntProperty(const std::string &name, int value);

        bool setFloatProperty(const std::string &name, float value);

        bool setStringProperty(const std::string &name, std::string value);

        bool setFloatVectorProperty(const std::string &name, std::vector<float> &value);

        bool setIntVectorProperty(const std::string &name, std::vector<int> &value);

        bool getIntProperty(const std::string &name, int &retValue);

        bool getFloatProperty(const std::string &name, float &retValue);

        bool getStringProperty(const std::string &name, std::string &retValue);

        bool getFloatVectorProperty(const std::string &name, std::vector<float> &retValue);

        bool getIntVectorProperty(const std::string &name, std::vector<int> &retValue);

        bool hasProperty(const std::string &name);

        bool hasProperty(const std::string &name, const std::string type);

        bool getPropertyComment(const std::string &name, std::string &retComment);

        bool getPropertyType(const std::string &name, std::string &retType);

        void setErrorCallback(void *opaque, gpuimage_error_callback callback);


        /// shader string
        static const std::string kGPUImageVertexShaderString;
        static const std::string kGPUImagePassThroughFragmentShaderString;

        virtual const std::string getVertexShaderString();

        virtual const std::string getFragmentShaderString();

        GPUImageFrameBuffer *outputFrameBuffer();

        void setOutputFrameBuffer(GPUImageFrameBuffer *frameBuffer);

    protected:

        GPUImageFrameBuffer *_filterFrameBuffer;
        GLProgram *_filterProgram;
        gpuimage_error_callback _errorCallback;
        void *_callbackOpaque;
        GLuint _inputTexture;
        GLint _filterPositionAttribute, _filterTextureCoordinateAttribute;
        GLint _filterInputTextureUniform;
        GLuint _filterModelMatrixUniform;
        glm::mat4 _modelMatrix;
        bool _isExtraFrameBuffer;

        struct Property {
            string type;
            string comment;
        };

        Property *_getProperty(const string &name);

        struct IntProperty : Property {
            int value;
            function<void(int &)> setCallback;
        };
        std::map<std::string, IntProperty> _intProperties;

        struct FloatProperty : Property {
            float value;
            std::function<void(float &)> setCallback;
        };
        std::map<std::string, FloatProperty> _floatProperties;

        struct StringProperty : Property {
            std::string value;
            std::function<void(std::string &)> setCallback;
        };
        std::map<std::string, StringProperty> _stringProperties;

        struct FloatVectorProperty : Property {
            std::vector<float> value;
            std::function<void(std::vector<float> &)> setCallback;
        };
        std::map<std::string, FloatVectorProperty> _floatVectorProperties;

        struct IntVectorProperty : Property {
            std::vector<int> value;
            std::function<void(std::vector<int> &)> setCallback;
        };
        std::map<std::string, IntVectorProperty> _intVectorProperties;
    private:
        void destroy();
    };

NS_GI_END


#endif //LEGEND_WBGPUIMAGEFILTER_HPP
