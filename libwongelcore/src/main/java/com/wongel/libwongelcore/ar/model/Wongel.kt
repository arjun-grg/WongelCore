package com.wongel.libwongelcore.ar.model

import android.location.Location
import android.view.MotionEvent
import com.google.ar.core.Anchor

/**
 * Created by tseringwongelgurung on 3/27/18.
 */
class Wongel {
    var anchorMatrix: FloatArray = FloatArray(16)
    var anchor: Anchor? = null
    var obj: Any? = null
    var position: WPosition? = null
    var tap: MotionEvent? = null
    var location:Location?=null

    constructor(object3D: Any?) {
        this.obj = object3D
    }

    constructor(obj: Any?, x: Float, y: Float, z: Float) {
        this.obj = obj
        this.position = WPosition(x, y, z)
    }

    constructor(obj: Any?, tap: MotionEvent?) {
        this.obj = obj
        this.tap = tap
    }

    constructor(obj: Any?, location: Location?) {
        this.obj = obj
        this.location = location
    }

    data class WPosition(val x: Float, val y: Float, val z: Float)
}