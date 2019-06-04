package com.tunebrains.compass

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.view.Surface
import android.view.WindowManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import io.reactivex.subjects.PublishSubject


class CompassSensorManager(ctx: Context) : LifecycleObserver, SensorEventListener {
    private var temporaryRotationMatrix = FloatArray(9)
    private var rotationMatrix = FloatArray(9)
    private var orientationData = FloatArray(3)
    private var accelerometerData = FloatArray(3)
    private var magneticData = FloatArray(3)
    private var azimuth: Double = 0.0
    val azimuthObserver = PublishSubject.create<Double>()
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

    }

    override fun onSensorChanged(event: SensorEvent) {
        val sensorType = event.sensor.type
        if (sensorType == Sensor.TYPE_ACCELEROMETER)
            accelerometerData = event.values
        else if (sensorType == Sensor.TYPE_MAGNETIC_FIELD)
            magneticData = event.values

        SensorManager.getRotationMatrix(temporaryRotationMatrix, null, accelerometerData, magneticData)
        configureDeviceAngle()
        SensorManager.getOrientation(rotationMatrix, orientationData)
        azimuth = Math.toDegrees(orientationData[0].toDouble())
        azimuthObserver.onNext(azimuth)

    }

    private val windowManager = ctx.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private val sensorManager = ctx.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    private val magneticFieldSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun start() {
        sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_GAME)
        sensorManager.registerListener(this, magneticFieldSensor, SensorManager.SENSOR_DELAY_GAME)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun stop() {
        sensorManager.unregisterListener(this, accelerometerSensor)
        sensorManager.unregisterListener(this, magneticFieldSensor)
    }

    private fun configureDeviceAngle() {
        when (windowManager.defaultDisplay.rotation) {
            Surface.ROTATION_0 // Portrait
            -> SensorManager.remapCoordinateSystem(
                temporaryRotationMatrix,
                SensorManager.AXIS_Z,
                SensorManager.AXIS_Y,
                rotationMatrix
            )
            Surface.ROTATION_90 // Landscape
            -> SensorManager.remapCoordinateSystem(
                temporaryRotationMatrix,
                SensorManager.AXIS_Y,
                SensorManager.AXIS_MINUS_Z,
                rotationMatrix
            )
            Surface.ROTATION_180 // Portrait
            -> SensorManager.remapCoordinateSystem(
                temporaryRotationMatrix,
                SensorManager.AXIS_MINUS_Z,
                SensorManager.AXIS_MINUS_Y,
                rotationMatrix
            )
            Surface.ROTATION_270 // Landscape
            -> SensorManager.remapCoordinateSystem(
                temporaryRotationMatrix,
                SensorManager.AXIS_MINUS_Y,
                SensorManager.AXIS_Z,
                rotationMatrix
            )
        }
    }

}