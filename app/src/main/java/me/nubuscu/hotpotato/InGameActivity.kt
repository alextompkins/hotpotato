package me.nubuscu.hotpotato

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import me.nubuscu.hotpotato.connection.handler.GameEndHandler
import me.nubuscu.hotpotato.connection.handler.InGameUpdateHandler
import me.nubuscu.hotpotato.model.ClientDetailsModel
import me.nubuscu.hotpotato.model.dto.GameEndMessage
import me.nubuscu.hotpotato.model.dto.InGameUpdateMessage
import me.nubuscu.hotpotato.scheduling.GameEvent.*
import me.nubuscu.hotpotato.scheduling.GameScheduler
import me.nubuscu.hotpotato.util.*
import tyrantgit.explosionfield.ExplosionField
import kotlin.random.Random


const val VIBRATE_DURATION = 75L
const val MIN_POTATO_DURATION = 10 * 1000L
const val MAX_POTATO_DURATION = 30 * 1000L


class InGameActivity : ThemedActivity() {
    private var scheduler: GameScheduler? = null
    private var setToExpireAt: Long? = null

    private lateinit var remainingTimeText: TextView
    private lateinit var container: LinearLayout
    private lateinit var potatoImage: ImageView
    private lateinit var potatoObject: PhysicsObject2D
    private lateinit var potatoExplosion: ExplosionField
    private lateinit var tiltManager: TiltManager
    private lateinit var vibrateManager: VibrateManager
    private lateinit var beepHelper: BeepHelper

