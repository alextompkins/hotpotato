package me.nubuscu.hotpotato.scheduling

import android.util.Log
import java.util.Timer
import java.util.TimerTask

class GameScheduler {
    private val timer: Timer = Timer("gameScheduler", true)
    private val tasks: MutableMap<String, TimerTask> = mutableMapOf()

    fun schedule(taskId: String, task: () -> Unit, delay: Long, isRepeating: Boolean = false) {
        val gameTask = object : TimerTask() {
            override fun run() {
                task()
                if (!isRepeating) {
                    tasks.remove(taskId)
                }
            }
        }
        Log.i("foo", "Scheduled new task with ID $taskId")
        when (isRepeating) {
            true -> timer.scheduleAtFixedRate(gameTask, delay, delay)
            false -> timer.schedule(gameTask, delay)
        }
        tasks[taskId] = gameTask
    }

    fun getScheduledTime(taskId: String): Long {
        return tasks[taskId]?.scheduledExecutionTime() ?: -1
    }

    fun cancelTask(taskId: String) {
        val task = tasks[taskId] ?: return
        task.cancel()
        tasks.remove(taskId)
    }

    fun kill() {
        timer.cancel()
    }

    // POTATO COUNTDOWN
    // PHYSICS TICK repeating 10 ms
    //      repeating
    // POTATO PLAYER ICON OVERLAPS CHECK
    //      repeating
    // SEND TO OTHER PLAYER
    //      after 3s
    //      cancellable

}
