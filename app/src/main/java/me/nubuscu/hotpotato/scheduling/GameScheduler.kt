package me.nubuscu.hotpotato.scheduling

import java.util.Timer
import java.util.TimerTask

class GameScheduler {
    private val timer: Timer = Timer("gameScheduler", true)
    private val tasks: MutableMap<String, GameTask> = mutableMapOf()
    private var killed = false

    fun schedule(taskId: String, task: () -> Unit, delay: Long, isRepeating: Boolean = false) {
        if (killed) return

        val postExecute: () -> Unit = if (isRepeating) ({}) else ({ tasks.remove(taskId) })
        val gameTask = GameTask(task, postExecute, delay, isRepeating)

        when (isRepeating) {
            true -> timer.scheduleAtFixedRate(gameTask, delay, delay)
            false -> timer.schedule(gameTask, delay)
        }
        tasks[taskId] = gameTask
    }

    fun getScheduledTime(taskId: String): Long? = tasks[taskId]?.nextExecutionTime

    fun cancelTask(taskId: String) {
        val task = tasks[taskId] ?: return
        task.cancel()
        tasks.remove(taskId)
    }

    fun kill() {
        killed = true
        timer.cancel()
    }
}

class GameTask(
    private val execute: () -> Unit,
    private val postExecute: () -> Unit,
    private val delay: Long,
    private val isRepeating: Boolean = false
): TimerTask() {
    var nextExecutionTime = System.currentTimeMillis() + delay

    override fun run() {
        execute()
        postExecute()
        if (isRepeating) {
            nextExecutionTime = System.currentTimeMillis() + delay
        }
    }
}
