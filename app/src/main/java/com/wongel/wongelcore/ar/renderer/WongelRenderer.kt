package com.wongel.wongelcore.ar.renderer

import android.view.View
import com.wongel.wongelcore.ar.rendering.PlaneRenderer

/**
 * Created by tseringwongelgurung on 3/27/18.
 */
interface WongelRenderer {
    fun initScene()

    fun showMessage(messsage: String?)

    fun addPlane(plane: PlaneRenderer)

    fun enableTouch(view: View?)

    fun enableScale(view: View?)

    fun enableScale(view: View?, scaleFactor: Float)

    fun addChild(child: Any)

    fun addChild(child: Any, x: Float, y: Float, z: Float)

    fun addChild(child: Any, latitude: Double, longitude: Double)

    interface WongelState {
        fun onResume()

        fun onPause()
    }
}