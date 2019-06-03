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
import android.widget.Toast
import androidx.core.view.isVisible
import me.nubuscu.hotpotato.model.ClientDetailsModel
import me.nubuscu.hotpotato.model.dto.InGameUpdateMessage
import me.nubuscu.hotpotato.scheduling.GameScheduler
import me.nubuscu.hotpotato.util.GameInfoHolder
import me.nubuscu.hotpotato.util.sendToAllNearbyEndpoints
import tyrantgit.explosionfield.ExplosionField
import kotlin.math.roundToInt


const val FRICTION_COEFF = 0.95f
const val MIN_POTATO_DURATION = 5 * 1000L
const val MAX_POTATO_DURATION = 6 * 1000L


data class Vector2D(var x: Float, var y: Float)


class InGameActivity : ThemedActivity(), SensorEventListener {

    private val TAG = "InGameActivity"

    private var scheduler: GameScheduler? = null

    private lateinit var rollText: TextView
    private lateinit var remainingTimeText: TextView
    private lateinit var pitchText: TextView
    private lateinit var container: LinearLayout
    private lateinit var potatoImage: ImageView
    private lateinit var potatoExplosion: ExplosionField
    private lateinit var sensorManager: SensorManager
    private lateinit var accelerometer: Sensor
    private lateinit var magnetometer: Sensor
    private var acceleration: FloatArray = FloatArray(9) { 0f }
    private var geomagnetic: FloatArray = FloatArray(9) { 0f }
    private var orientation: FloatArray = FloatArray(3) { 0f }

    private lateinit var playerMapping: List<Pair<ClientDetailsModel, ImageView>>
    private val prevOverlaps: MutableMap<ImageView, Boolean> = mutableMapOf()

    private var potatoPos = Vector2D(0f, 0f)
    private var potatoVel = Vector2D(0f, 0f)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_in_game)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        rollText = findViewById(R.id.rollText)
        remainingTimeText = findViewById(R.id.remainingTimeText)
        pitchText = findViewById(R.id.pitchText)
        container = findViewById(R.id.container)
        potatoImage = findViewById(R.id.potatoImage)
        potatoImage.setImageResource(
            if (super.currentTheme == "night_mode") R.drawable.ic_bomb_100dp else R.drawable.ic_potato_100dp
        )
        potatoExplosion = ExplosionField.attach2Window(this)

        val playerIcons: Array<ImageView> = arrayOf(
            findViewById(R.id.p1Icon),
            findViewById(R.id.p2Icon),
            findViewById(R.id.p3Icon),
            findViewById(R.id.p4Icon),
            findViewById(R.id.p5Icon),
            findViewById(R.id.p6Icon),
            findViewById(R.id.p7Icon)
        )
        playerIcons.forEach { it.isVisible = false }
        playerIcons.forEach { prevOverlaps[it] = false }
        playerMapping = GameInfoHolder.instance.endpoints.zip(playerIcons)
        playerMapping.forEach { (_, icon) -> icon.isVisible = true }

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        enableFullscreen()
        isPlaying = GameInfoHolder.instance.isHost
    }

    private fun processPhysics() {
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

    private fun checkOverlaps() {
        playerMapping.forEach { (details, icon) ->
            val taskId = "GIVE_POTATO_${details.id}"
            val nowOverlapping = isOverlapping(icon, potatoImage)
            val prevOverlapping = prevOverlaps[potatoImage] ?: false

            if (!prevOverlapping && nowOverlapping) {
                setHighlighted(icon, true)
                scheduler?.schedule(taskId, {
                    runOnUiThread {
                        Toast.makeText(this, "Sending to player ${details.id}", Toast.LENGTH_SHORT).show()
                        sendToAllNearbyEndpoints(InGameUpdateMessage(5000, details.id), this)
                    }
                }, 2000)
                Log.i(TAG, "Scheduled $taskId")
            } else if (prevOverlapping && !nowOverlapping) {
                setHighlighted(icon, false)
                scheduler?.cancelTask(taskId)
                Log.i(TAG, "Cancelled $taskId")
            }

            prevOverlaps[potatoImage] = nowOverlapping
        }
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

        // Unblock thread
        startScheduler()
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)

        // TODO save amount of time remaining and handle correctly onResume
        scheduler?.kill()
        scheduler = null
    }

    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> acceleration = event.values
            Sensor.TYPE_MAGNETIC_FIELD -> geomagnetic = event.values
            else -> Log.d(TAG, "Unknown sensor type: ${event.sensor.stringType}")
        }

        val rotation = FloatArray(9) { 0f }
        SensorManager.getRotationMatrix(
            rotation,
            null,
            acceleration,
            geomagnetic
        )  // TODO something if false (i.e. device in freefall)
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

    private fun enableFullscreen() {
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_IMMERSIVE or
                    View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
    }

    var isPlaying = false
        set(value) {
            potatoImage.isVisible = value
            if (value) {
                startScheduler()
                onReceivePotato()
            } else {
                scheduler?.kill()
                playerMapping.forEach { setHighlighted(it.second, false) }
            }
            field = value
        }

    private fun startScheduler() {
        scheduler?.kill()
        scheduler = GameScheduler()
        scheduler?.schedule("PHYSICS_TICK", {
            runOnUiThread {
                Log.i("foo", "Running PHYSICS_TICK")
                processPhysics()
            }
        }, 10, true)
        scheduler?.schedule("OVERLAPS_CHECK", {
            runOnUiThread {
                Log.i("foo", "Running OVERLAPS_CHECK")
                checkOverlaps()
            }
        }, 100, true)
    }

    private fun onReceivePotato() {
        // TODO
//        val currentTime = System.currentTimeMillis()
//        val potatoDuration = Random(currentTime).nextLong(MIN_POTATO_DURATION, MAX_POTATO_DURATION)
//        countdownToExplode = object : CountDownTimer(potatoDuration, 100) {
//            override fun onTick(millisUntilFinished: Long) {
//                Log.i(TAG, "countdownToExplode.onTick()")
//                remainingTimeText.text = "${millisUntilFinished / 1000}s remaining"
//            }
//
//            override fun onFinish() {
//                remainingTimeText.text = "0s remaining"
//                potatoExplosion.explode(potatoImage)
//                Toast.makeText(this@InGameActivity, "The potato exploded.", Toast.LENGTH_SHORT).show()
//                sendToAllNearbyEndpoints(GameStateUpdateMessage(false), this@InGameActivity)
//                isPlaying = false
//            }
//        }.start()
    }
}
