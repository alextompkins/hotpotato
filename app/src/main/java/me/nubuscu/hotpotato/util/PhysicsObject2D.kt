package me.nubuscu.hotpotato.util


const val FRICTION_COEFF = 0.95f


data class Vector2D(var x: Float, var y: Float)


class PhysicsObject2D(
    initPosX: Float = 0f,
    initPosY: Float = 0f
) {
    var pos = Vector2D(initPosX, initPosY)
    var vel = Vector2D(0f, 0f)

    fun processPhysics(accelX: Float, accelY: Float, maxX: Float, maxY: Float) {
        // Update positions
        pos.x = pos.x.addWithinBounds(vel.x, 0f, maxX)
        pos.y = pos.y.addWithinBounds(vel.y, 0f, maxY)

        // If on edge, bounce
        if (pos.x == 0f || pos.x == maxX) {
            vel.x = -vel.x
        }
        if (pos.y == 0f || pos.y == maxY) {
            vel.y = -vel.y
        }

        // Update velocities
        vel.x += accelX
        vel.y -= accelY * 2

        // Slow down gradually over time
        vel.x *= FRICTION_COEFF
        vel.y *= FRICTION_COEFF
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
