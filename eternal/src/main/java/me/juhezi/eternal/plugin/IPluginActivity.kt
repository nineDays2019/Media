package me.juhezi.eternal.plugin

import android.os.Bundle
import android.view.MotionEvent

interface IPluginActivity {

    fun onCreate(savedInstanceState: Bundle?)

    fun onStart()

    fun onResume()

    fun onPause()

    fun onStop()

    fun onDestroy()

    fun onSaveInstanceState(outState: Bundle?)

    fun onTouchEvent(event: MotionEvent?): Boolean

    fun onBackPressed()

}