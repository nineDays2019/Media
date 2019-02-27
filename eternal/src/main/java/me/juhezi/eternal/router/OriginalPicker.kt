package me.juhezi.eternal.router

import android.content.Intent

class OriginalPicker {
    enum class Type(var value: String) {
        ANY("*"),   // 全部种类
        IMAGE("image/*"),   //  图片
        VIDEO("video/*"),   // 视频
        AUDIO("audio/*"),   // 音频
        VIDEO_AND_AUDIO("video/*;image/*"),    // 视频和音频
    }

    companion object {

        @JvmStatic
        fun getIntent(type: Type = Type.ANY): Intent {
            val intent = Intent(Intent.ACTION_GET_CONTENT);
            intent.setType(type.value)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            return intent
        }
    }

}