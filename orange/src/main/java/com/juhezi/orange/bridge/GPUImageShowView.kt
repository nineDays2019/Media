package com.juhezi.orange.bridge

import me.juhezi.eternal.extension.e

class GPUImageShowView : OrangeBridge() {

    init {
        mNativeClassId = nativeInitShowView()
        if (mNativeClassId == 0L) {
            e("Create ShowView error!")
        }
    }

    fun show(texture2DId: Int, time: Float, rotationMode: Int) {
        if (mNativeClassId != 0L) {
            if (!nativeShowView(texture2DId, mNativeClassId, time, rotationMode)) {
                e("ShowView Invoke show() error!")
            }
        }
    }

    fun release() {
        if (mNativeClassId != 0L) {
            nativeReleaseShowView(mNativeClassId)
        } else {
            e("No create ShowView error!")
        }
    }
}
