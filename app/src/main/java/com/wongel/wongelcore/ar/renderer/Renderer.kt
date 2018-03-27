package com.wongel.wongelcore.ar.renderer

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.widget.Toast
import com.google.ar.core.*
import com.google.ar.core.exceptions.UnavailableApkTooOldException
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException
import com.google.ar.core.exceptions.UnavailableSdkTooOldException
import com.google.ar.core.exceptions.UnavailableUserDeclinedInstallationException
import com.wongel.wongelcore.ar.listner.OnListner
import com.wongel.wongelcore.ar.listner.ScaleGesture
import com.wongel.wongelcore.ar.model.Wongel
import com.wongel.wongelcore.ar.rendering.BackgroundRenderer
import com.wongel.wongelcore.ar.rendering.ObjectRenderer
import com.wongel.wongelcore.ar.rendering.PlaneRenderer
import com.wongel.wongelcore.ar.rendering.PointCloudRenderer
import com.wongel.wongelcore.ar.util.CameraPermissionHelper
import com.wongel.wongelcore.ar.util.DisplayRotationHelper
import java.io.IOException
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * Created by tseringwongelgurung on 3/27/18.
 */
abstract class Renderer(val context: Context) : GLSurfaceView.Renderer, WongelRenderer, WongelRenderer.WongelState, View.OnTouchListener {
    private val TAG = Renderer::class.java.toString()

    private val list: MutableList<Wongel>

    private lateinit var delegate: ArDelegate
    private val backgroundRenderer = BackgroundRenderer()
    private var planeRenderer: PlaneRenderer? = null
    private val pointCloud = PointCloudRenderer()

    private var displayRotationHelper: DisplayRotationHelper? = null
    private var session: Session? = null
    var errorListener: OnListner<String>? = null

    var enablePlane: Boolean = false

    private var scaleGesture: ScaleGesture? = null
    private var scaleGestureDetector: ScaleGestureDetector? = null
    private var gestureDetector: GestureDetector? = null

    private var tapObject: ObjectRenderer? = null

    init {
        list = mutableListOf()

        try {
            displayRotationHelper = DisplayRotationHelper(context)
            delegate = ArDelegate()
        } catch (e: NoSuchMethodError) {
            showMessage("Sorry your device does not supprt Ar")
        }
    }

    override fun showMessage(message: String?) {
        if (message != null) {
            if (errorListener == null)
                throw Exception("Error Listner not implimented")
            else
                errorListener?.show(message)
        }
    }

    override fun onResume() {
        var message: String? = null
        try {
            delegate.onResume()
        } catch (e: UnavailableArcoreNotInstalledException) {
            message = "Please install ARCore"
        } catch (e: UnavailableUserDeclinedInstallationException) {
            message = "Please install ARCore"
        } catch (e: UnavailableApkTooOldException) {
            message = "Please update ARCore"
        } catch (e: UnavailableSdkTooOldException) {
            message = "Please update this app"
        } catch (e: Exception) {
            message = "This device does not support AR"
        }

        if (message != null)
            showMessage(message)
        else {
            session?.resume()
            displayRotationHelper?.onResume()
        }
    }

    override fun onPause() {
        if (session != null) {
            displayRotationHelper!!.onPause()
            session!!.pause()
        }
    }

    override fun addPlane(plane: PlaneRenderer?) {
        this.planeRenderer = plane
    }

    fun setTapObject(obj: ObjectRenderer) {
        this.tapObject = obj
    }

    override fun onDrawFrame(p0: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        if (session == null) {
            return
        }

        displayRotationHelper!!.updateSessionIfNeeded(session)

        try {
            session!!.setCameraTextureName(backgroundRenderer.getTextureId())

            val frame = session!!.update()
            val camera = frame.camera

            backgroundRenderer.draw(frame)

            if (camera.trackingState == TrackingState.PAUSED) {
                return
            }

            drawObjects(camera, frame)
        } catch (t: Throwable) {
            Log.e(TAG, "Exception on the OpenGL thread", t)
        }
    }

    override fun onSurfaceChanged(p0: GL10?, width: Int, height: Int) {
        displayRotationHelper!!.onSurfaceChanged(width, height)
        GLES20.glViewport(0, 0, width, height)
    }

    override fun onSurfaceCreated(p0: GL10?, p1: EGLConfig?) {
        GLES20.glClearColor(0.1f, 0.1f, 0.1f, 1.0f)
        backgroundRenderer.createOnGlThread(context)
        pointCloud.createOnGlThread(context)

        if (enablePlane) {
            renderPlane()
        } else
            addPlane(null)

        initScene()
    }

    override fun onTouch(p0: View?, event: MotionEvent?): Boolean {
        if (gestureDetector != null)
            gestureDetector!!.onTouchEvent(event)
        if (scaleGestureDetector != null)
            scaleGestureDetector!!.onTouchEvent(event)

        return (gestureDetector != null || scaleGestureDetector != null)
    }


