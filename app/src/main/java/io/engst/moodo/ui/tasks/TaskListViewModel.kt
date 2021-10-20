package io.engst.moodo.ui.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.engst.moodo.model.TaskRepository
import io.engst.moodo.model.types.DateShift
import io.engst.moodo.model.types.Task
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class TaskListViewModel(private val taskRepository: TaskRepository) : ViewModel() {

    companion object {
        const val todayHeaderId = "Today"
    }

    val scrollToToday: MutableStateFlow<Boolean> = MutableStateFlow(false)

    val tasks: Flow<List<ListItem>> = taskRepository.tasks
        .map { sortTasksWithHeader(it) }
        .flowOn(Dispatchers.Default)

    fun shift(task: Task, shiftBy: DateShift) {
        viewModelScope.launch(Dispatchers.Default) {
            val baseDate = when {
                task.scheduled && !task.done && !task.due -> task.dueDate!!
                else -> LocalDateTime.now()
            }

            val shiftedDueDate = when (shiftBy) {
                DateShift.None -> baseDate
                DateShift.OneDay -> baseDate.plusDays(1)
                DateShift.TwoDays -> baseDate.plusDays(2)
                DateShift.OneWeek -> baseDate.plusWeeks(1)
                DateShift.OneMonth -> baseDate.plusMonths(1)
            }

            val update = task.copy(
                dueDate = shiftedDueDate,
                doneDate = null,
                shiftCount = task.shiftCount + 1,
                redoCount = if (task.doneDate != null) task.redoCount + 1 else 0
            )
            taskRepository.updateTask(update)
        }
    }

    fun setDone(task: Task) {
        viewModelScope.launch(Dispatchers.Default) {
            val update = task.copy(doneDate = LocalDateTime.now())
            taskRepository.updateTask(update)
        }
    }

    fun setUndone(task: Task) {
        viewModelScope.launch(Dispatchers.Default) {
            val update = task.copy(
                dueDate = null,
                doneDate = null
            )
            taskRepository.updateTask(update)
        }
    }

    fun undoDelete(task: Task) {
        viewModelScope.launch(Dispatchers.Default) {
            taskRepository.addTask(task)
        }
    }

    fun delete(task: Task) {
        viewModelScope.launch(Dispatchers.Default) {
            taskRepository.deleteTask(task)
        }
    }

    private fun sortTasksWithHeader(tasks: List<Task>): List<ListItem> {
        val dateGroupHelper = TaskListGroupHelper(LocalDate.now())
        val todayDate = LocalDateTime.of(dateGroupHelper.today, LocalTime.MIN)

        val doneList = mutableListOf<TaskListItem>()
        val todayHeader = HeaderListItem(
            id = todayHeaderId,
            date = todayDate
        )
        val dueList = mutableListOf<TaskListItem>()
        val scheduledHeaderList = mutableSetOf<HeaderListItem>()
        val scheduledList = mutableListOf<TaskListItem>()

        tasks.forEach { task ->
            val item = TaskListItem("${task.id!!}", task)
            when {
                task.done -> doneList.add(item)
                !task.done && task.dueDate?.let { it <= LocalDateTime.now() } ?: true -> dueList.add(
                    item
                )
                task.scheduled -> scheduledList.add(item)
            }
        }

        scheduledList.forEach {
            val headerDate =
                LocalDateTime.of(dateGroupHelper.getDateGroupFor(it.task.dueDate), LocalTime.MIN)
            if (headerDate != todayHeader.date) {
                scheduledHeaderList.add(HeaderListItem("$headerDate", headerDate))
            }
        }

        val sortedDoneList = doneList.sortedBy { it.task.doneDate }
        val sortedDueList = dueList.sortedBy { it.task.createdDate }
        val sortedScheduledList = (scheduledHeaderList + scheduledList).sortedBy {
            when (it) {
                is TaskListItem -> it.task.dueDate
                is HeaderListItem -> it.date
            }
        }

        return sortedDoneList + todayHeader + sortedDueList + sortedScheduledList
    }

    fun scrollToToday() {
        scrollToToday.value = !scrollToToday.value
    }
}