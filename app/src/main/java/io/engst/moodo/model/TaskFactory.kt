package io.engst.moodo.model

import io.engst.moodo.model.persistence.entity.TaskEntity
import io.engst.moodo.model.types.Task
import java.time.Clock
import java.time.LocalDateTime

class TaskFactory(private val clock: Clock) {

    fun createTask(entity: TaskEntity): Task =
        Task(
            id = entity.id!!,
            description = entity.description,
            createdDate = entity.createdDate,
            isDue = entity.dueDate?.let { it < LocalDateTime.now(clock) } ?: false,
            dueDate = entity.dueDate,
            doneDate = entity.doneDate,
            priority = entity.priority ?: 0,
            tags = emptyList()
        )

    fun createTaskList(list: List<TaskEntity>): List<Task> = list.map { createTask(it) }
}


fun Task.toEntity() = TaskEntity(
    id = id,
    description = description,
    createdDate = createdDate,
    dueDate = dueDate,
    doneDate = doneDate,
    tags = "",
    redoCount = 0,
    shiftCount = 0,
    priority = priority
)