package io.engst.moodo.ui.tasks

import io.engst.moodo.model.types.Task
import java.time.LocalDateTime

sealed class ListItem {
    abstract val id: String

    data class GroupItem(
        override val id: String,
        val labelResId: Int,
        val date: LocalDateTime,
        val message: String? = null
    ) : ListItem()

    data class TaskItem(
        override val id: String,
        val dateText: String,
        val task: Task
    ) : ListItem()
}
