package me.juhezi.eternal.global

import android.annotation.SuppressLint
import com.google.gson.Gson
import io.realm.Realm

val globalGson = Gson()

@SuppressLint("StaticFieldLeak")
val globalRealm = Realm.getDefaultInstance()