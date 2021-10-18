package io.engst.moodo.headless

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import io.engst.moodo.model.NotificationHelper
import io.engst.moodo.model.types.extraTaskDescription
import io.engst.moodo.model.types.extraTaskDueDate
import io.engst.moodo.model.types.extraTaskId
import io.engst.moodo.shared.Logger
import io.engst.moodo.shared.extraAsString
import io.engst.moodo.shared.inject
import io.engst.moodo.shared.injectLogger

class TaskReminderReceiver : BroadcastReceiver() {

    private val logger: Logger by injectLogger("headless")
    private val helper: NotificationHelper by inject()

    override fun onReceive(context: Context, intent: Intent) {
        logger.debug { "onReceive $intent with extras ${intent.extraAsString}" }

        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED -> {
                // TODO: set reminders because device restarted!?
            }
            else -> {
                val id = intent.getLongExtra(extraTaskId, -1L)
                val dueDate = intent.getLongExtra(extraTaskDueDate, 0L)
                val description = intent.getStringExtra(extraTaskDescription) ?: "empty"

                helper.showNotification(id, dueDate, description)
            }
        }
    }
}