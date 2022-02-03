package io.engst.moodo.headless

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import io.engst.moodo.model.TaskRepository
import io.engst.moodo.model.types.TaskAction
import io.engst.moodo.model.types.extraTaskId
import io.engst.moodo.shared.Logger
import io.engst.moodo.shared.extraAsString
import io.engst.moodo.shared.inject
import io.engst.moodo.shared.injectLogger

class TaskActionReceiver : BroadcastReceiver() {

    private val logger: Logger by injectLogger("notification")
    private val repository: TaskRepository by inject()

    override fun onReceive(context: Context, intent: Intent) {
        logger.debug { "onReceive $intent with extras ${intent.extraAsString}" }

        when (intent.action) {
            TaskAction.ShiftToTomorrow.action -> {
                intent.getLongExtra(extraTaskId, -1L).takeIf { it > 0 }?.let {
                    repository.shiftTo(it, TaskAction.ShiftToTomorrow)
                }
            }
            TaskAction.ShiftToNextWeek.action -> {
                intent.getLongExtra(extraTaskId, -1L).takeIf { it > 0 }?.let {
                    repository.shiftTo(it, TaskAction.ShiftToNextWeek)
                }
            }
            TaskAction.Done.action -> {
                intent.getLongExtra(extraTaskId, -1L).takeIf { it > 0 }?.let {
                    repository.setDone(it)
                }
            }
            else -> {
                logger.error { "unknown action" }
            }
        }
    }
}