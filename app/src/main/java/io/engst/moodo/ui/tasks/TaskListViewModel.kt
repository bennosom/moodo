package io.engst.moodo.ui.tasks

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import io.engst.moodo.model.api.DateShift
import io.engst.moodo.model.api.Task
import io.engst.moodo.model.service.TaskRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class TaskListViewModel(private val taskRepository: TaskRepository) : ViewModel() {

    private val viewModelJob = SupervisorJob()
    private val viewModelScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    val tasks: LiveData<List<Task>> = taskRepository.tasks

    init {
        viewModelScope.launch {
            //repository.insertDummyData()
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    fun shift(task: Task, shiftBy: DateShift) {
        viewModelScope.launch {
            val due = maxOf(task.dueDate, LocalDateTime.now().minusDays(1))
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
        viewModelScope.launch {
            val update = task.copy(doneDate = LocalDateTime.now())
            taskRepository.updateTask(update)
        }
    }

    fun delete(task: Task) {
        viewModelScope.launch {
            taskRepository.deleteTask(task)
        }
    }
}

