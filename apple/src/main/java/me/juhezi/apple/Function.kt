package me.juhezi.apple

import android.app.Activity

fun select(a: Activity?, b: Activity, closure: (Activity.() -> Unit)) {
    if (a != null) {
        closure(a)
    } else {
        closure(b)
    }
}