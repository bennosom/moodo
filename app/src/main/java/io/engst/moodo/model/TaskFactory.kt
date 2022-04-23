package io.engst.moodo.model

import io.engst.moodo.model.persistence.entity.TaskEntity
import io.engst.moodo.model.types.Tag
import io.engst.moodo.model.types.Task
import java.time.Clock
import java.time.LocalDateTime

class TaskFactory(private val clock: Clock) {
    fun createTask(entity: TaskEntity, tags: List<Tag>): Task = Task(
        id = entity.task_id,
        description = entity.description,
        createdDate = entity.createdDate,
        isDue = entity.dueDate?.let { it < LocalDateTime.now(clock) } ?: false,
        dueDate = entity.dueDate,
        doneDate = entity.doneDate,
        priority = entity.priority ?: 0,
        tags = tags
    )
}

fun Task.toEntity() = TaskEntity(
    task_id = id ?: 0,
    description = description,
    createdDate = createdDate,
    dueDate = dueDate,
    doneDate = doneDate,
    priority = priority
)