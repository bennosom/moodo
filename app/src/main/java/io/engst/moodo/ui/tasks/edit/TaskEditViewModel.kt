package io.engst.moodo.ui.tasks.edit

import android.os.VibrationEffect
import android.os.Vibrator
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import io.engst.moodo.model.TaskRepository
import io.engst.moodo.model.types.Tag
import io.engst.moodo.model.types.Task
import io.engst.moodo.shared.Logger
import io.engst.moodo.shared.injectLogger
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*

class TaskEditViewModel(
    private val taskRepository: TaskRepository,
    private val vibrator: Vibrator,
    val clock: Clock,
    val locale: Locale
) : ViewModel() {

    private val logger: Logger by injectLogger("viewmodel")

    private val tags: MutableLiveData<List<Tag>> = MutableLiveData(emptyList())

    val tagsAvailable: LiveData<List<Tag>> = taskRepository.tags.asLiveData()
    val tagsUsed: LiveData<List<Tag>> = tags

    var originalTask: Task? = null
        set(value) {
            field = value

            description = field?.description ?: ""
            dueDate = field?.dueDate?.toLocalDate()
            dueTime = field?.dueDate?.toLocalTime()

            logger.info { "edit task: $field" }
        }

    var description: String = ""
    var dueDate: LocalDate? = null
    var dueTime: LocalTime? = null

    fun init(id: Long) {
        if (id > -1L) {
            originalTask = taskRepository.getTask(id)
            tags.value = originalTask?.tags ?: emptyList()
        }
    }

    fun addTask() {
        if (description.isBlank()) {
            logger.error { "emtpy description, do nothing" }
            return
        }
        val newTask = Task(
            description = description,
            createdDate = LocalDateTime.now(),
            dueDate = buildDueDate(),
            isDue = false,
            priority = 0
        )
        vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
        GlobalScope.launch { taskRepository.addTask(newTask) }
    }


    fun deleteTask() {
        logger.info { "deleteTask" }

        originalTask?.let {
            GlobalScope.launch {
                taskRepository.deleteTask(it)
            }
        } ?: Unit.let {
            logger.warn { "task not existing" }
        }
    }

    fun setTaskDone() {
        originalTask?.let { original ->
            val updatedTask = original.copy(
                doneDate = LocalDateTime.now()
            )
            GlobalScope.launch { taskRepository.updateTask(updatedTask) }
        } ?: Unit.let {
            logger.error { "failed to set task to done: no task set" }
        }
    }

    fun saveTask() {
        logger.info { "saveChanges" }

        val dueDateTime = buildDueDate()

        originalTask?.let { original ->
            if (description.isBlank()) {
                logger.info { "delete task, because description has been removed" }

                deleteTask()
                return@saveTask
            }

            val updatedTask = original.copy(
                description = description,
                dueDate = dueDateTime,
                tags = tags.value ?: emptyList()
            )

            // fix done date if due date has changed
            if (dueDateTime != original.dueDate) {
                updatedTask.doneDate = null
            }

            GlobalScope.launch { taskRepository.updateTask(updatedTask) }
        } ?: Unit.let {
            val newTask = Task(
                description = description,
                createdDate = LocalDateTime.now(),
                dueDate = dueDateTime,
                isDue = false,
                priority = 0,
                tags = tags.value ?: emptyList()
            )

            GlobalScope.launch { taskRepository.addTask(newTask) }
        }
    }

    fun setTags(tags: List<Tag>) {
        this.tags.value = tags
    }

    fun clear() {
        description = ""
        dueDate = null
        dueTime = null
    }

    private fun buildDueDate(): LocalDateTime? =
        dueDate?.let {
            LocalDateTime.of(dueDate, dueTime ?: LocalTime.of(9, 0))
        }
}