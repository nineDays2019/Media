package me.juhezi.eternal.builder

import me.juhezi.eternal.extension.e
import me.juhezi.eternal.extension.isEmpty

/**
 * Scheme 的规则
 * <scheme> :// <host> : <port> [<path>|<pathPrefix>|<pathPattern>]
 *
 * <data android:scheme="string"
 *       android:host="string"
 *       android:port="string"
 *       android:path="string"
 *
 *       android:pathPattern="string"
 *       android:pathPrefix="string"
 *       android:mimeType="string" />
 *
 */
const val TIME = "time"
const val PERMISSION = "permission"

class SchemeBuilder {

    var scheme: String? = null
    var host: String? = null
    var port: String? = null
    var path: String? = null

    private var params: MutableMap<String, String> = mutableMapOf()
    var permission: String? = null  // 需要申请的权限，目前只支持申请单个权限，之后可以添加 PermissionGroup
    /**
     * 目前只是通过此方法创建的 Scheme
     * 默认添加当前时间作为参数
     */
    private fun defaultParams(): Map<String, String> = mapOf(
        TIME to
                System.currentTimeMillis().toString()
    )

    /**
     * 如果 key 已经存在，则更新 value
     */
    fun appendOrUpdateParam(pair: Pair<String, String>) {
        if (pair.isEmpty()) return
        // 有待观察
        params[pair.first] = pair.second
    }

    fun removeParam(key: String) {
        params.remove(key)
    }

    /**
     * 任意更改
     */
    fun updateParam(closure: MutableMap<String, String>.() -> Unit) {
        params.apply(closure)
    }

    fun build() =
        if (isEmpty(scheme)) ""
        else buildString {
            append(scheme)
            append("://")
            append(host ?: "")
            if (!isEmpty(port)) {
                append(":")
                append(port)
            }
            if (!isEmpty(path)) {
                append("/")
                append(path)
            }
            if (!isEmpty(permission)) { // map 中添加权限
                if (params.containsKey(PERMISSION)) {
                    e("Scheme 中的 Key [permission:${params[PERMISSION]}] 被替换了！！")
                }
                params[PERMISSION] = permission!!   // 有一个替换的风险
            }
            val totalParams = defaultParams() + params
            if (totalParams.isNotEmpty()) {
                append("?")
                totalParams.entries.forEachIndexed { index, entry ->
                    append(entry.key)
                    append("=")
                    append(entry.value)
                    if (index != totalParams.size - 1) {    // 如果不是最后一个元素
                        append("&")
                    }
                }
            }
        }

}

fun buildScheme(closure: SchemeBuilder.() -> Unit) = SchemeBuilder().apply(closure).build()