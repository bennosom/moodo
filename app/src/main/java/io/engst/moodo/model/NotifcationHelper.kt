package io.engst.moodo.model

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import io.engst.moodo.BuildConfig
import io.engst.moodo.R
import io.engst.moodo.headless.TaskActionReceiver
import io.engst.moodo.headless.TaskReminderReceiver.Companion.ExtraKeyTaskId
import io.engst.moodo.shared.Logger
import io.engst.moodo.ui.MainActivity

class NotificationHelper(val logger: Logger, val context: Context) {

    companion object {
        private const val NotificationChannel =
            BuildConfig.APPLICATION_ID + ".notification.channel.main"
        private const val NotificationGroup = BuildConfig.APPLICATION_ID + ".notification.group.all"
    }

    init {
        createNotificationChannel()
    }

    fun showNotification(taskId: Long, dueDateMillis: Long, description: String) {
        logger.debug { "showNotification for task #$taskId" }

        val ringTone = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val pendingIntent =
            PendingIntent.getActivity(context, 0, Intent(context, MainActivity::class.java), 0)

        val pendingIntentDone =
            PendingIntent.getBroadcast(context, 0, Intent().apply {
                action = TaskActionReceiver.Type.Done.action
                putExtra(ExtraKeyTaskId, taskId)
            }, 0)

        val pendingIntentShift =
            PendingIntent.getBroadcast(context, 0, Intent().apply {
                action = TaskActionReceiver.Type.Shift.action
                putExtra(ExtraKeyTaskId, taskId)
            }, 0)

        val actionA: NotificationCompat.Action = NotificationCompat.Action.Builder(
            R.drawable.app_icon_foreground,
            context.getString(R.string.notification_action_task_done),
            pendingIntentDone
        ).build()

        val actionB: NotificationCompat.Action = NotificationCompat.Action.Builder(
            R.drawable.app_icon_foreground,
            context.getString(R.string.notification_action_task_shift),
            pendingIntentShift
        ).build()

        val notificationId = (dueDateMillis % Int.MAX_VALUE).toInt()
        val notification: Notification = NotificationCompat.Builder(context, NotificationChannel)
            .setSmallIcon(R.drawable.ic_done_all_black_24dp)
            .setSound(ringTone)
            .setContentTitle(description)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .addAction(actionA)
            .addAction(actionB)
            //.setGroup(NotificationGroup)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).apply {
            notify(notificationId, notification)
        }
    }

    private fun createNotificationChannel() {
        val serviceChannel = NotificationChannel(
            NotificationChannel,
            "Moodo Task Reminder",
            NotificationManager.IMPORTANCE_HIGH
        )
        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(serviceChannel)
    }
}