package io.engst.moodo.model

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.content.getSystemService
import io.engst.moodo.headless.TaskReminderReceiver
import io.engst.moodo.model.types.Task
import io.engst.moodo.shared.Logger
import io.engst.moodo.shared.inject
import io.engst.moodo.shared.msecsSinceEpoch
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.launch

class TaskScheduler(
    private val logger: Logger,
    private val context: Context
) {
    private val repository: TaskRepository by inject()

    private val alarmManager = context.getSystemService<AlarmManager>()

    init {
        GlobalScope.launch {
            repository.tasks.collect {
                updateReminder(it)
            }
        }
    }

    fun updateReminder() {
        GlobalScope.launch {
            val tasks = repository.tasks.first()
            updateReminder(tasks)
        }
    }

    private fun updateReminder(tasks: List<Task>) {
        val scheduledTasks = tasks.filter { it.isScheduled }

        if (scheduledTasks.isEmpty()) {
            logger.debug { "no scheduled tasks - clear reminder!" }
            clearReminder()
            return
        }

        val earliestDate = scheduledTasks
            .mapNotNull { it.dueDate }
            .reduce { acc, date ->
                if (date < acc) date else acc
            }

        logger.info { "updateReminder earliestDate=$earliestDate" }

        alarmManager?.set(
            AlarmManager.RTC_WAKEUP,
            earliestDate.msecsSinceEpoch,
            buildPendingIntent()
        )

        logger.info { "set reminder at $earliestDate" }
    }

    private fun clearReminder() {
        buildPendingIntent().apply {
            alarmManager?.cancel(this)
            cancel()
        }
    }

    private fun buildPendingIntent() = PendingIntent.getBroadcast(
        context,
        0,
        Intent(context, TaskReminderReceiver::class.java),
        PendingIntent.FLAG_UPDATE_CURRENT
    )
}