package io.engst.moodo.model.persistence

import androidx.room.Entity
import androidx.room.PrimaryKey
import io.engst.moodo.model.types.Task
import java.time.LocalDateTime

@Entity(
    tableName = "task"
)
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long?,
    val description: String,
    val createdDate: LocalDateTime,
    val dueDate: LocalDateTime?,
    val doneDate: LocalDateTime?,
    val redoCount: Int,
    val shiftCount: Int,
    val priority: Int?
)

fun Task.toEntity() = TaskEntity(
    id = id,
    description = description,
    createdDate = createdDate,
    dueDate = dueDate,
    doneDate = doneDate,
    redoCount = 0,
    shiftCount = 0,
    priority = priority
)