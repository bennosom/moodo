package io.engst.moodo.headless

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.annotation.RequiresApi
import io.engst.moodo.shared.Logger
import io.engst.moodo.shared.injectLogger

class TaskReminderService : Service() {
    private val logger: Logger by injectLogger("headless")

    private lateinit var vibrator: Vibrator

    override fun onCreate() {
        super.onCreate()

        logger.debug { "onCreate" }
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        logger.debug { "onStartCommand" }

        vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))

        //startForeground(1, notification)

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()

        logger.debug { "onDestroy" }
        vibrator.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? {
        logger.debug { "onBind $intent" }

        return null
    }
}