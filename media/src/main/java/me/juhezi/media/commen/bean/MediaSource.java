package me.juhezi.media.commen.bean;

import android.text.TextUtils;

public class MediaSource {

    private final String path;

    public MediaSource(String path) {
        this.path = path;
    }

    public static MediaSource create(String path) {
        if (TextUtils.isEmpty(path)) {
            return null;
        }
        return new MediaSource(path);
    }

}
