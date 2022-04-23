package io.engst.moodo.model.persistence.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "task")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val task_id: Long,
    val description: String,
    val createdDate: LocalDateTime,
    val dueDate: LocalDateTime?,
    val doneDate: LocalDateTime?,
    val priority: Int?
)