package io.engst.moodo.model

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.RingtoneManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.getSystemService
import io.engst.moodo.R
import io.engst.moodo.headless.TaskActionReceiver
import io.engst.moodo.headless.TaskReminderReceiver.Companion.ExtraKeyTaskId
import io.engst.moodo.moodo
import io.engst.moodo.shared.Logger
import io.engst.moodo.ui.MainActivity


class NotificationHelper(val logger: Logger, val context: Context) {

    companion object {
        private const val ReminderChannelId = "$moodo.notification.channel.reminders"
        private const val ReminderGroupId = "$moodo.notification.group.reminders"
    }

    init {
        createNotificationChannel()
    }

    fun showNotification(taskId: Long, dueDateMillis: Long, description: String) {
        logger.debug { "showNotification for task #$taskId" }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notificationId = (taskId % Int.MAX_VALUE).toInt() + 100
        val notificationSummaryId = 0

        val doneIntent = Intent(context, TaskActionReceiver::class.java).apply {
            action = TaskActionReceiver.Type.Done.action
            putExtra(ExtraKeyTaskId, taskId)
        }
        val donePendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId,
            doneIntent,
            0
        )
        logger.debug { "showNotification: doneIntent=$doneIntent" }

        val shiftIntent = Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            putExtra(ExtraKeyTaskId, taskId)
        }
        val shiftPendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            shiftIntent,
            0
        )
        logger.debug { "showNotification: shiftIntent=$shiftIntent" }

        val actionDone: NotificationCompat.Action = NotificationCompat.Action.Builder(
            R.drawable.ic_done_white_24dp,
            context.getString(R.string.notification_action_task_done),
            donePendingIntent
        ).build()

        val actionShift: NotificationCompat.Action = NotificationCompat.Action.Builder(
            R.drawable.ic_navigate_next_white_24dp,
            context.getString(R.string.notification_action_task_shift),
            shiftPendingIntent
        ).build()

        val notificationTask: Notification = NotificationCompat.Builder(context, ReminderChannelId)
            .setSmallIcon(R.drawable.ic_baseline_schedule_24)
            .setContentTitle(description)
            .setContentIntent(pendingIntent)
            .addAction(actionDone)
            .addAction(actionShift)
            .setWhen(dueDateMillis)
            .setShowWhen(true)
            .setAutoCancel(false)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setGroup(ReminderGroupId)
            .build()

        val notificationSummary = NotificationCompat.Builder(context, ReminderChannelId)
            .setSmallIcon(R.drawable.ic_done_all_black_24dp)
            .setStyle(
                NotificationCompat.InboxStyle()
                    .addLine("a b")
                    .addLine("c d")
                    .setBigContentTitle("2 new messages")
                    .setSummaryText("bla blubber")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setGroup(ReminderGroupId)
            .setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_CHILDREN)
            .setGroupSummary(true)
            .build()

        NotificationManagerCompat.from(context).apply {
            notify(notificationId, notificationTask)
            notify(notificationSummaryId, notificationSummary)
        }
        logger.debug { "showNotification: id=$notificationId notification=$notificationTask" }
    }

    private fun createNotificationChannel() {
        context.getSystemService<NotificationManager>()?.let { notificationManager ->
            // channel
            val channel = NotificationChannel(
                ReminderChannelId,
                context.getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(R.string.notification_channel_description)
                enableLights(true)
                enableVibration(true)
                setShowBadge(true)
                setSound(
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION),
                    Notification.AUDIO_ATTRIBUTES_DEFAULT
                )
                lightColor = Color.BLUE
                lockscreenVisibility = Notification.VISIBILITY_PRIVATE
            }
            notificationManager.createNotificationChannel(channel)
        } ?: Unit.let {
            logger.error { "Failed to get system service ${NotificationManager::class.java}" }
        }
    }
}