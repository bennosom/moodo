package io.engst.moodo.model

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Bundle
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.getSystemService
import io.engst.moodo.R
import io.engst.moodo.headless.TaskActionReceiver
import io.engst.moodo.model.types.Task
import io.engst.moodo.model.types.TaskAction
import io.engst.moodo.model.types.app
import io.engst.moodo.model.types.extraTaskId
import io.engst.moodo.shared.Logger
import io.engst.moodo.shared.msecsSinceEpoch
import io.engst.moodo.ui.MainActivity
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class TaskNotifications(
    private val logger: Logger,
    private val context: Context,
    private val repository: TaskRepository
) {
    companion object {
        private const val ReminderChannelId = "$app.notification.channel.reminders"
        private const val ReminderGroupId = "$app.notification.group.reminders"
        private const val ReminderHashKey = "$app.notification.hash"
    }

    init {
        createNotificationChannel()

        GlobalScope.launch {
            repository.tasks.collect {
                updateNotifications(it)
            }
        }
    }

    fun updateNotifications() {
        GlobalScope.launch {
            val tasks = repository.tasks.first()
            updateNotifications(tasks)
        }
    }

    private fun updateNotifications(tasks: List<Task>) {
        logger.debug { "updateNotifications tasks=$tasks" }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notifications = tasks.filter { it.isDue && it.isScheduled }.map {
            val taskId = it.id!!
            val notificationId = (it.id % (Int.MAX_VALUE - 100)).toInt() + 100

            val donePendingIntent = PendingIntent.getBroadcast(
                context,
                notificationId,
                Intent(context, TaskActionReceiver::class.java).apply {
                    action = TaskAction.Done.action
                    putExtra(extraTaskId, taskId)
                },
                0
            )

            val snoozePendingIntent = PendingIntent.getActivity(
                context,
                notificationId,
                Intent(context, MainActivity::class.java).apply {
                    action = TaskAction.Snooze.action
                    putExtra(extraTaskId, taskId)
                },
                0
            )

            val actionDone: NotificationCompat.Action = NotificationCompat.Action.Builder(
                R.drawable.ic_done_24,
                context.getString(R.string.notification_action_task_done),
                donePendingIntent
            ).build()

            val actionSnooze: NotificationCompat.Action = NotificationCompat.Action.Builder(
                R.drawable.ic_more_time_24,
                context.getString(R.string.notification_action_task_snooze),
                snoozePendingIntent
            ).build()

            val notification = NotificationCompat.Builder(context, ReminderChannelId)
                .setSmallIcon(R.drawable.ic_done_all_24)
                .setContentTitle(it.description)
                .setContentIntent(pendingIntent)
                .addAction(actionSnooze)
                .addAction(actionDone)
                .setWhen(it.dueDate!!.msecsSinceEpoch)
                .setShowWhen(true)
                .setAutoCancel(false)
                .setGroup(ReminderGroupId)
                .setExtras(Bundle().apply {
                    putInt(ReminderHashKey, it.hashCode())
                })
                .setOnlyAlertOnce(true)
                .build()

            it to notification
        }

        val count = notifications.count()
        val summaryTitle = context.resources.getQuantityString(R.plurals.x_tasks_due, count, count)

        val notificationSummary = NotificationCompat.Builder(context, ReminderChannelId)
            .setSmallIcon(R.drawable.ic_done_all_24)
            .setContentIntent(pendingIntent)
            .setContentTitle(summaryTitle)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setGroup(ReminderGroupId)
            .setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_CHILDREN)
            .setGroupSummary(true)
            .build()

        val notificationManager = context.getSystemService<NotificationManager>()!!
        val activeNotifications = notificationManager.activeNotifications.toList()
        val activeNotificationIds = activeNotifications.map { it.id }

        val nextNotifications = notifications.map { it.first.id!!.toInt() }
        val orphanedNotifications = activeNotificationIds - nextNotifications

        NotificationManagerCompat.from(context).apply {
            orphanedNotifications.forEach {
                cancel(it)
            }
            notifications.forEach {
                val id = it.first.id!!.toInt()
                val hash = it.first.hashCode()
                val activeHash = activeNotifications.find { it.id == id }?.notification?.extras?.getInt(
                    ReminderHashKey)
                if (hash != activeHash) {
                    logger.debug { "notify $id" }
                    notify(id, it.second)
                } else {
                    logger.debug { "$id unchanged" }
                }
            }
            //notify(0, notificationSummary)
        }
    }

    private fun createNotificationChannel() {
        logger.debug { "createNotificationChannel" }

        context.getSystemService<NotificationManager>()?.let { notificationManager ->
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