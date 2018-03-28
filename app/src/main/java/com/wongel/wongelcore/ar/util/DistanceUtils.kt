package com.wongel.wongelcore.ar.util

import android.location.Location

/**
 * Created by tseringwongelgurung on 3/28/18.
 */
object DistanceUtils {
    private fun angleFromCoordinate(l1: Location, l2: Location): Float {
        val dLon = l2.longitude - l1.longitude

        val y = Math.sin(dLon) * Math.cos(l2.latitude)
        val x = Math.cos(l1.latitude) * Math.sin(l2.latitude) - (Math.sin(l1.latitude)
                * Math.cos(l2.latitude) * Math.cos(dLon))

        var brng = Math.atan2(y, x)

        brng = Math.toDegrees(brng)
        brng = (brng + 360) % 360
        brng = 360 - brng

        return brng.toFloat()
    }
}