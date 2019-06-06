//
// Created by yunrui on 2019/6/4.
//

#ifndef LEGEND_GPUIMAGEOUTPUT_HPP
#define LEGEND_GPUIMAGEOUTPUT_HPP

#include <stdio.h>
#include <vector>
#include "GPUImageMacros.h"
#include "GPUObject.hpp"
#include "GPUImageInput.hpp"
#include "GPUImageMath.h"

NS_GI_BEGIN

    class GPUImageOutput : public virtual GPUObject {

    public:
        GPUImageOutput();

        virtual ~GPUImageOutput();

        virtual GLuint textureForOutput();

        virtual GPUSize textureForOutputSize();

        virtual GPUTime outputCaptureTime();

        void informTargetsAboutNewFrameAtTime();

        virtual std::vector<GPUImageInput *> getTargets();

        virtual GPUImageOutput *getTerminalTarget();

        virtual void addTarget(GPUImageInput *newTarget);

        virtual void addTarget(GPUImageInput *newTarget, GLuint textureLocation);

        virtual void removeTarget(GPUImageInput *targetToRemove);

        virtual void removeAllTargets();

    protected:
        std::vector<GPUImageInput *> _targets;
        std::vector<GLuint> _targetTextureIndices;
        GLuint _outputTexture;
        GPUSize _outputTextureSize;
        GPUTime _captureTime;
    private:
        void destroy();

        GPUImageOutput *predictTerminalFilter(GPUImageOutput *filter);
    };

NS_GI_END


#endif //LEGEND_GPUIMAGEOUTPUT_HPP
