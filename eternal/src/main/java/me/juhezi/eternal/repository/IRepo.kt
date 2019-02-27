package me.juhezi.eternal.repository

import me.juhezi.eternal.global.Fail
import me.juhezi.eternal.global.Success

// CRUD
// 增删改查

interface IRepo<T> {

    fun add(t: T, success: Success<T>?, fail: Fail?)

    fun update(t: T, success: Success<T>?, fail: Fail?)

    fun remove(id: String, success: Success<T>?, fail: Fail?)

    fun query(id: String, success: Success<T>?, fail: Fail?)

    fun queryAll(success: Success<List<T>>?, fail: Fail?)

}