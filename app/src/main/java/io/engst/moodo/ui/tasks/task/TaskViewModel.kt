package io.engst.moodo.ui.tasks.task

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.engst.moodo.model.api.Task
import io.engst.moodo.model.service.TaskRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class TaskViewModel(private val taskRepository: TaskRepository) : ViewModel() {

    var description: String = ""
    var dueDate: LocalDateTime = LocalDateTime.now()

    fun addTask(task: Task) {
        viewModelScope.launch(Dispatchers.IO) {
            val nextTaskId = taskRepository.getNextTaskId()
            taskRepository.updateTask(
                task.copy(
                    id = nextTaskId
                )
            )
        }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch(Dispatchers.IO) {
            taskRepository.updateTask(task)
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch(Dispatchers.IO) {
            taskRepository.deleteTask(task)
        }
    }
}