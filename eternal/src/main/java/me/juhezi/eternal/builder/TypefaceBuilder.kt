package me.juhezi.eternal.builder

import android.content.res.AssetManager
import android.graphics.Typeface
import android.text.TextUtils

/**
 * Typeface Builder
 * Created by Juhezi[juhezix@163.com] on 2017/8/4.
 */
class Wrapper(var path: String = "",
              var assetManager: AssetManager? = null) {
    fun build(): Typeface? {
        if (!TextUtils.isEmpty(path) && assetManager != null) {
            return Typeface.createFromAsset(assetManager, path)
        }
        return null
    }
}

fun buildTypeface(buildAction: Wrapper.() -> Unit) =
        Wrapper().apply(buildAction).build()

/*
Simple:
textView.typeface = buildTypeface {
    path = FONT_ASSET_PATH
    assetManager = assets
}
*/
