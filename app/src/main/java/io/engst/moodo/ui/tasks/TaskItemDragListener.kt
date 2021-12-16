package io.engst.moodo.ui.tasks

import io.engst.moodo.model.types.Task

interface TaskItemDragListener {
    fun canDrag(dragTask: Task): Boolean
    fun onDragStart(dragTask: Task)
    fun canDrop(dragTask: Task, dropTask: Task): Boolean
    fun onDragMove(dragTask: Task, dropTask: Task) {}
    fun onDrop(dragTask: Task, dropTask: Task)
}
