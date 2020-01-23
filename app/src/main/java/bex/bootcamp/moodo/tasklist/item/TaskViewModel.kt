package bex.bootcamp.moodo.tasklist.item

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import bex.bootcamp.moodo.domain.Task

class TaskViewModel : ViewModel() {

    private val _task = MutableLiveData<Task>()
    val task: LiveData<Task>
        get() = _task

    init {
        _task.value = Task("foo")
    }

    fun show() {
        // TODO: open task detail view
    }
}