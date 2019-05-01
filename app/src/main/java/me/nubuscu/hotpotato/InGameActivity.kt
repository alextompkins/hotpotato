package me.nubuscu.hotpotato

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import kotlin.math.roundToInt

class InGameActivity : AppCompatActivity(), SensorEventListener {

    private val TAG = "InGameActivity"

    private lateinit var rollText: TextView
    private lateinit var pitchText: TextView
    private lateinit var sensorManager: SensorManager
    private lateinit var accelerometer: Sensor
    private lateinit var magnetometer: Sensor
    private var acceleration: FloatArray = FloatArray(9) { 0f }
    private var geomagnetic: FloatArray = FloatArray(9) { 0f }
    private var orientation: FloatArray = FloatArray(9) { 0f }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_in_game)

        rollText = findViewById(R.id.rollText)
        pitchText = findViewById(R.id.pitchText)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
    }

    override fun onResume() {
        super.onResume()
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI)
        sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI)
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> acceleration = event.values
            Sensor.TYPE_MAGNETIC_FIELD -> geomagnetic = event.values
            else -> Log.d(TAG, "Unknown sensor type: ${event.sensor.stringType}")
        }

        val rotation = FloatArray(9) { 0f }
        SensorManager.getRotationMatrix(rotation, null, acceleration, geomagnetic)  // TODO something if false (i.e. device in freefall)
        SensorManager.getOrientation(rotation, orientation)

        val pitchDeg = orientation[1].toDegrees().roundToInt()
        val rollDeg = orientation[2].toDegrees().roundToInt()

        rollText.text = "Roll: $rollDeg°"
        pitchText.text = "Pitch: $pitchDeg°"
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) = Unit

    private fun Float.toDegrees(): Float {
        return Math.toDegrees(this.toDouble()).toFloat()
    }
}
