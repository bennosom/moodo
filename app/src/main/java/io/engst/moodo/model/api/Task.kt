package io.engst.moodo.model.api

import java.time.LocalDateTime

const val ExtraDescription = "description"
const val ExtraId = "taskId"

data class Task(
    val id: Long? = null,
    var description: String = "",
    val createdDate: LocalDateTime = LocalDateTime.now(),
    var dueDate: LocalDateTime = LocalDateTime.now(),
    var doneDate: LocalDateTime? = null,
    var redoCount: Int = 0,
    var shiftCount: Int = 0
) {
    val isExpired: Boolean
        get() = dueDate < LocalDateTime.now()

    val isDone: Boolean
        get() = doneDate != null
}