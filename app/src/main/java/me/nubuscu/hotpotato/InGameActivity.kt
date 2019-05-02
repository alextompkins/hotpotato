package me.nubuscu.hotpotato

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.roundToInt

class InGameActivity : AppCompatActivity(), SensorEventListener {

    private val TAG = "InGameActivity"

    private lateinit var physicsThread: Thread
    private lateinit var rollText: TextView
    private lateinit var pitchText: TextView
    private lateinit var container: LinearLayout
    private lateinit var potatoImage: ImageView
    private lateinit var sensorManager: SensorManager
    private lateinit var accelerometer: Sensor
    private lateinit var magnetometer: Sensor
    private var acceleration: FloatArray = FloatArray(9) { 0f }
    private var geomagnetic: FloatArray = FloatArray(9) { 0f }
    private var orientation: FloatArray = FloatArray(3) { 0f }

    private var potatoPos = floatArrayOf(0f, 0f)
    private var potatoVel = floatArrayOf(0f, 0f)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_in_game)

        rollText = findViewById(R.id.rollText)
        pitchText = findViewById(R.id.pitchText)
        container = findViewById(R.id.container)
        potatoImage = findViewById(R.id.potatoImage)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        // TODO involve onResume and onPause so that the physics processing only occurs when activity in foreground
        physicsThread = object : Thread() {
            override fun run() {
                while (!isInterrupted) {
                    try {
                        sleep(50)
                        runOnUiThread {
                            processPhysics()
                        }
                    } catch (exc: InterruptedException) {
                        Log.d(TAG, "Physics thread was interrupted")
                    }
                }
            }
        }
    }

    private fun processPhysics() {
        val MAX_VELOCITY = 30f

        Log.i(TAG, "processPhysics()")

        val maxX = container.width - potatoImage.drawable.intrinsicWidth
        val maxY = container.height - potatoImage.drawable.intrinsicHeight

        potatoVel[0] = potatoVel[0].addWithinBounds(orientation[2] * 5, -MAX_VELOCITY, MAX_VELOCITY)
        potatoVel[1] = potatoVel[1].addWithinBounds(-(orientation[1] * 5), -MAX_VELOCITY, MAX_VELOCITY)
        potatoPos[0] = potatoPos[0].addWithinBounds(potatoVel[0], 0f, maxX.toFloat())
        potatoPos[1] = potatoPos[1].addWithinBounds(potatoVel[1], 0f, maxY.toFloat())
        updatePotatoPos(potatoPos[0].toInt(), potatoPos[1].toInt())
    }

    private fun updatePotatoPos(x: Int, y: Int) {
        val layoutParams = potatoImage.layoutParams as LinearLayout.LayoutParams
        layoutParams.leftMargin = x
        layoutParams.topMargin = y
        layoutParams.rightMargin = 0
        layoutParams.bottomMargin = 0
        potatoImage.layoutParams = layoutParams
    }

    override fun onResume() {
        super.onResume()
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI)
        sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI)
        physicsThread.start()
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
        physicsThread.interrupt()
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

    private fun Float.addWithinBounds(other: Float, min: Float, max: Float): Float {
        val added = this + other
        return when {
            added < min -> min
            added > max -> max
            else -> added
        }
    }
}
