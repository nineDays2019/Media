package me.juhezi.eternal.service

import android.content.Context
import android.content.SharedPreferences

object SharedPreferencesService {


    private fun getSharedPreferences(context: Context, name: String): SharedPreferences {
        return context.getSharedPreferences(name, Context.MODE_PRIVATE)
    }

    fun write(context: Context, name: String, closure: (SharedPreferences.Editor.() -> Unit)?) {
        val editor = getSharedPreferences(context, name).edit()
        closure?.invoke(editor)
        editor.apply()
    }

    fun read(context: Context, name: String, closure: (SharedPreferences.() -> Unit)?) {
        closure?.invoke(getSharedPreferences(context, name))
    }

}