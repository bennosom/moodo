package io.engst.moodo.model.types

import java.io.Serializable
import java.time.LocalDateTime

data class Task(
    val id: Long? = null,
    var description: String = "",
    val createdDate: LocalDateTime,
    var dueDate: LocalDateTime? = null,
    val isDue: Boolean,
    var doneDate: LocalDateTime? = null,
    val priority: Int
) : Serializable {
    val isDone: Boolean
        get() = doneDate != null

    val isScheduled: Boolean
        get() = doneDate == null && dueDate != null

    val isBacklog: Boolean
        get() = doneDate == null && dueDate == null
}