package io.engst.moodo.headless

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import io.engst.moodo.BuildConfig
import io.engst.moodo.model.TaskRepository
import io.engst.moodo.shared.Logger
import io.engst.moodo.shared.inject
import io.engst.moodo.shared.injectLogger

class TaskActionReceiver : BroadcastReceiver() {

    enum class Type(val action: String) {
        Done("${BuildConfig.APPLICATION_ID}.intent.action.done"),
        Shift("${BuildConfig.APPLICATION_ID}.intent.action.shift")
    }

    private val logger: Logger by injectLogger("headless")
    private val repository: TaskRepository by inject()

    override fun onReceive(context: Context, intent: Intent) {
        logger.debug { "onReceive $intent with extras ${intent.extraAsString}" }

        when (intent.action) {
            Type.Done.action -> {
                val taskId = intent.getLongExtra(TaskReminderReceiver.ExtraKeyTaskId, -1L)
                repository.setDone(taskId)
            }
            Type.Shift.action -> {
                val taskId = intent.getLongExtra(TaskReminderReceiver.ExtraKeyTaskId, -1L)
                repository.shift(taskId)
            }
        }
    }
}