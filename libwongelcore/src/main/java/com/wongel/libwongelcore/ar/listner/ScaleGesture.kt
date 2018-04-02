package com.wongel.libwongelcore.ar.listner

import android.view.ScaleGestureDetector

/**
 * Created by tseringwongelgurung on 3/27/18.
 */
class ScaleGesture() : ScaleGestureDetector.SimpleOnScaleGestureListener() {
    internal var scaleFactor = 1f

    constructor(scaleFactor:Float) : this() {
        this.scaleFactor=scaleFactor
    }

    override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
        return true
    }

    override fun onScale(detector: ScaleGestureDetector): Boolean {
        val factor = detector.scaleFactor - 1
        if (scaleFactor >= 0.10) {
            scaleFactor += factor
            if (scaleFactor < 0.10)
                scaleFactor = 0.10f
        }
        return true
    }
}