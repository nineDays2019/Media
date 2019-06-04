//
// Created by yunrui on 2019/6/3.
//

#include "GPUObject.hpp"
#include <assert.h>

NS_GI_BEGIN

    GPUObject::GPUObject() : _referenceCount(1) {
    }

    GPUObject::~GPUObject() {
    }

    void GPUObject::retain() {
        assert(_referenceCount > 0);
        ++_referenceCount;
    }

    void GPUObject::release() {
        assert(_referenceCount > 0);
        --_referenceCount;
        if (_referenceCount == 0) {
            delete this;
        }
    }

    unsigned int GPUObject::getReferenceCount() const {
        return _referenceCount;
    }

    void GPUObject::resetReferenceCount() {
        _referenceCount = 1;
    }

NS_GI_END