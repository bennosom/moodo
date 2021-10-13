package io.engst.moodo.headless

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import io.engst.moodo.BuildConfig
import io.engst.moodo.model.NotificationHelper
import io.engst.moodo.shared.Logger
import io.engst.moodo.shared.inject
import io.engst.moodo.shared.injectLogger

val Intent.extraAsString: String?
    get() = extras?.keySet()?.joinToString(", ", "[", "]") {
        "$it => ${extras?.get(it)}"
    }

class TaskReminderReceiver : BroadcastReceiver() {

    companion object {
        const val ExtraKeyTaskId = BuildConfig.APPLICATION_ID + ".intent.extra.TaskId"
        const val ExtraKeyTaskDueDate = BuildConfig.APPLICATION_ID + ".intent.extra.TaskDueDate"
        const val ExtraKeyTaskDescription =
            BuildConfig.APPLICATION_ID + ".intent.extra.TaskDescription"
    }

    private val logger: Logger by injectLogger("headless")
    private val helper: NotificationHelper by inject()

    override fun onReceive(context: Context, intent: Intent) {
        logger.debug { "onReceive $intent with extras ${intent.extraAsString}" }

        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED -> {
                // TODO: set reminders because device restarted!?
            }
            else -> {
                val id = intent.getLongExtra(ExtraKeyTaskId, -1L)
                val dueDate = intent.getLongExtra(ExtraKeyTaskDueDate, 0L)
                val description = intent.getStringExtra(ExtraKeyTaskDescription) ?: "empty"

                // startAlarmService(context, intent)
                helper.showNotification(id, dueDate, description)
            }
        }
    }

    /*
    private fun startAlarmService(context: Context, intent: Intent) {
        val serviceIntent = Intent(context, TaskReminderService::class.java).apply {
            intent.extras?.let { putExtras(it) }
        }
        context.startForegroundService(serviceIntent)
    }
     */
}