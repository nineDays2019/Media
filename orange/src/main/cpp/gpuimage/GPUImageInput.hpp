//
// Created by yunrui on 2019/6/4.
//

#ifndef LEGEND_GPUIMAGEINPUT_HPP
#define LEGEND_GPUIMAGEINPUT_HPP

#include "GPUImageMacros.h"
#include "GPUImageMath.h"
#include "GPUObject.hpp"
#include "glm/glm.hpp"
#include "glm/gtc/matrix_transform.hpp"

NS_GI_BEGIN

    class GPUImageInput : public virtual GPUObject {
    public:
        virtual void newFrameReadyAtTime(GPUTime frameTime, GLuint textureIndex) = 0;

        virtual void setInputTexture(GLuint newInputTexture, GLuint textureIndex) = 0;

        virtual int nextAvailableTextureIndex() = 0;

        virtual void setInputSize(GPUSize newSize, GLuint textureIndex) = 0;

        virtual void setModelMatrix(glm::mat4 modelMatrix) = 0;

        virtual glm::mat4 getModelMatrix() = 0;

        virtual void newFrameReadyPre() = 0;

        virtual void newFrameReadyAfter() = 0;

        virtual void reset() = 0;

        virtual void removeTargetReset() = 0;

    };

NS_GI_END

#endif //LEGEND_GPUIMAGEINPUT_HPP
