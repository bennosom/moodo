package io.engst.moodo.headless

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import io.engst.moodo.MoodoApplication.Companion.CHANNEL_ID
import io.engst.moodo.R
import io.engst.moodo.model.ExtraDescription
import io.engst.moodo.model.ExtraId
import io.engst.moodo.shared.Logger
import io.engst.moodo.shared.injectLogger
import io.engst.moodo.ui.MainActivity

class TaskReminderService : Service() {
    private val logger: Logger by injectLogger()

    private lateinit var vibrator: Vibrator

    override fun onCreate() {
        logger.debug { "onCreate" }

        super.onCreate()

        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val taskId = intent.getLongExtra(ExtraId, -1L)
        val description = intent.getStringExtra(ExtraDescription)

        logger.debug { "onStartCommand taskId=$taskId" }

        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0)

        val actionA: NotificationCompat.Action = NotificationCompat.Action.Builder(
            R.drawable.app_icon_foreground,
            getString(R.string.notification_action_task_done),
            pendingIntent
        ).build()

        val actionB: NotificationCompat.Action = NotificationCompat.Action.Builder(
            R.drawable.app_icon_foreground,
            getString(R.string.notification_action_task_shift),
            pendingIntent
        ).build()

        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentText(description)
            .setSmallIcon(R.drawable.ic_done_all_black_24dp)
            .setContentIntent(pendingIntent)
            .addAction(actionA)
            .addAction(actionB)
            .build()

        vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))

        startForeground(1, notification)

        return START_STICKY
    }

    override fun onDestroy() {
        logger.debug { "onDestroy" }

        super.onDestroy()

        vibrator.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? {
        logger.debug { "onBind $intent" }

        return null
    }
}