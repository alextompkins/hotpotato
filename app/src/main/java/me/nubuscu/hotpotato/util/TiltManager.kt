package me.nubuscu.hotpotato.util

import android.app.Activity
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log

class TiltManager(activity: Activity) : SensorEventListener {
    private val sensorManager = activity.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
    private var acceleration: FloatArray = FloatArray(9) { 0f }
    private var geomagnetic: FloatArray = FloatArray(9) { 0f }
    var orientation: FloatArray = FloatArray(3) { 0f }

    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> acceleration = event.values
            Sensor.TYPE_MAGNETIC_FIELD -> geomagnetic = event.values
            else -> Log.e(this.javaClass.name, "Unknown sensor type: ${event.sensor.stringType}")
        }

        val rotation = FloatArray(9) { 0f }
        SensorManager.getRotationMatrix(
            rotation,
            null,
            acceleration,
            geomagnetic
        )  // TODO something if false (i.e. device in freefall)
        SensorManager.getOrientation(rotation, orientation)
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) = Unit

    fun registerListeners() {
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI)
        sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI)
    }

    fun unregisterListeners() {
        sensorManager.unregisterListener(this)
    }
}
