package io.engst.moodo.ui.tasks.edit

import androidx.lifecycle.ViewModel
import io.engst.moodo.model.TaskRepository
import io.engst.moodo.model.types.Task
import io.engst.moodo.shared.Logger
import io.engst.moodo.shared.injectLogger
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class TaskEditViewModel(private val taskRepository: TaskRepository) : ViewModel() {

    private val logger: Logger by injectLogger("viewmodel")

    var originalTask: Task? = null
        set(value) {
            field = value

            description = field?.description
            dueDate = field?.dueDate?.toLocalDate()
            dueTime = field?.dueDate?.toLocalTime()

            logger.info { "edit task: $field" }
        }

    var description: String? = null
    var dueDate: LocalDate? = null
    var dueTime: LocalTime? = null

    fun deleteTask() {
        logger.info { "deleteTask" }

        originalTask?.let {
            GlobalScope.launch {
                taskRepository.deleteTask(it)
            }
        } ?: Unit.let {
            logger.warn { "task not existing" }
        }
    }

    fun saveChanges() {
        logger.info { "saveChanges" }

        description?.let {
            if (it.isBlank()) {
                logger.info { "delete task, because text has been removed" }

                deleteTask()
                return@saveChanges
            }
        }

        val dueDateTime = buildDueDateTime()

        originalTask?.let {
            val updatedTask = it.copy(
                description = description ?: "",
                dueDate = dueDateTime,
                doneDate = dueDateTime?.let { null } ?: it.doneDate
            )

            if (hasChanged()) {
                GlobalScope.launch { taskRepository.updateTask(updatedTask) }
            } else {
                logger.info { "nothing changed" }
            }
        } ?: Unit.let {
            val newTask = Task(
                description = description ?: "",
                createdDate = LocalDateTime.now(),
                dueDate = dueDateTime
            )

            if (hasChanged()) {
                GlobalScope.launch { taskRepository.addTask(newTask) }
            } else {
                logger.info { "nothing changed" }
            }
        }
    }

    private fun hasChanged(): Boolean =
        originalTask?.let {
            it.description != description ||
                    it.dueDate != buildDueDateTime()
        } ?: true


    private fun buildDueDateTime(): LocalDateTime? =
        if (dueDate != null) {
            val dueTime = dueTime ?: LocalTime.of(9, 0)
            LocalDateTime.of(dueDate, dueTime)
        } else {
            null
        }
}