package io.engst.moodo.ui.tasks

import io.engst.moodo.model.types.Task

interface ListItemClickListener {
    fun onClick(task: Task)
}