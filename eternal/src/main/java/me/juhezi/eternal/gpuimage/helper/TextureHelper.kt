package me.juhezi.eternal.gpuimage.helper

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.opengl.GLES20.*
import android.opengl.GLUtils.texImage2D
import android.support.annotation.DrawableRes
import android.util.Log
import me.juhezi.eternal.global.isDebug
import java.util.*


object TextureHelper {

    private val TAG = "TextureHelper"

    fun loadTexture(context: Context?, @DrawableRes resourceId: Int): Int {
        if (context == null) return 0
        val textureObjectIds = IntArray(1)
        glGenTextures(1, textureObjectIds, 0)   // 创建一个纹理对象

        if (textureObjectIds[0] == 0) {
            if (isDebug()) {
                Log.w(TAG, "loadTexture: Could not generate a new OpenGL texture object.")
            }
            return 0
        }
        val options = BitmapFactory.Options()
        options.inScaled = false
        val bitmap = BitmapFactory.decodeResource(context.resources, resourceId, options)
        if (bitmap == null) {
            if (isDebug()) {
                Log.w(TAG, "loadTexture: Resource ID: $resourceId could not be decoded.")
            }
            glDeleteTextures(1, textureObjectIds, 0)
            return 0
        }
        glBindTexture(GL_TEXTURE_2D, textureObjectIds[0])
        // 进行纹理过滤
        // 缩小的情况下，使用三线性过滤
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR)
        // 放大的情况下，使用双线性过滤
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)
        // 加载位图数据到 OpenGL 中
        // 告诉 OpenGL 读入 bitmap 定义的位图数据，并把它复制到"当前绑定"的纹理对象
        texImage2D(GL_TEXTURE_2D, 0, bitmap, 0)
        bitmap.recycle()
        // 生成 MIP 贴图
        glGenerateMipmap(GL_TEXTURE_2D)
        // 与纹理解除绑定
        glBindTexture(GL_TEXTURE_2D, 0)
        return textureObjectIds[0]
    }

    fun loadTexture(bitmap: Bitmap?, recycle: Boolean = true): Int {
        if (bitmap == null || bitmap.isRecycled) return 0
        val textureId = IntArray(1)
        glGenTextures(1, textureId, 0)
        if (textureId[0] == 0) {
            if (isDebug()) {
                Log.w(TAG, "loadTexture: Could not generate a new OpenGL texture object.")
            }
            return 0
        }
        glBindTexture(GL_TEXTURE_2D, textureId[0])
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR)
        // 放大的情况下，使用双线性过滤
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)
        // 加载位图数据到 OpenGL 中
        // 告诉 OpenGL 读入 bitmap 定义的位图数据，并把它复制到"当前绑定"的纹理对象
        texImage2D(GL_TEXTURE_2D, 0, bitmap, 0)
        if (recycle) {
            bitmap.recycle()
        }
        // 生成 MIP 贴图
        glGenerateMipmap(GL_TEXTURE_2D)
        // 与纹理解除绑定
        glBindTexture(GL_TEXTURE_2D, 0)
        return textureId[0]
    }

    fun loadCubeMap(context: Context, cubeResources: IntArray): Int {
        val textureIds = IntArray(1)
        glGenTextures(1, textureIds, 0)

        if (textureIds[0] == 0) {
            if (isDebug()) {
                Log.w(TAG, "Could not generate a new texture object.")
            }
            return 0
        }
        val options = BitmapFactory.Options()
        options.inScaled = false
        val cubeBitmaps = ArrayList<Bitmap?>(6)
        // 把所有 6 个图像都解码到内存中
        for (i in 0..5) {
            cubeBitmaps.add(i, BitmapFactory.decodeResource(context.resources,
                    cubeResources[i], options))

            if (cubeBitmaps[i] == null) {
                if (isDebug()) {
                    Log.w(TAG, "Resource ID " + cubeResources[i] + " could not be decoded.")
                }
                glDeleteTextures(1, textureIds, 0)
                return 0
            }
        }
        // 配置纹理过滤器
        glBindTexture(GL_TEXTURE_CUBE_MAP, textureIds[0])
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MAG_FILTER, GL_LINEAR)

        // 左 右 下 上 前 后 的顺序
        texImage2D(GL_TEXTURE_CUBE_MAP_NEGATIVE_X, 0, cubeBitmaps[0], 0)
        texImage2D(GL_TEXTURE_CUBE_MAP_POSITIVE_X, 0, cubeBitmaps[1], 0)

        texImage2D(GL_TEXTURE_CUBE_MAP_NEGATIVE_Y, 0, cubeBitmaps[2], 0)
        texImage2D(GL_TEXTURE_CUBE_MAP_POSITIVE_Y, 0, cubeBitmaps[3], 0)

        texImage2D(GL_TEXTURE_CUBE_MAP_NEGATIVE_Z, 0, cubeBitmaps[4], 0)
        texImage2D(GL_TEXTURE_CUBE_MAP_POSITIVE_Z, 0, cubeBitmaps[5], 0)

        // 解除纹理绑定
        glBindTexture(GL_TEXTURE_2D, 0)

        cubeBitmaps.forEach {
            it?.recycle()
        }
        return textureIds[0]
    }

}
