//
// Created by yunrui on 2019/6/10.
//

#include "GPUImageFilterGroup.hpp"
#include "../GPUImageUtil.hpp"

GPUImage::GPUImageFilterGroup::GPUImageFilterGroup()
        : _terminalFilter(NULL) {
}

GPUImage::GPUImageFilterGroup::~GPUImageFilterGroup() {
    destroy();
}

bool GPUImage::GPUImageFilterGroup::hasFilter(const GPUImage::GPUImageFilter *filter) const {
    std::vector<GPUImageFilter *>::const_iterator it =
            std::find(_filters.begin(), _filters.end(), filter);
    return it != _filters.end();
}

void GPUImage::GPUImageFilterGroup::addFilter(GPUImage::GPUImageFilter *newFilter) {
    if (hasFilter(newFilter)) return;
    _filters.push_back(newFilter);
    GPUObject *object = dynamic_cast<GPUObject *>(newFilter);
    if (object) {
        object->retain();
    }
    setTerminalFilter(newFilter);
}

GPUImage::GPUImageFilter *GPUImage::GPUImageFilterGroup::filterAtIndex(int filterIndex) {
    return nullptr;
}

size_t GPUImage::GPUImageFilterGroup::getFilterCount() {
    return _filters.size();
}

void GPUImage::GPUImageFilterGroup::removeFilter(GPUImage::GPUImageFilter *filter) {
    std::vector<GPUImageFilter *>::const_iterator it =
            std::find(_filters.begin(), _filters.end(), filter);
    if (it != _filters.end()) {
        GPUObject *object = dynamic_cast<GPUObject *>(*it);
        if (object) {
            object->release();
        }
        _filters.erase(it);
    }
}

void GPUImage::GPUImageFilterGroup::removeAllFilters() {
    for (auto const &filter: _filters) {
        filter->release();
    }
    _filters.clear();
}

GLuint GPUImage::GPUImageFilterGroup::textureForOutput() {
    if (_terminalFilter) {
        return _terminalFilter->textureForOutput();
    }
    return static_cast<GLuint>(NO_TEXTURE);
}

GPUSize GPUImage::GPUImageFilterGroup::textureForOutputSize() {
    if (_terminalFilter) {
        return _terminalFilter->textureForOutputSize();
    }
    return kGPUSizeZero;
}

GPUTime GPUImage::GPUImageFilterGroup::outputCaptureTime() {
    if (_terminalFilter) {
        return _terminalFilter->outputCaptureTime();
    }
    return kGPUTimeZero;
}

GPUImage::GPUImageFilter *GPUImage::GPUImageFilterGroup::getTerminalFilter() {
    return _terminalFilter;
}

void GPUImage::GPUImageFilterGroup::addTarget(GPUImage::GPUImageInput *newTarget) {
    if (_terminalFilter) {
        _terminalFilter->addTarget(newTarget);
    }
}

void GPUImage::GPUImageFilterGroup::addTarget(GPUImage::GPUImageInput *newTarget, GLuint textureLocation) {
    if (_terminalFilter) {
        _terminalFilter->addTarget(newTarget, textureLocation);
    }
}

void GPUImage::GPUImageFilterGroup::removeTarget(GPUImage::GPUImageInput *targetToRemove) {
    if (_terminalFilter) {
        _terminalFilter->removeTarget(targetToRemove);
    }
}

void GPUImage::GPUImageFilterGroup::removeAllTargets() {
    if (_terminalFilter) {
        _terminalFilter->removeAllTargets();
    }
}

void GPUImage::GPUImageFilterGroup::newFrameReadyAtTime(GPUTime frameTime, GLuint textureIndex) {
    if (_filters.empty()) return;
    newFrameReadyPre();
    for (auto filter : _filters) {
        filter->newFrameReadyAtTime(frameTime, textureIndex);
    }
    newFrameReadyAfter();
}

void GPUImage::GPUImageFilterGroup::setInputTexture(GLuint newInputTexture, GLuint textureIndex) {
    GPUImageFilter *preInput = NULL;
    for (auto filter: _filters) {
        if (!preInput) {
            filter->setInputTexture(newInputTexture, textureIndex);
        } else {
            filter->setInputTexture(preInput->textureForOutput(), textureIndex);
        }
        preInput = filter;
    }
}

int GPUImage::GPUImageFilterGroup::nextAvailableTextureIndex() {
    return 0;
}

void GPUImage::GPUImageFilterGroup::setInputSize(GPUSize newSize, GLuint textureIndex) {
    for (auto filter : _filters) {
        filter->setInputSize(newSize, textureIndex);
    }
}

void GPUImage::GPUImageFilterGroup::setModelMatrix(glm::mat4 modelMatrix) {
    if (_terminalFilter) {
        _terminalFilter->setModelMatrix(modelMatrix);
    }
}

glm::mat4 GPUImage::GPUImageFilterGroup::getModelMatrix() {
    if (_terminalFilter) {
        return _terminalFilter->getModelMatrix();
    }
    return glm::mat4(0);
}

void GPUImage::GPUImageFilterGroup::newFrameReadyPre() {

}

void GPUImage::GPUImageFilterGroup::newFrameReadyAfter() {

}

void GPUImage::GPUImageFilterGroup::reset() {
    removeAllTargets();
}

void GPUImage::GPUImageFilterGroup::removeTargetReset() {

}

void GPUImage::GPUImageFilterGroup::destroy() {
    removeAllFilters();
    _terminalFilter = NULL;
}

GPUImage::GPUImageFilter *GPUImage::GPUImageFilterGroup::predictTerminalFilter(GPUImage::GPUImageFilter *filter) {
    if (filter->getTargets().size() == 0) {
        return filter;
    } else {
        return predictTerminalFilter(dynamic_cast<GPUImageFilter *>(filter->getTargets()[0]));
    };
}

void GPUImage::GPUImageFilterGroup::setTerminalFilter(GPUImage::GPUImageFilter *filter) {
    _terminalFilter = filter;
}
