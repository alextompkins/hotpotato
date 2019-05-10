package me.nubuscu.hotpotato

import android.content.Context
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.roundToInt


const val FRICTION_COEFF = 0.95f


data class Vector2D(var x: Float, var y: Float)


class InGameActivity : AppCompatActivity(), SensorEventListener {

    private val TAG = "InGameActivity"

    private lateinit var physicsThread: PhysicsThread
    private val pauseLock = Object()

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

    private lateinit var playerIcons: Array<ImageView>

    private var potatoPos = Vector2D(0f, 0f)
    private var potatoVel = Vector2D(0f, 0f)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_in_game)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        rollText = findViewById(R.id.rollText)
        pitchText = findViewById(R.id.pitchText)
        container = findViewById(R.id.container)
        potatoImage = findViewById(R.id.potatoImage)

        playerIcons = arrayOf(
            findViewById(R.id.p1Icon),
            findViewById(R.id.p2Icon),
            findViewById(R.id.p3Icon),
            findViewById(R.id.p4Icon),
            findViewById(R.id.p5Icon),
            findViewById(R.id.p6Icon),
            findViewById(R.id.p7Icon)
        )

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        physicsThread = PhysicsThread()
        physicsThread.start()
    }

    inner class PhysicsThread : Thread() {
        var paused = false

        override fun run() {
            while (!isInterrupted) {
                synchronized (pauseLock) {
                    try {
                        if (paused) {
                            pauseLock.wait()
                        }

                        sleep(10)
                        runOnUiThread {
                            processPhysics()
                            runInteractions()
                        }
                    } catch (exc: InterruptedException) {
                        Log.d(TAG, "Physics thread was interrupted")
                    }
                }
            }
        }
    }

    private fun processPhysics() {
        Log.i(TAG, "processPhysics()")
        val maxX = container.width - potatoImage.drawable.intrinsicWidth
        val maxY = container.height - potatoImage.drawable.intrinsicHeight
        
        // Update positions
        potatoPos.x = potatoPos.x.addWithinBounds(potatoVel.x, 0f, maxX.toFloat())
        potatoPos.y = potatoPos.y.addWithinBounds(potatoVel.y, 0f, maxY.toFloat())
        updatePotatoPos(potatoPos.x.toInt(), potatoPos.y.toInt())

        // If on edge, bounce
        if (potatoPos.x == 0f || potatoPos.x == maxX.toFloat()) {
            potatoVel.x = -potatoVel.x
        }
        if (potatoPos.y == 0f || potatoPos.y == maxY.toFloat()) {
            potatoVel.y = -potatoVel.y
        }

        // Update velocities
        potatoVel.x += orientation[2]
        potatoVel.y -= orientation[1] * 2

        // Slow down gradually over time
        potatoVel.x *= FRICTION_COEFF
        potatoVel.y *= FRICTION_COEFF
    }

    private fun runInteractions() {
        playerIcons.forEach { setHighlighted(it, isOverlapping(it, potatoImage)) }
    }

    private fun isOverlapping(view: View, other: View): Boolean {
        return !(view.x < other.x && view.x + view.width < other.x ||
                other.x < view.x && other.x + other.width < view.x ||
                view.y < other.y && view.y + view.width < other.y ||
                other.y < view.y && other.y + other.width < view.y)
    }

    private fun setHighlighted(icon: ImageView, highlighted: Boolean) {
        val alpha = if (highlighted) 100 else 0
        icon.setColorFilter(Color.argb(alpha, 255, 255, 255))
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

        synchronized (pauseLock) {
            // Unblock thread
            physicsThread.paused = false
            pauseLock.notifyAll()
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)

        // Block thread
        physicsThread.paused = true
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
