package bex.bootcamp.moodo.tasklist

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import bex.bootcamp.moodo.domain.Task
import java.time.LocalDateTime

class TaskListViewModel : ViewModel() {

    val _tasks = MutableLiveData<List<Task>>()
    val tasks: LiveData<List<Task>>
        get() = _tasks

    init {
        _tasks.value = listOf(
            Task("-3d", due = LocalDateTime.now().minusDays(3)),
            Task("now", due = LocalDateTime.now()),
            Task("+m30", due = LocalDateTime.now().plusMinutes(30)),
            Task("+H4", due = LocalDateTime.now().plusHours(4)),
            Task("+d1", due = LocalDateTime.now().plusDays(1)),
            Task("+d1", due = LocalDateTime.now().plusDays(1)),
            Task("+d3", due = LocalDateTime.now().plusDays(3)),
            Task("+w1", due = LocalDateTime.now().plusWeeks(1)),
            Task("+w2", due = LocalDateTime.now().plusWeeks(2)),
            Task("+w3", due = LocalDateTime.now().plusWeeks(3)),
            Task("+w4", due = LocalDateTime.now().plusWeeks(4)),
            Task("+w5", due = LocalDateTime.now().plusWeeks(5))
        )
    }
}

class ListViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TaskListViewModel::class.java)) {
            return TaskListViewModel() as T
        }
        throw IllegalArgumentException("Unknown viewmodel type")
    }
}