    private lateinit var playerMapping: List<Pair<ClientDetailsModel, ImageView>>
    private val prevOverlaps: MutableMap<ImageView, Boolean> = mutableMapOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_in_game)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        tiltManager = TiltManager(this)
        vibrateManager = VibrateManager(this)

        remainingTimeText = findViewById(R.id.remainingTimeText)
        container = findViewById(R.id.container)
        potatoImage = findViewById(R.id.potatoImage)
        potatoImage.setImageResource(
            if (currentTheme == "night_mode") R.drawable.ic_bomb_100dp else R.drawable.ic_potato_100dp
        )
        potatoObject = PhysicsObject2D(onCollideWithBounds = {
            vibrateManager.vibrate(VIBRATE_DURATION)
        })
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

        val otherPlayers = GameInfoHolder.instance.endpoints.filter { it.id != GameInfoHolder.instance.myEndpointId }
        playerMapping = otherPlayers.zip(playerIcons)
        playerMapping.forEach { (_, icon) -> icon.isVisible = true }

        isPlaying = GameInfoHolder.instance.isHost
    }

    override fun onStart() {
        super.onStart()
        InGameUpdateHandler.addExtraHandler(inGameUpdateHandler)
        GameEndHandler.addExtraHandler(gameEndHandler)
    }

    override fun onResume() {
        super.onResume()
        enableFullscreen()
        tiltManager.registerListeners()
        beepHelper = BeepHelper()

        // If the user had the potato, restart game scheduler
        if (isPlaying && scheduler == null) {
            startScheduler()
            // If the potato was set to expire, resume countdown
            setToExpireAt?.let {
                startPotatoCountdown(it - System.currentTimeMillis())
            }
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

    override fun onPause() {
        super.onPause()
        tiltManager.unregisterListeners()
        beepHelper.release()

        // If the game scheduler is running, save the time of potato expiry and kill it
        scheduler?.let {
            setToExpireAt = it.getScheduledTime(POTATO_EXPIRE.name)
            it.kill()
            scheduler = null
        }
    }

    override fun onStop() {
        super.onStop()
        InGameUpdateHandler.removeExtraHandler(inGameUpdateHandler)
        GameEndHandler.removeExtraHandler(gameEndHandler)
    }

    override fun onBackPressed() {
        AlertDialog.Builder(this)
            .setMessage("Are you sure you want to leave the game? This will remove you from the current lobby.")
            .setPositiveButton("Yes") { _, _ ->
                // TODO Go back to lobby screen and leave current lobby
                finish()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.cancel()
            }
            .create()
            .show()
    }

    private var isPlaying = false
        set(value) {
            potatoImage.isVisible = value
            if (value) {
                startScheduler()
                val currentTime = System.currentTimeMillis()
                val potatoDuration = setToExpireAt?.minus(currentTime) ?: Random(currentTime)
                    .nextLong(MIN_POTATO_DURATION, MAX_POTATO_DURATION)
                startPotatoCountdown(potatoDuration)
            } else {
                scheduler?.kill()
                playerMapping.forEach { setHighlighted(it.second, false) }
            }
            field = value
        }

    private val timeUntilExpiry: Long
        get() {
            val expiryTime = scheduler?.getScheduledTime(POTATO_EXPIRE.name)
            return if (expiryTime == null) 0 else expiryTime - System.currentTimeMillis()
        }

    private fun startScheduler() {
        scheduler?.kill()
        scheduler = GameScheduler()
        scheduler?.schedule(PHYSICS_TICK.name, this::processPhysics, 10, true)
        scheduler?.schedule(OVERLAPS_CHECK.name, this::checkOverlaps, 50, true)
    }

    // MOVING POTATO
    private fun processPhysics() {
        potatoObject.processPhysics(
            tiltManager.orientation[2],
            tiltManager.orientation[1] * 2,
            (container.width - potatoImage.drawable.intrinsicWidth).toFloat(),
            (container.height - potatoImage.drawable.intrinsicHeight).toFloat()
        )
        runOnUiThread { updatePotatoPos(potatoObject.pos.x, potatoObject.pos.y) }
    }

    private fun updatePotatoPos(x: Float, y: Float) {
        val layoutParams = potatoImage.layoutParams as LinearLayout.LayoutParams
        layoutParams.leftMargin = x.toInt()
        layoutParams.topMargin = y.toInt()
        layoutParams.rightMargin = 0
        layoutParams.bottomMargin = 0
        potatoImage.layoutParams = layoutParams
    }

    // CHECKING OVERLAPS
    private fun checkOverlaps() {
        playerMapping.forEach { (details, icon) ->
            val taskId = "${GIVE_POTATO.name}-${details.id}"
            val nowOverlapping = isOverlapping(icon, potatoImage)
            val prevOverlapping = prevOverlaps[icon] ?: false

            if (!prevOverlapping && nowOverlapping) {
                runOnUiThread { setHighlighted(icon, true) }
                scheduler?.schedule(taskId, { runOnUiThread { passPotato(details) } }, 3000)
            } else if (prevOverlapping && !nowOverlapping) {
                runOnUiThread { setHighlighted(icon, false) }
                scheduler?.cancelTask(taskId)
            }

            prevOverlaps[icon] = nowOverlapping
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

    private fun passPotato(receiver: ClientDetailsModel) {
        for (i in 1..7) {
            scheduler?.cancelTask("${GIVE_POTATO.name}-$i")
        }

        Toast.makeText(this, "Sending to player ${receiver.id}", Toast.LENGTH_SHORT).show()
        sendToAllNearbyEndpoints(InGameUpdateMessage(timeUntilExpiry, receiver.id), this)
        isPlaying = false
    }

    // POTATO COUNTDOWN
    private fun startPotatoCountdown(timeUntilExpiry: Long) {
        if (timeUntilExpiry < 0) {
            updateTimeUntilPotatoExpiry()
            explodePotato()
            return
        }

        scheduler?.schedule(POTATO_EXPIRE.name, {
            runOnUiThread(this::explodePotato)
        }, timeUntilExpiry)
        scheduler?.schedule(TIME_UNTIL_EXPIRY_UPDATE.name, {
            runOnUiThread(this::updateTimeUntilPotatoExpiry)
        }, 100, true)
        scheduleNextBeep()
    }

    private fun updateTimeUntilPotatoExpiry() {
        remainingTimeText.text = "${timeUntilExpiry / 1000}s remaining"

        var colourFactor = 1 - (timeUntilExpiry.toFloat() / MIN_POTATO_DURATION)
        colourFactor = if (colourFactor < 0) 0f else colourFactor
        potatoImage.setColorFilter(Color.argb(
            (colourFactor * 150).toInt(),
            (colourFactor * 255).toInt(),
            (colourFactor * 16).toInt(),
            0))
    }

    private fun scheduleNextBeep() {
        val timeToNextBeep = when (timeUntilExpiry / 1000) {
            in 0..3 -> 200
            in 3..5 -> 500
            in 5..10 -> 1000
            in 10..20 -> 1500
            else -> 2000
        }
        if (timeToNextBeep > timeUntilExpiry) return

        scheduler?.schedule(BEEP.name, {
            beepHelper.beep(50)
            scheduleNextBeep()
        }, timeToNextBeep.toLong())
    }

    private fun explodePotato() {
        potatoExplosion.explode(potatoImage)
        vibrateManager.vibrate(500)
        Toast.makeText(this, "The potato exploded.", Toast.LENGTH_SHORT).show()
        sendToAllNearbyEndpoints(GameEndMessage(GameInfoHolder.instance.myEndpointId!!), this)
        isPlaying = false

        Handler(Looper.getMainLooper()).postDelayed({
            goToGameOverScreen(GameInfoHolder.instance.myEndpointId!!)
        }, 2000L)
    }

    private fun goToGameOverScreen(loserEndpointId: String) {
        finish()
        val intent = Intent(this, GameOverActivity::class.java)
        intent.putExtra("loserEndpointId", loserEndpointId)
        startActivity(intent)
    }

    // MESSAGE HANDLING
    private val inGameUpdateHandler = { message: InGameUpdateMessage ->
        if (message.dest == GameInfoHolder.instance.myEndpointId) {
            val currentTime = System.currentTimeMillis()
            // Since it's not very fair if someone receives a potato with only 2 seconds left on it, give them some extra time
            val timeLeft = if (message.timeRemaining < 2000) {
                message.timeRemaining + Random(currentTime).nextLong(3000, 6000)
            } else {
                message.timeRemaining
            }
            setToExpireAt = currentTime + timeLeft
            isPlaying = true
        }
    }

    private val gameEndHandler = { message : GameEndMessage ->
        goToGameOverScreen(message.loserEndpointId)
    }
}
