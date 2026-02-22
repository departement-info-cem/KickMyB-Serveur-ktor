package ca.cem.ktormyb.service

import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

/**
 * Runs a background coroutine that fires once per day at 03:00 (server time)
 * and sends push notifications for tasks due the following day.
 */
class NotificationScheduler(
    private val taskService: TaskService,
    private val firebaseService: FirebaseService
) {
    private val logger = LoggerFactory.getLogger(NotificationScheduler::class.java)
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun start() {
        scope.launch {
            while (isActive) {
                delay(millisUntilNext3am())
                try {
                    notifyTasksDueTomorrow()
                } catch (e: Exception) {
                    logger.error("Notification scheduler error", e)
                }
            }
        }
        logger.info("Notification scheduler started (fires daily at 03:00)")
    }

    fun stop() = scope.cancel()

    private suspend fun notifyTasksDueTomorrow() {
        val zone = ZoneId.systemDefault()
        val tomorrow = LocalDate.now().plusDays(1)
        val start = tomorrow.atStartOfDay(zone).toInstant().toEpochMilli()
        val end = tomorrow.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()

        taskService.allTasksForScheduler()
            .filter { (_, deadline, token) -> deadline in start until end && !token.isNullOrBlank() }
            .forEach { (taskId, _, token) ->
                firebaseService.sendNotification(token!!, "Tâche à remettre demain", "Une de vos tâches est due demain !")
                logger.info("Notification sent for task $taskId")
            }
    }

    private fun millisUntilNext3am(): Long {
        val now = LocalDateTime.now()
        var next3am = now.toLocalDate().atTime(LocalTime.of(3, 0))
        if (!next3am.isAfter(now)) next3am = next3am.plusDays(1)
        val millis = next3am.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() - System.currentTimeMillis()
        return millis.coerceAtLeast(0)
    }
}
