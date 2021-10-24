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

            description = field?.description ?: ""
            dueDate = field?.dueDate?.toLocalDate()
            dueTime = field?.dueDate?.toLocalTime()

            logger.info { "edit task: $field" }
        }

    var description: String = ""
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

        val dueDateTime = buildDueDate()

        originalTask?.let { original ->
            if (description.isBlank()) {
                logger.info { "delete task, because description has been removed" }

                deleteTask()
                return@saveChanges
            }

            val updatedTask = original.copy(
                description = description,
                dueDate = dueDateTime
            )

            // fix done date if due date has changed
            if (dueDateTime != original.dueDate) {
                updatedTask.doneDate = null
            }

            if (hasDescriptionOrDueDateChanged()) {
                GlobalScope.launch { taskRepository.updateTask(updatedTask) }
            } else {
                logger.info { "nothing changed" }
            }
        } ?: Unit.let {
            val newTask = Task(
                description = description,
                createdDate = LocalDateTime.now(),
                dueDate = dueDateTime
            )

            if (hasDescriptionOrDueDateChanged()) {
                GlobalScope.launch { taskRepository.addTask(newTask) }
            } else {
                logger.info { "nothing changed" }
            }
        }
    }

    private fun hasDescriptionOrDueDateChanged(): Boolean =
        originalTask?.let {
            it.description != description ||
                    it.dueDate != buildDueDate()
        } ?: true


    private fun buildDueDate(): LocalDateTime? =
        dueDate?.let {
            LocalDateTime.of(dueDate, dueTime ?: LocalTime.of(9, 0))
        }

    fun addNewTask() {
        if (description.isBlank()) {
            logger.error { "emtpy description, do nothing" }
            return
        }
        val newTask = Task(
            description = description,
            createdDate = LocalDateTime.now(),
            dueDate = buildDueDate()
        )
        GlobalScope.launch { taskRepository.addTask(newTask) }
    }

    fun setTaskDone() {
        originalTask?.let { original ->
            val updatedTask = original.copy(
                doneDate = LocalDateTime.now()
            )
            GlobalScope.launch { taskRepository.updateTask(updatedTask) }
        } ?: Unit.let {
            logger.error { "failed to set task to done: no task set" }
        }
    }
}