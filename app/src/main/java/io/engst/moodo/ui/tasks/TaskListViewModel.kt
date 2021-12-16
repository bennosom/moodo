package io.engst.moodo.ui.tasks

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.engst.moodo.R
import io.engst.moodo.model.TaskRepository
import io.engst.moodo.model.types.DateShift
import io.engst.moodo.model.types.Task
import io.engst.moodo.shared.Logger
import io.engst.moodo.shared.injectLogger
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*


class TaskListViewModel(
    private val context: Context,
    private val dispatcher: CoroutineDispatcher,
    private val taskRepository: TaskRepository,
    private val clock: Clock,
    private val locale: Locale
) : ViewModel() {

    private val logger: Logger by injectLogger("TaskListViewModel")

    private val _scrollToToday = MutableSharedFlow<Unit>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val scrollToToday: SharedFlow<Unit> = _scrollToToday

    private var cachedTasks: List<Task> = emptyList()
    val tasks: Flow<List<ListItem>> = taskRepository.tasks
        .onEach { cachedTasks = it }
        .map { sortTasksWithHeader(it) }
        .flowOn(dispatcher)

    fun scrollToToday() {
        _scrollToToday.tryEmit(Unit)
    }

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

    fun done(task: Task) {
        viewModelScope.launch(dispatcher) {
            val update = task.copy(doneDate = LocalDateTime.now(clock))
            taskRepository.updateTask(update)
        }
    }

    fun undone(task: Task) {
        viewModelScope.launch(dispatcher) {
            val update = task.copy(
                dueDate = null,
                doneDate = null
            )
            taskRepository.updateTask(update)
        }
    }

    fun delete(task: Task) {
        viewModelScope.launch(dispatcher) {
            taskRepository.deleteTask(task)
        }
    }

    fun undoDelete(task: Task) {
        viewModelScope.launch(dispatcher) {
            taskRepository.addTask(task)
        }
    }

    fun updateOrder(order: List<Long>) {
        viewModelScope.launch(dispatcher) {
            taskRepository.updateOrder(order)
        }
    }

    private fun sortTasksWithHeader(tasks: List<Task>): List<ListItem> {
        val helper = TaskListGroupHelper(LocalDateTime.now(clock), locale)
        var groupItemToday = helper.getGroupItem(Group.Today)
        val groupItemBacklog = ListItem.GroupItem(
            id = "Backlog",
            labelResId = R.string.backlog,
            date = LocalDateTime.MAX
        )

        val doneList = mutableListOf<ListItem.TaskItem>()
        val dueList = mutableListOf<ListItem.TaskItem>()
        val scheduledList = mutableListOf<ListItem.TaskItem>()
        val backlogList = mutableListOf<ListItem.TaskItem>()

        tasks.forEach { task ->
            val item = ListItem.TaskItem(
                id = "${task.id!!}",
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

        if (dueList.isEmpty() && scheduledList.none { it.task.dueDate!!.toLocalDate() == LocalDate.now() }) {
            groupItemToday =
                groupItemToday.copy(message = context.getString(R.string.today_nothing_todo))
        }

        val sortedDoneList = doneList.sortedBy { it.task.doneDate }
        val sortedDueList = dueList.sortedBy { it.task.dueDate }
        val sortedScheduledList = (scheduledHeaderList + scheduledList).sortedBy {
            when (it) {
                is ListItem.TaskItem -> it.task.dueDate
                is ListItem.GroupItem -> it.date
            }
        }

        return sortedDoneList + groupItemToday + sortedDueList + sortedScheduledList + groupItemBacklog + backlogList
    }
}
