package io.engst.moodo.model.persistence.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
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
    val tags: String?,
    val redoCount: Int,
    val shiftCount: Int,
    val priority: Int?
)