    private fun enableTouch(view: View?) {
        view?.setOnTouchListener(this)
    }

    override fun enableTap(view: View?) {
        gestureDetector = GestureDetector(
                context,
                object : GestureDetector.SimpleOnGestureListener() {
                    override fun onSingleTapUp(e: MotionEvent): Boolean {
                        addTapChild(e)
                        return true
                    }

                    override fun onDown(e: MotionEvent): Boolean {
                        return true
                    }
                })
        enableTouch(view)
    }

    override fun enableScale(view: View?) {
        enableTouch(view)
        scaleGesture = ScaleGesture()
        scaleGestureDetector = ScaleGestureDetector(context, scaleGesture)
    }

    override fun enableScale(view: View?, scaleFactor: Float) {
        enableTouch(view)
        scaleGesture = ScaleGesture(scaleFactor)
        scaleGestureDetector = ScaleGestureDetector(context, scaleGesture)
    }

    override fun addTapChild(tap: MotionEvent) {
        if (tapObject != null)
            list.add(Wongel(tapObject, tap))
        else
            showMessage("Tap Object not set")
    }

    override fun addChild(child: Any) {
        list.add(Wongel(child, 0f, 0f, 0f))
    }

    override fun addChild(child: Any, x: Float, y: Float, z: Float) {
        list.add(Wongel(child, x, y, z))
    }

    override fun addChild(child: Any, latitude: Double, longitude: Double) {

    }

    private fun drawObjects(camera: Camera, frame: Frame) {
        val projmtx = FloatArray(16)
        camera.getProjectionMatrix(projmtx, 0, 0.1f, 100.0f)

        val viewmtx = FloatArray(16)
        camera.getViewMatrix(viewmtx, 0)

        val pointCloud = frame.acquirePointCloud()
        this.pointCloud.update(pointCloud)
        this.pointCloud.draw(viewmtx, projmtx)

        pointCloud.release()

        if (enablePlane)
            planeRenderer?.drawPlanes(session?.getAllTrackables(Plane::class.java), camera.displayOrientedPose, projmtx)

        for (obj in list) {
            if (obj is Wongel) {
                if (obj.anchor == null) {
                    if (obj.position != null)
                        obj.anchor = getAnchor(frame, obj.position!!)
                    else
                        obj.anchor = getAnchor(frame, obj.tap!!)
                }

                anchorObject(obj, frame, viewmtx, projmtx)
            }
        }
    }

    private fun anchorObject(wongel: Wongel, frame: Frame, viewmtx: FloatArray, projmtx: FloatArray) {
        val lightIntensity = frame.lightEstimate.pixelIntensity

        wongel.anchor?.pose?.toMatrix(wongel.anchorMatrix, 0)

        if (wongel.obj is ObjectRenderer) {
            val obj = (wongel.obj as ObjectRenderer)
            obj?.updateModelMatrix(wongel.anchorMatrix, scaleGesture?.scaleFactor!!)
            obj?.draw(viewmtx, projmtx, lightIntensity)
        }
    }

    private fun getAnchor(frame: Frame, position: Wongel.WPosition) = session?.createAnchor(
            frame.camera.pose
                    .compose(Pose.makeTranslation(position.x, position.y, position.z))
                    .extractTranslation())

    private fun getAnchor(frame: Frame, tap: MotionEvent): Anchor? {
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

    inner class ArDelegate {
        private var installRequested: Boolean = false

        fun onResume() {
            if (session == null) {
                if (!isArInstalled() || !hasCameraPermission())
                    return

                initSession()
            }
        }

        private fun hasCameraPermission(): Boolean = if (!CameraPermissionHelper.hasCameraPermission(context as AppCompatActivity)) {
            CameraPermissionHelper.requestCameraPermission(context as AppCompatActivity)
            false
        } else
            true

        private fun isArInstalled(): Boolean {
            when (ArCoreApk.getInstance().requestInstall(context as AppCompatActivity, !installRequested)) {
                ArCoreApk.InstallStatus.INSTALL_REQUESTED -> {
                    installRequested = true
                    return false
                }
            }

            return true
        }

        fun initSession() {
            session = Session(context as AppCompatActivity)


            val config = Config(session!!)
            if (!session!!.isSupported(config))
                showMessage("This device does not support AR")

            session!!.configure(config)
        }
    }

    fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, results: IntArray) {
        if (!CameraPermissionHelper.hasCameraPermission(context as AppCompatActivity)) {
            Toast.makeText(context, "Camera permission is needed to run this application", Toast.LENGTH_LONG)
                    .show()
            if (!CameraPermissionHelper.shouldShowRequestPermissionRationale(context)) {
                CameraPermissionHelper.launchPermissionSettings(context)
            }
            context.finish()
        }
    }

    private fun renderPlane() {
        try {
            val plane = PlaneRenderer().createOnGlThread(context, "trigrid.png")
            addPlane(plane)
        } catch (e: IOException) {
            showMessage("Failed to read plane texture")
        }
    }
}