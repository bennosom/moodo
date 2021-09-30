package io.engst.moodo.ui.tasks.task

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.engst.moodo.model.api.Task
import io.engst.moodo.model.service.TaskRepository
import io.engst.moodo.shared.Logger
import io.engst.moodo.shared.injectLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime

class TaskViewModel(private val taskRepository: TaskRepository) : ViewModel() {

    private val logger: Logger by injectLogger("viewmodel")

    var taskText: String? = null
        set(value) {
            field = value
            logger.info { "set text: $field" }
        }

    var taskDate: LocalDate? = null
        set(value) {
            field = value
            logger.info { "set date: $field" }
        }

    var taskTime: LocalTime? = null
        set(value) {
            field = value
            logger.info { "set time: $field" }
        }

    fun addTask(task: Task) {
        viewModelScope.launch(Dispatchers.IO) {
            logger.debug { "add $task" }
            taskRepository.addTask(task)
        }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch(Dispatchers.IO) {
            logger.debug { "update $task" }
            taskRepository.updateTask(task)
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch(Dispatchers.IO) {
            logger.debug { "delete $task" }
            taskRepository.deleteTask(task)
        }
    }
}