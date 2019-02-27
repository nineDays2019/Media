package me.juhezi.eternal.service

import android.annotation.SuppressLint
import android.content.Context
import android.text.TextUtils
import me.juhezi.eternal.base.BaseApplication
import me.juhezi.eternal.global.USER_STORE_SHARED_PREFERENCES_EDITOR_KEY
import me.juhezi.eternal.global.USER_STORE_SHARED_PREFERENCES_NAME
import me.juhezi.eternal.global.globalGson
import me.juhezi.eternal.model.User

/**
 * 所提供的服务：
 * 1. 查询当前登录用户的信息
 * 2. 登录
 * 3. 注销
 * 4. 修改用户信息
 *
 * 注：不包含用户界面
 *
 * Created by Juhezi[juhezix@163.com] on 2018/7/24.
 */
class UserService private constructor() {

    companion object {
        fun getInstance() = Holder.sInstance
    }

    private object Holder {
        @SuppressLint("StaticFieldLeak")
        val sInstance = UserService()
    }

    private var mCurrentUser: User? = null
    private val context: Context by lazy { BaseApplication.getApplicationContext() }

    init {
        restoreUser()
    }

    /**
     * 是否有用户登录
     */
    fun isOnline() = mCurrentUser != null


    /**
     * 注销
     */
    fun signOut() {
        mCurrentUser = null
        storeUser()
    }

    /**
     * 登录
     * 参数待定
     */
    fun signIn(user: User) {
        mCurrentUser = user
        storeUser()
    }

    fun getCurrentUser() = mCurrentUser

    /**
     * 存储用户信息
     */
    private fun storeUser() {
        val preferences = context.getSharedPreferences(USER_STORE_SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
        val editor = preferences.edit()
        val content = if (mCurrentUser == null) {
            null
        } else {
            globalGson.toJson(mCurrentUser)
        }
        editor.putString(USER_STORE_SHARED_PREFERENCES_EDITOR_KEY, content)
        editor.apply()
    }

    /**
     * 读取用户信息
     */
    private fun restoreUser() {
        val preferences = context.getSharedPreferences(USER_STORE_SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
        val content = preferences.getString(USER_STORE_SHARED_PREFERENCES_EDITOR_KEY, null)
        mCurrentUser = if (TextUtils.isEmpty(content)) {   // 并没有存储用户数据
            null
        } else {
            globalGson.fromJson(content, User::class.java)
        }
    }

}

// sign up - 注册
// sign out - 注销
// sign in - 登录
