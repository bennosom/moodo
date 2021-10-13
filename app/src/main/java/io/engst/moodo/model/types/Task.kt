package io.engst.moodo.model.types

import java.io.Serializable
import java.time.LocalDateTime

data class Task(
    val id: Long? = null,
    var description: String = "",
    val createdDate: LocalDateTime = LocalDateTime.now(),
    var dueDate: LocalDateTime? = null,
    var doneDate: LocalDateTime? = null,
    var redoCount: Int = 0,
    var shiftCount: Int = 0
) : Serializable {
    val done: Boolean
        get() = doneDate != null

    val scheduled: Boolean
        get() = dueDate != null

    val due: Boolean
        get() = dueDate?.let { it <= LocalDateTime.now() } ?: true
}