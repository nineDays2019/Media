package me.juhezi.apple

import android.app.Activity

fun checkNull(
    a: Any?, closure1: () -> Unit,
    closure2: () -> Unit
) {
    if (a != null) {
        closure1()
    } else {
        closure2()
    }
}