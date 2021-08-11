package bex.bootcamp.moodo.ui.tasks.task

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bex.bootcamp.moodo.model.api.Task
import bex.bootcamp.moodo.model.service.TaskRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class TaskViewModel(private val taskRepository: TaskRepository) : ViewModel() {

    var dueDate: LocalDateTime = LocalDateTime.now()

    fun addTask(task: Task) {
        viewModelScope.launch(Dispatchers.IO) {
            taskRepository.addTask(task)
        }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch(Dispatchers.IO) {
            taskRepository.updateTask(task)
        }
    }
}