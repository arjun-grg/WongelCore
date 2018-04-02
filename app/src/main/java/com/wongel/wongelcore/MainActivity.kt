package com.wongel.wongelcore

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.CompoundButton
import com.wongel.libwongelcore.ar.listner.OnListner
import com.wongel.libwongelcore.ar.renderer.Renderer
import com.wongel.libwongelcore.ar.rendering.ObjectRenderer
import com.wongel.libwongelcore.ar.util.OrientationSensor
import kotlinx.android.synthetic.main.activity_main.*
import java.io.IOException


class MainActivity : AppCompatActivity(), OnListner<String>, SensorEventListener {
    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {

    }

    override fun onSensorChanged(event: SensorEvent?) {

    }

    var myRenderer: MyRenderer? = null
    lateinit var sensorManager: SensorManager
    var accelerometer: Sensor? = null
    var magnetometer: Sensor? = null

    override fun show(value: String) {
        Log.d("ar", value)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        myRenderer = MyRenderer(this)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)


        OrientationSensor(sensorManager,this).Register(this,1)

        initToolbar()
        initSurfaceView()
        addCheckListner()
    }

    fun initSurfaceView() {
        myRenderer?.enableScale(surfaceView, 0.25f)
//        myRenderer?.enableTap(surfaceView)
        myRenderer?.errorListener = this
//        myRenderer?.enablePlane = true

        val location = Location("dummy")
        location.latitude = 27.685055
        location.longitude = 85.320089

        myRenderer?.locationConfig = Renderer.LocationConfig(location, 0f)

        surfaceView.preserveEGLContextOnPause = true
        surfaceView.setEGLContextClientVersion(2)
        surfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0) // Alpha used for plane blending.
        surfaceView.setRenderer(myRenderer)
        surfaceView.renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
    }

    fun initToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        title = "Ar Test"
    }

    fun addCheckListner() {
        chkPlane.setOnCheckedChangeListener(object : CompoundButton.OnCheckedChangeListener {
            override fun onCheckedChanged(p0: CompoundButton?, checked: Boolean) {
                myRenderer?.enablePlane = checked
                surfaceView.requestRender()
            }
        })
    }

    override fun onResume() {
        super.onResume()
        myRenderer?.onResume()
        surfaceView?.onResume()
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI)
        sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI)
    }

    override fun onPause() {
        super.onPause()
        myRenderer?.onPause()
        surfaceView?.onPause()
        sensorManager.unregisterListener(this);
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        myRenderer?.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    class MyRenderer(c: Context) : Renderer(c) {
        internal var resourceName: String? = "aircraft.obj"
        internal var textureName: String? = "aircraft.jpg"

        override fun initScene() {
            try {
                val obj = ObjectRenderer().createOnGlThread(context, resourceName, textureName)
                obj.setMaterialProperties(0.0f, 3.5f, 1.0f, 6.0f)

                addChild(obj, 27.685167, 85.320100)

//                addChild(obj, 0f, 0f, -1f)

//                val obj1 = ObjectRenderer().createOnGlThread(context, resourceName, textureName)
//                obj1.setMaterialProperties(0.0f, 3.5f, 1.0f, 6.0f)
//
//                addChild(obj1, -1f, 0f, 0f)

//                val tabObj = ObjectRenderer().createOnGlThread(context, resourceName, textureName)
//                setTapObject(tabObj)
            } catch (e: IOException) {
                Log.e("Ar", "Failed to read obj file")
            }
        }
    }
}
