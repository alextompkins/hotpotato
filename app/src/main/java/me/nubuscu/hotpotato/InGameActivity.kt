package me.nubuscu.hotpotato

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import me.nubuscu.hotpotato.connection.handler.InGameUpdateHandler
import me.nubuscu.hotpotato.model.ClientDetailsModel
import me.nubuscu.hotpotato.model.dto.GameStateUpdateMessage
import me.nubuscu.hotpotato.model.dto.InGameUpdateMessage
import me.nubuscu.hotpotato.scheduling.GameEvent.*
import me.nubuscu.hotpotato.scheduling.GameScheduler
import me.nubuscu.hotpotato.util.GameInfoHolder
import me.nubuscu.hotpotato.util.PhysicsObject2D
import me.nubuscu.hotpotato.util.TiltManager
import me.nubuscu.hotpotato.util.sendToAllNearbyEndpoints
import tyrantgit.explosionfield.ExplosionField
import kotlin.random.Random


const val MIN_POTATO_DURATION = 5 * 1000L
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

    private lateinit var playerMapping: List<Pair<ClientDetailsModel, ImageView>>
    private val prevOverlaps: MutableMap<ImageView, Boolean> = mutableMapOf()

    private val handler = { message: InGameUpdateMessage ->
        if (message.dest == GameInfoHolder.instance.myEndpointId) {
            setToExpireAt = System.currentTimeMillis() + message.timeRemaining
            isPlaying = true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_in_game)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        tiltManager = TiltManager(this)

        remainingTimeText = findViewById(R.id.remainingTimeText)
        container = findViewById(R.id.container)
        potatoImage = findViewById(R.id.potatoImage)
        potatoImage.setImageResource(
            if (currentTheme == "night_mode") R.drawable.ic_bomb_100dp else R.drawable.ic_potato_100dp
        )
        potatoObject = PhysicsObject2D()
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
        InGameUpdateHandler.addExtraHandler(handler)
    }

    override fun onResume() {
        super.onResume()
        enableFullscreen()
        tiltManager.registerListeners()

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

        // If the game scheduler is running, save the time of potato expiry and kill it
        scheduler?.let {
            setToExpireAt = it.getScheduledTime(POTATO_EXPIRE.name)
            it.kill()
            scheduler = null
        }
    }

    override fun onStop() {
        super.onStop()
        InGameUpdateHandler.removeExtraHandler(handler)
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
        scheduler?.schedule(PHYSICS_TICK.name, {
            runOnUiThread(this::processPhysics)
        }, 10, true)
        scheduler?.schedule(OVERLAPS_CHECK.name, {
            runOnUiThread(this::checkOverlaps)
        }, 100, true)
    }

    // MOVING POTATO
    private fun processPhysics() {
        potatoObject.processPhysics(
            tiltManager.orientation[2],
            tiltManager.orientation[1] * 2,
            (container.width - potatoImage.drawable.intrinsicWidth).toFloat(),
            (container.height - potatoImage.drawable.intrinsicHeight).toFloat()
        )
        updatePotatoPos(potatoObject.pos.x, potatoObject.pos.y)
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
            val prevOverlapping = prevOverlaps[potatoImage] ?: false

            if (!prevOverlapping && nowOverlapping) {
                setHighlighted(icon, true)
                scheduler?.schedule(taskId, { runOnUiThread { passPotato(details) } }, 2000)
            } else if (prevOverlapping && !nowOverlapping) {
                setHighlighted(icon, false)
                scheduler?.cancelTask(taskId)
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

    private fun passPotato(receiver: ClientDetailsModel) {
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
    }

    private fun updateTimeUntilPotatoExpiry() {
        remainingTimeText.text = "${timeUntilExpiry / 1000}s remaining"
    }

    private fun explodePotato() {
        potatoExplosion.explode(potatoImage)
        Toast.makeText(this, "The potato exploded.", Toast.LENGTH_SHORT).show()
        sendToAllNearbyEndpoints(GameStateUpdateMessage(false), this)
        isPlaying = false
    }
}
