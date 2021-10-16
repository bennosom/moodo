package io.engst.moodo.model

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.content.getSystemService
import io.engst.moodo.headless.TaskReminderReceiver
import io.engst.moodo.headless.TaskReminderReceiver.Companion.ExtraKeyTaskDescription
import io.engst.moodo.headless.TaskReminderReceiver.Companion.ExtraKeyTaskDueDate
import io.engst.moodo.headless.TaskReminderReceiver.Companion.ExtraKeyTaskId
import io.engst.moodo.model.types.Task
import io.engst.moodo.shared.Logger
import io.engst.moodo.shared.msecsSinceEpoch

class TaskScheduler(
    private val logger: Logger,
    private val context: Context
) {
    private val alarmManager = context.getSystemService<AlarmManager>()

    fun removeSchedule(task: Task) {
        val pendingIntent = buildIntent(task)
        alarmManager?.cancel(pendingIntent)
        pendingIntent.cancel()

        logger.info { "remove reminder for task #${task.id}" }
    }

    fun schedule(task: Task) {
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

    private fun buildIntent(task: Task): PendingIntent {
        val taskId = task.id
        val dueDateMillis = task.dueDate?.msecsSinceEpoch ?: 0L
        val description = task.description

        val intent = Intent(context, TaskReminderReceiver::class.java).apply {
            putExtra(ExtraKeyTaskId, taskId)
            putExtra(ExtraKeyTaskDueDate, dueDateMillis)
            putExtra(ExtraKeyTaskDescription, description)
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
}