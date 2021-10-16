package io.engst.moodo.model.persistence

import androidx.room.Entity
import androidx.room.PrimaryKey
import io.engst.moodo.model.types.Task
import java.time.LocalDateTime

@Entity(tableName = "task")
data class TaskEntity constructor(
    @PrimaryKey(autoGenerate = true) val id: Long?,
    val description: String,
    val createdDate: LocalDateTime,
    val dueDate: LocalDateTime?,
    val doneDate: LocalDateTime?,
    val redoCount: Int,
    val shiftCount: Int
) {
    companion object {
        fun from(task: Task): TaskEntity =
            TaskEntity(
                id = task.id,
                description = task.description,
                createdDate = task.createdDate,
                dueDate = task.dueDate,
                doneDate = task.doneDate,
                redoCount = task.redoCount,
                shiftCount = task.shiftCount
            )
    }
}

fun List<TaskEntity>.toTaskList(): List<Task> = map { it.toTask() }

fun TaskEntity.toTask(): Task =
    Task(
        id = id!!,
        description = description,
        createdDate = createdDate,
        dueDate = dueDate,
        doneDate = doneDate,
        redoCount = redoCount,
        shiftCount = shiftCount
    )