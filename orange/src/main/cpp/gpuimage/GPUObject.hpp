//
// Created by yunrui on 2019/6/3.
//

#ifndef LEGEND_GPUOBJECT_HPP
#define LEGEND_GPUOBJECT_HPP

#include <stdio.h>
#include "GPUImageMacros.h"

NS_GI_BEGIN

    class GPUObject {
    public:
        GPUObject();

        virtual ~GPUObject();

        void retain();

        virtual void release();

        unsigned int getReferenceCount() const;

        void resetReferenceCount();

    protected:
        unsigned int _referenceCount;
    };

NS_GI_END

#endif //LEGEND_GPUOBJECT_HPP
