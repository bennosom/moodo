package io.engst.moodo.headless

import android.app.Notification
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import io.engst.moodo.MoodoApplication
import io.engst.moodo.R
import io.engst.moodo.model.ExtraDescription
import io.engst.moodo.model.ExtraId
import io.engst.moodo.shared.Logger
import io.engst.moodo.shared.injectLogger
import io.engst.moodo.ui.MainActivity


class TaskReminderReceiver : BroadcastReceiver() {
    private val logger: Logger by injectLogger()

    override fun onReceive(context: Context, intent: Intent) {
        logger.debug { "onReceive $intent with extras ${intent.extras}" }

        val id = intent.getLongExtra(ExtraId, -1L)
        val description = intent.getStringExtra(ExtraDescription) ?: ""

        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED -> {
                /*NotificationScheduler.setReminder(
                    context,
                    AlarmBroadcastReceiver::class.java,
                    localData.get_hour(),
                    localData.get_min()
                )*/
            }
            //else -> startAlarmService(context, intent)
            else -> {
                showReminder(context, id, description)
                /*NotificationScheduler.showNotification(
                    context, MainActivity::class.java,
                    description
                )*/
            }
        }
    }

    private fun startAlarmService(context: Context, intent: Intent) {
        val serviceIntent = Intent(context, TaskReminderService::class.java).apply {
            intent.extras?.let { putExtras(it) }
        }
        context.startForegroundService(serviceIntent)
    }

    private fun showReminder(context: Context, id: Long, description: String) {
        logger.debug { "showReminder id=$id" }

        val notificationIntent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0)

        val actionA: NotificationCompat.Action = NotificationCompat.Action.Builder(
            R.drawable.app_icon_foreground,
            context.getString(R.string.notification_action_task_done),
            pendingIntent
        ).build()

        val actionB: NotificationCompat.Action = NotificationCompat.Action.Builder(
            R.drawable.app_icon_foreground,
            context.getString(R.string.notification_action_task_shift),
            pendingIntent
        ).build()

        val notification: Notification = NotificationCompat.Builder(
            context,
            MoodoApplication.CHANNEL_ID
        )
            .setContentText(description)
            .setSmallIcon(R.drawable.ic_done_all_black_24dp)
            .setContentIntent(pendingIntent)
            .addAction(actionA)
            .addAction(actionB)
            .setAutoCancel(true)
            .build()

        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.notify(0, notification)
    }
}