package bex.bootcamp.moodo.model.service.persistence

import androidx.room.Entity
import androidx.room.PrimaryKey
import bex.bootcamp.moodo.model.api.Task
import java.time.LocalDateTime

@Entity(tableName = "task")
data class TaskEntity constructor(
    @PrimaryKey(autoGenerate = true)
    val id: Long?,
    val description: String,
    val createdDate: LocalDateTime,
    val dueDate: LocalDateTime,
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

fun List<TaskEntity>.asDomainModel(): List<Task> = map {
    Task(
        id = it.id!!,
        description = it.description,
        createdDate = it.createdDate,
        dueDate = it.dueDate,
        doneDate = it.doneDate,
        redoCount = it.redoCount,
        shiftCount = it.shiftCount
    )
}