package com.juhezi.orange.media;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import com.juhezi.orange.utils.NativeUtils;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static android.media.MediaMetadataRetriever.*;


public class FFmpegMediaMetadataRetriever {

    static {
        NativeUtils.loadLibraries();
        native_init();
    }

    private static final String TAG = "FFmpegMediaMetadataRetr";

    public static Bitmap.Config IN_PREFERRED_CONFIG;

    // The field below is accessed by native methods
    private long mNativeContext;

    public FFmpegMediaMetadataRetriever() {
        native_setup();
    }

    public native void setDataSource(String path) throws IllegalArgumentException;

    public void setDataSource(String uri, Map<String, String> headers)
            throws IllegalArgumentException {
        int i = 0;
        String[] keys = new String[headers.size()];
        String[] values = new String[headers.size()];

        for (Map.Entry<String, String> entry : headers.entrySet()) {
            keys[i] = entry.getKey();
            values[i] = entry.getValue();
            i++;
        }
        _setDataSource(uri, keys, values);
    }

    private native void _setDataSource(String uri, String[] keys, String[] values)
            throws IllegalArgumentException;

    public native void setDataSource(FileDescriptor fileDescriptor, long offset, long length)
            throws IllegalArgumentException;

    public void setDataSource(FileDescriptor fileDescriptor)
            throws IllegalArgumentException {
        // intentionally less than LONG_MAX
        setDataSource(fileDescriptor, 0, 0x7ffffffffffffffL);
    }

    public void setDataSource(Context context, Uri uri)
            throws IllegalArgumentException, SecurityException {
        if (uri == null) {
            throw new IllegalArgumentException("Uri can not be null");
        }
        String scheme = uri.getScheme();
        if (scheme == null || scheme.equals("file")) {
            setDataSource(uri.getPath());
            return;
        }

        AssetFileDescriptor fileDescriptor = null;
        try {
            ContentResolver resolver = context.getContentResolver();
            try {
                // todo 需要了解怎么使用
                fileDescriptor = resolver.openAssetFileDescriptor(uri, "r");
            } catch (FileNotFoundException e) {
                throw new IllegalArgumentException(e);
            }
            if (fileDescriptor == null) {
                throw new IllegalArgumentException();
            }
            FileDescriptor descriptor = fileDescriptor.getFileDescriptor();
            if (!descriptor.valid()) {
                throw new IllegalArgumentException();
            }
            // Note: using getDeclaredLength so that our behavior is the same
            // as previous versions when the content provider is returning
            // a full file.
            if (fileDescriptor.getDeclaredLength() < 0) {
                setDataSource(descriptor);
            } else {
                setDataSource(descriptor, fileDescriptor.getStartOffset(), fileDescriptor.getDeclaredLength());
            }
            return;
        } catch (SecurityException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fileDescriptor != null) {
                    fileDescriptor.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        setDataSource(uri.toString());
    }

    /**
     * Call this method after setDataSource(). This method retrieves the
     * meta data value associated with the keyCode.
     * <p>
     * The keyCode currently supported is listed below as METADATA_XXX
     * constants. With any other value, it returns a null pointer.
     *
     * @param key One of the constants listed below at the end of the class.
     * @return The meta data value associate with the given keyCode on success;
     * null on failure.
     */
    public native String extractMetadata(String key);

    public native String extractMetadataFromChapter(String key, int chapter);

    private native final HashMap<String, String> native_getMetadata(boolean update_only, boolean apply_filter,
                                                                    HashMap<String, String> reply);

    public Bitmap getFrameAtTime(long timeUs, int option) {
        if (option < OPTION_PREVIOUS_SYNC || option > OPTION_CLOSEST) {
            throw new IllegalArgumentException("Unsupported option: " + option);
        }

        Bitmap b = null;
        BitmapFactory.Options bitmapOptionsCache = new BitmapFactory.Options();
        bitmapOptionsCache.inDither = false;    // 是否采用抖动解码

        byte[] picture = _getFrameAtTime(timeUs, option);

        if (picture != null) {
            b = BitmapFactory.decodeByteArray(picture, 0, picture.length, bitmapOptionsCache);
        }
        return b;
    }

    public Bitmap getFrameAtTime(long timeUs) {
        return getFrameAtTime(timeUs, OPTION_CLOSEST_SYNC);
    }

    public Bitmap getFrameAtTime() {
        return getFrameAtTime(-1, OPTION_CLOSEST_SYNC);
    }

    public Bitmap getScaledFrameAtTime(long timeUs, int option, int width, int height) {
        if (option < OPTION_PREVIOUS_SYNC ||
                option > OPTION_CLOSEST) {
            throw new IllegalArgumentException("Unsupported option: " + option);
        }

        Bitmap b = null;

        BitmapFactory.Options bitmapOptionsCache = new BitmapFactory.Options();
        bitmapOptionsCache.inDither = false;

        byte[] picture = _getScaledFrameAtTime(timeUs, option, width, height);

        if (picture != null) {
            b = BitmapFactory.decodeByteArray(picture, 0, picture.length, bitmapOptionsCache);
        }

        return b;
    }

    public Bitmap getScaledFrameAtTime(long timeUs, int width, int height) {
        return getScaledFrameAtTime(timeUs, OPTION_CLOSEST_SYNC, width, height);
    }

    private native byte[] _getScaledFrameAtTime(long timeUs, int option, int width, int height);

    private native byte[] _getFrameAtTime(long timeUs, int option);

    public native byte[] getEmbeddedPicture();

    public native void release();

    private native void native_setup();

    private static native void native_init();

    private native final void native_finalize();

    @Override
    protected void finalize() throws Throwable {
        try {
            native_finalize();
        } finally {
            super.finalize();
        }
    }

    public native void setSurface(Object surface);

}
