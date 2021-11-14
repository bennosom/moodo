package io.engst.moodo.headless

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import io.engst.moodo.model.TaskNotifications
import io.engst.moodo.model.TaskScheduler
import io.engst.moodo.shared.Logger
import io.engst.moodo.shared.inject
import io.engst.moodo.shared.injectLogger

class TaskReminderReceiver : BroadcastReceiver() {

    private val logger: Logger by injectLogger("headless")
    private val taskScheduler: TaskScheduler by inject()
    private val notifications: TaskNotifications by inject()

    override fun onReceive(context: Context, intent: Intent) {
        logger.debug { "onReceive $intent" }

        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED -> {
                // update reminder because device restarted and former reminder got lost
                taskScheduler.updateReminder()
            }
            else -> {
                // update notifications because alarm fired
                notifications.updateNotifications()
            }
        }
    }
}