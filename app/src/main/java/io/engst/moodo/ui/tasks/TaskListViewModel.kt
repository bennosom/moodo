package io.engst.moodo.ui.tasks

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.engst.moodo.R
import io.engst.moodo.model.TaskRepository
import io.engst.moodo.model.types.DateShift
import io.engst.moodo.model.types.Task
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.LocalDateTime
import java.util.*

class TaskListViewModel(
    private val context: Context,
    private val dispatcher: CoroutineDispatcher,
    private val taskRepository: TaskRepository,
    private val clock: Clock,
    private val locale: Locale
) : ViewModel() {

    val scrollToToday: MutableStateFlow<Boolean> = MutableStateFlow(false)

    val tasks: Flow<List<ListItem>> = taskRepository.tasks
        .map { sortTasksWithHeader(it) }
        .flowOn(dispatcher)

    fun shift(task: Task, shiftBy: DateShift) {
        viewModelScope.launch(dispatcher) {
            val baseDate = when {
                task.isScheduled && !task.isDone && !task.isDue -> task.dueDate!!
                else -> LocalDateTime.now(clock)
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
                doneDate = null
            )
            taskRepository.updateTask(update)
        }
    }

    fun setDone(task: Task) {
        viewModelScope.launch(dispatcher) {
            val update = task.copy(doneDate = LocalDateTime.now(clock))
            taskRepository.updateTask(update)
        }
    }

    fun setUndone(task: Task) {
        viewModelScope.launch(dispatcher) {
            val update = task.copy(
                dueDate = null,
                doneDate = null
            )
            taskRepository.updateTask(update)
        }
    }

    fun undoDelete(task: Task) {
        viewModelScope.launch(dispatcher) {
            taskRepository.addTask(task)
        }
    }

    fun delete(task: Task) {
        viewModelScope.launch(dispatcher) {
            taskRepository.deleteTask(task)
        }
    }

    private fun sortTasksWithHeader(tasks: List<Task>): List<ListItem> {
        val helper = TaskListGroupHelper(LocalDateTime.now(clock), locale)
        val groupItemToday = helper.getGroupItem(Group.Today)
        val groupItemBacklog = GroupListItem(
            id = "Backlog",
            index = 0,
            labelResId = R.string.backlog,
            date = LocalDateTime.MAX
        )

        val doneList = mutableListOf<TaskListItem>()
        val dueList = mutableListOf<TaskListItem>()
        val scheduledList = mutableListOf<TaskListItem>()
        val backlogList = mutableListOf<TaskListItem>()

        tasks.forEach { task ->
            val item = TaskListItem(
                id = "${task.id!!}",
                index = 0,
                dateText = helper.format(
                    context,
                    dateTime = task.doneDate ?: task.dueDate,
                    done = task.isDone,
                    isDue = task.isDue
                ),
                task = task
            )
            when {
                task.isDone -> doneList.add(item)
                task.isScheduled -> if (task.isDue) dueList.add(item) else scheduledList.add(item)
                else -> backlogList.add(item)
            }
        }

        val scheduledHeaderList = scheduledList.mapNotNull { it.task.dueDate }.mapNotNull { date ->
            helper.getGroup(date).takeIf { it != Group.Today }?.let { group ->
                helper.getGroupItem(group)
            }
        }.toSet().toList()

        val sortedDoneList = doneList.sortedBy { it.task.doneDate }
        val sortedDueList = dueList.sortedBy { it.task.dueDate }
        val sortedScheduledList = (scheduledHeaderList + scheduledList).sortedBy {
            when (it) {
                is TaskListItem -> it.task.dueDate
                is GroupListItem -> it.date
            }
        }

        return sortedDoneList + groupItemToday + sortedDueList + sortedScheduledList + groupItemBacklog + backlogList
    }

    fun scrollToToday() {
        scrollToToday.value = !scrollToToday.value
    }
}