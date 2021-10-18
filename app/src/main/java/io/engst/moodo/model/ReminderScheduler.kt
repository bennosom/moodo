package io.engst.moodo.model

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.content.getSystemService
import io.engst.moodo.headless.TaskReminderReceiver
import io.engst.moodo.model.types.Task
import io.engst.moodo.model.types.extraTaskDescription
import io.engst.moodo.model.types.extraTaskDueDate
import io.engst.moodo.model.types.extraTaskId
import io.engst.moodo.shared.Logger
import io.engst.moodo.shared.inject
import io.engst.moodo.shared.msecsSinceEpoch

class ReminderScheduler(
    private val logger: Logger,
    private val context: Context
) {
    private val helper: NotificationHelper by inject()

    private val alarmManager = context.getSystemService<AlarmManager>()

    fun clearReminder(task: Task) {
        logger.info { "clearReminder #${task.id}" }

        val pendingIntent = buildIntent(task)
        alarmManager?.cancel(pendingIntent)
        pendingIntent.cancel()

        removeNotifications(task)
    }

    fun updateReminder(task: Task) {
        logger.info { "updateReminder #${task.id}" }

        removeNotifications(task)

        if (task.scheduled) {
            task.dueDate?.let {
                val alarmIntent = buildIntent(task)
                val triggerAtMillis = task.dueDate!!.msecsSinceEpoch

                logger.debug { "alarm at $triggerAtMillis for with intent $alarmIntent" }

                // TODO: schedule inexact wakeup for default reminders, but exact wakeup for user specified reminders
                alarmManager?.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    alarmIntent
                )

                logger.info { "set reminder for task #${task.id}: ${task.dueDate}" }
            } ?: Unit.let {
                logger.info { "no reminder for task #${task.id}" }
            }
        }
    }

    private fun buildIntent(task: Task): PendingIntent {
        val taskId = task.id
        val dueDateMillis = task.dueDate?.msecsSinceEpoch ?: 0L
        val description = task.description

        val intent = Intent(context, TaskReminderReceiver::class.java).apply {
            putExtra(extraTaskId, taskId)
            putExtra(extraTaskDueDate, dueDateMillis)
            putExtra(extraTaskDescription, description)
        }

        logger.debug { "buildIntent: intent=$intent" }

        val requestCode = (task.id!! % Int.MAX_VALUE).toInt()
        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    private fun removeNotifications(task: Task) {
        helper.removeNotification(task.id!!)
    }
}