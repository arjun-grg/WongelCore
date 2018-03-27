package com.wongel.wongelcore.ar.model

import com.google.ar.core.Anchor

/**
 * Created by tseringwongelgurung on 3/27/18.
 */
class Wongel {
    var anchorMatrix: FloatArray = FloatArray(16)
    var anchor: Anchor? = null
    var obj: Any? = null
    var position: WPosition? = null

    constructor(object3D: Any?) {
        this.anchor = anchor
        this.obj = object3D
    }

    constructor(obj: Any?, x: Float, y: Float, z: Float) {
        this.obj=obj
        this.anchorMatrix = anchorMatrix
        this.position = WPosition(x, y, z)
    }

    data class WPosition(val x: Float, val y: Float, val z: Float)
}