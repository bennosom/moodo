package io.engst.moodo.headless

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import io.engst.moodo.model.TaskRepository
import io.engst.moodo.moodoIntentActionPrefix
import io.engst.moodo.shared.Logger
import io.engst.moodo.shared.inject
import io.engst.moodo.shared.injectLogger

class TaskActionReceiver : BroadcastReceiver() {

    enum class Type(val action: String) {
        Done("$moodoIntentActionPrefix.done")
    }

    private val logger: Logger by injectLogger("headless")
    private val repository: TaskRepository by inject()

    override fun onReceive(context: Context, intent: Intent) {
        logger.debug { "onReceive $intent with extras ${intent.extraAsString}" }

        when (intent.action) {
            Type.Done.action -> {
                val taskId = intent.getLongExtra(TaskReminderReceiver.ExtraKeyTaskId, -1L)
                if (taskId != -1L) {
                    repository.setDone(taskId)
                } else {
                    logger.error { "Invalid task id" }
                }
            }
        }
    }
}