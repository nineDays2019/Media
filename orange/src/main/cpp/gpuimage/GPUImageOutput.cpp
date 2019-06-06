//
// Created by yunrui on 2019/6/4.
//

#include "GPUImageOutput.hpp"

NS_GI_BEGIN

    GPUImageOutput::GPUImageOutput() :
            _outputTexture(static_cast<GLuint>(NO_TEXTURE)),
            _outputTextureSize(kGPUSizeZero),
            _captureTime(kGPUTimeZero) {
    }

    GPUImageOutput::~GPUImageOutput() {
        destroy();
    }

    GLuint GPUImageOutput::textureForOutput() {
        return _outputTexture;
    }

    GPUSize GPUImageOutput::textureForOutputSize() {
        return _outputTextureSize;
    }

    GPUTime GPUImageOutput::outputCaptureTime() {
        return _captureTime;
    }

    void GPUImageOutput::informTargetsAboutNewFrameAtTime() {
        size_t numberTargets = _targets.size();

        for (size_t k = 0; k < numberTargets; k++) {
            GPUImageInput *currentTarget = _targets[k];
            GLuint textureIndex = _targetTextureIndices[k];

            if (currentTarget) {
                currentTarget->setInputSize(textureForOutputSize(), textureIndex);
                currentTarget->setInputTexture(textureForOutput(), textureIndex);
                currentTarget->newFrameReadyAtTime(outputCaptureTime(), textureIndex);
            }

        }
    }

    GPUImageOutput *GPUImageOutput::predictTerminalFilter(GPUImage::GPUImageOutput *filter) {
        if (filter->getTargets().size() == 0) {
            return filter;
        } else {
            return predictTerminalFilter(dynamic_cast<GPUImageOutput *>(filter->getTargets()[0]));
        }
    }

    GPUImageOutput *GPUImageOutput::getTerminalTarget() {
        return predictTerminalFilter(this);
    }

    std::vector<GPUImageInput *> GPUImageOutput::getTargets() {
        return std::vector<GPUImageInput *>();
    }

    void GPUImageOutput::addTarget(GPUImageInput *newTarget) {
        GLuint nextAvailableTextureIndex = static_cast<GLuint>(newTarget->nextAvailableTextureIndex());
        addTarget(newTarget, nextAvailableTextureIndex);
    }

    void GPUImageOutput::addTarget(GPUImageInput *newTarget, GLuint textureLocation) {
        std::vector<GPUImageInput *>::const_iterator found =
                std::find(_targets.begin(), _targets.end(), newTarget);
        if (found != _targets.end()) {
            return;
        }

        if (newTarget) {
            newTarget->setInputTexture(textureForOutput(), textureLocation);
            newTarget->retain();
        }

        _targets.push_back(newTarget);
        _targetTextureIndices.push_back(textureLocation);

    }

    void GPUImageOutput::removeTarget(GPUImageInput *targetToRemove) {
        std::vector<GPUImageInput *>::iterator found = std::find(_targets.begin(), _targets.end(), targetToRemove);
        if (found == _targets.end()) {
            return;
        }

        size_t indexOfObject = (found - _targets.begin());
        GLuint textureIndexOfTarget = _targetTextureIndices[indexOfObject];

        targetToRemove->removeTargetReset();
        targetToRemove->release();

        _targets.erase(found);

        std::vector<GLuint>::const_iterator textureFound =
                std::find(_targetTextureIndices.begin(),
                          _targetTextureIndices.end(), textureIndexOfTarget);
        if (textureFound != _targetTextureIndices.end()) {
            _targetTextureIndices.erase(textureFound);
        }
    }

    void GPUImageOutput::removeAllTargets() {
        size_t numberTargets = _targets.size();

        for (size_t k = 0; k < numberTargets; k++) {
            GPUImageInput *targetToRemove = _targets[k];
            targetToRemove->removeTargetReset();
            targetToRemove->release();
        }

        _targets.clear();
        _targetTextureIndices.clear();
    }

    void GPUImageOutput::destroy() {
        removeAllTargets();
    }

NS_GI_END
