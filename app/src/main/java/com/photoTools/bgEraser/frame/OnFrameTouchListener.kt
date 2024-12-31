package com.photoTools.bgEraser.frame

import android.view.MotionEvent

interface OnFrameTouchListener {
    fun onFrameTouch(event: MotionEvent)
    fun onFrameDoubleClick(event: MotionEvent)
}
