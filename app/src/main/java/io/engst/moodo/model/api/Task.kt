package io.engst.moodo.model.api

import java.time.LocalDateTime

const val ExtraDescription = "description"
const val ExtraId = "taskId"

data class Task(
    val id: Long?,
    var description: String,
    val createdDate: LocalDateTime,
    var dueDate: LocalDateTime,
    var doneDate: LocalDateTime?,
    var redoCount: Int,
    var shiftCount: Int
) {
    val isExpired: Boolean
        get() = dueDate < LocalDateTime.now()

    val isDone: Boolean
        get() = doneDate != null
}