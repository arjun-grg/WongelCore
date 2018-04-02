package com.wongel.libwongelcore.ar.util

import android.location.Location
import android.util.Log
import android.view.MotionEvent
import com.google.ar.core.*
import com.wongel.libwongelcore.ar.model.Wongel
import com.wongel.libwongelcore.ar.renderer.Renderer

/**
 * Created by tseringwongelgurung on 3/28/18.
 */
object AnchorUtil {
    fun getAnchor(session: Session?, frame: Frame, position: Wongel.WPosition) = session?.createAnchor(
            frame.camera.pose
                    .compose(Pose.makeTranslation(position.x, position.y, position.z))
                    .extractTranslation())

    fun getAnchor(session: Session?, frame: Frame, locationConfig: Renderer.LocationConfig, obj: Wongel): Anchor? {
        val results = FloatArray(5)
        val userLocation=locationConfig.location

        Location.distanceBetween(userLocation.latitude, userLocation.longitude, obj.location!!.latitude, obj.location!!.longitude, results)

        val angle = DistanceUtils.angleFromCoordinate(userLocation, obj.location!!)

        val  distance=results[0]

        val X=distance*Math.cos(angle.toDouble())
        val Y=distance*Math.sin(angle.toDouble())

        Log.d("Ar",angle.toString()+"   x="+X+"    y="+Y)
        obj.position = Wongel.WPosition(0f, 0f, -results[0])
        return getAnchor(session, frame, obj.position!!)
    }

    fun getAnchor(frame: Frame, tap: MotionEvent): Anchor? {
        if (tap != null && frame.camera.getTrackingState() == TrackingState.TRACKING) {
            for (hit in frame.hitTest(tap)) {
                val trackable = hit.getTrackable()

                if (trackable is Plane && (trackable as Plane).isPoseInPolygon(hit.getHitPose()) || trackable is Point && (trackable as Point).orientationMode == Point.OrientationMode.ESTIMATED_SURFACE_NORMAL) {
                    return hit.createAnchor()
                    break
                }
            }
        }

        return null
    }
}