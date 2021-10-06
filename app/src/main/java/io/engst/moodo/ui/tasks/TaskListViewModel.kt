package io.engst.moodo.ui.tasks

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.engst.moodo.model.DateShift
import io.engst.moodo.model.Task
import io.engst.moodo.model.TaskRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class TaskListViewModel(private val taskRepository: TaskRepository) : ViewModel() {

    val tasks: LiveData<List<Task>> = taskRepository.tasks

    fun shift(task: Task, shiftBy: DateShift) {
        viewModelScope.launch(Dispatchers.Default) {
            val due = maxOf(task.dueDate ?: LocalDateTime.now(), LocalDateTime.now().minusDays(1))
            val update = task.copy(
                dueDate = when (shiftBy) {
                    DateShift.OneDay -> due.plusDays(1)
                    DateShift.TwoDays -> due.plusDays(2)
                    DateShift.OneWeek -> due.plusWeeks(1)
                    DateShift.OneMonth -> due.plusMonths(1)
                },
                doneDate = null,
                shiftCount = task.shiftCount + 1,
                redoCount = if (task.doneDate != null) task.redoCount + 1 else 0
            )
            taskRepository.updateTask(update)
        }
    }

    fun resolve(task: Task) {
        viewModelScope.launch(Dispatchers.Default) {
            val update = task.copy(doneDate = LocalDateTime.now())
            taskRepository.updateTask(update)
        }
    }

    fun delete(task: Task) {
        viewModelScope.launch(Dispatchers.Default) {
            taskRepository.deleteTask(task)
        }
    }
}

