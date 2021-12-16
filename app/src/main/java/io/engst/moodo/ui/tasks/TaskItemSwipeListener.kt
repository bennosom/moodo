package io.engst.moodo.ui.tasks

import io.engst.moodo.model.types.DateShift
import io.engst.moodo.model.types.Task

interface TaskItemSwipeListener {
    fun onDone(task: Task)
    fun onRemoved(task: Task)
    fun onShift(task: Task, shiftBy: DateShift)
}
