package io.engst.moodo.ui.tasks.edit

import androidx.lifecycle.ViewModel
import io.engst.moodo.model.TaskRepository
import io.engst.moodo.model.types.Task
import io.engst.moodo.shared.Logger
import io.engst.moodo.shared.injectLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime

class TaskEditViewModel(private val taskRepository: TaskRepository) : ViewModel() {

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
        GlobalScope.launch(Dispatchers.Default) {
            taskRepository.addTask(task)
        }
    }

    fun updateTask(task: Task) {
        GlobalScope.launch(Dispatchers.Default) {
            taskRepository.updateTask(task)
        }
    }

    fun deleteTask(task: Task) {
        GlobalScope.launch(Dispatchers.Default) {
            taskRepository.deleteTask(task)
        }
    }
}