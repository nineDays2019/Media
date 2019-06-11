//
// Created by yunrui on 2019/6/10.
//

#ifndef LEGEND_GPUIMAGEFILTERGROUP_HPP
#define LEGEND_GPUIMAGEFILTERGROUP_HPP

#include <stdio.h>
#include <vector>
#include "../GPUImageMacros.h"
#include "../GPUImageInput.hpp"
#include "../GPUImageOutput.hpp"
#include "GPUImageFilter.hpp"

NS_GI_BEGIN

    class GPUImageFilterGroup : public GPUImageInput, public GPUImageOutput {
    public:
        GPUImageFilterGroup();

        virtual ~GPUImageFilterGroup();

        bool hasFilter(const GPUImageFilter *filter) const;

        void addFilter(GPUImageFilter *newFilter);

        GPUImageFilter *filterAtIndex(int filterIndex);

        size_t getFilterCount();

        void removeFilter(GPUImageFilter *filter);

        void removeAllFilters();

        GLuint textureForOutput();

        GPUSize textureForOutputSize();

        GPUTime outputCaptureTime();

        GPUImageFilter *getTerminalFilter();

        void addTarget(GPUImageInput *newTarget);

        void addTarget(GPUImageInput *newTarget, GLuint textureLocation);

        void removeTarget(GPUImageInput *targetToRemove);

        void removeAllTargets();

        /// WBGPUImageInput protocol
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

    protected:
        GPUImageFilter *_terminalFilter;
    private:
        std::vector<GPUImageFilter *> _filters;

        void destroy();

        GPUImageFilter *predictTerminalFilter(GPUImageFilter *filter);

        void setTerminalFilter(GPUImageFilter *filter);

    };

NS_GI_END


#endif //LEGEND_GPUIMAGEFILTERGROUP_HPP
