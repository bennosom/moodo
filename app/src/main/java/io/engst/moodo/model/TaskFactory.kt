package io.engst.moodo.model

import io.engst.moodo.model.persistence.TaskEntity
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
            doneDate = entity.doneDate
        )

    fun createTaskList(list: List<TaskEntity>): List<Task> = list.map { createTask(it) }
}