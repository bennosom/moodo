package io.engst.moodo.ui.tasks

import io.engst.moodo.model.types.Task

interface TaskItemClickListener {
    fun onClick(task: Task)
}