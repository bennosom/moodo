package io.engst.moodo.model

import io.engst.moodo.model.persistence.TaskDao
import io.engst.moodo.model.persistence.TaskEntity
import io.engst.moodo.model.types.Task
import io.engst.moodo.model.types.TaskAction
import io.engst.moodo.shared.Logger
import io.engst.moodo.shared.injectLogger
import io.engst.moodo.ui.tasks.TaskListGroupHelper
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.*
import java.time.Clock
import java.time.LocalDateTime
import java.util.*

class TaskRepository(
    private val taskDao: TaskDao,
    private val taskFactory: TaskFactory,
    private val clock: Clock,
    private val locale: Locale
) {
    private val logger: Logger by injectLogger("model")

    private val forceUpdateChannel = ConflatedBroadcastChannel<Unit>()

    val tasks: Flow<List<Task>> = taskDao.getTasks()
        .combine(forceUpdateChannel.asFlow()) { tasks, _ -> tasks }
        .map { taskFactory.createTaskList(it) }
        .flowOn(Dispatchers.IO)

    fun getTask(id: Long): Task {
        return runBlocking(Dispatchers.IO) {
            val entity = taskDao.getTaskById(id)
            return@runBlocking taskFactory.createTask(entity)
        }
    }

    suspend fun addTask(task: Task) {
        withContext(Dispatchers.IO) {
            logger.debug { "add $task" }
            taskDao.addTask(TaskEntity.from(task))
        }
    }

    suspend fun updateTask(task: Task) {
        withContext(Dispatchers.IO) {
            logger.debug { "update $task" }
            taskDao.updateTask(TaskEntity.from(task))
        }
    }

    suspend fun deleteTask(task: Task) {
        withContext(Dispatchers.IO) {
            logger.debug { "delete $task" }
            taskDao.deleteTask(TaskEntity.from(task))
        }
    }

    fun shift(taskId: Long, amount: TaskAction) {
        GlobalScope.launch(Dispatchers.IO) {
            logger.debug { "shift #$taskId by $amount" }

            val helper = TaskListGroupHelper(LocalDateTime.now(clock), locale)
            val shiftOffset = when (amount) {
                TaskAction.ShiftOneDay -> helper.tomorrow.atTime(9, 0)
                TaskAction.ShiftOneWeek -> helper.startOfNextWeek.atTime(9, 0)
                else -> throw IllegalStateException("failed to shift task by $amount")
            }

            val task = taskDao.getTaskById(taskId)
            taskDao.updateTask(task.copy(dueDate = shiftOffset))
        }
    }

    fun setDone(taskId: Long) {
        GlobalScope.launch(Dispatchers.IO) {
            logger.debug { "setDone #$taskId" }
            val task = taskDao.getTaskById(taskId)
            taskDao.updateTask(task.copy(doneDate = LocalDateTime.now()))
        }
    }


    fun forceUpdateList() {
        forceUpdateChannel.offer(Unit)
    }

    /**
     * TODO: Replace with Database Pre-Initialization step of Database-Builder
     */
    suspend fun insertDummyData() {
        withContext(Dispatchers.IO) {
            val now = LocalDateTime.now()
            val tasks = arrayOf(
                TaskEntity(
                    null,
                    "Willkommen bei Moodo! Organisiere Deine TODO's mit Moodoo ganz einfach und intuitiv. Du behälst stets den Überblick und verpasst keine wichtigen Dinge mehr. Alle TODO's werden chronologisch sortiert nach ihrem Fälligkeitsdatum!",
                    now.minusMinutes(1),
                    now.minusMinutes(1),
                    null,
                    0,
                    0
                ),
                TaskEntity(
                    null,
                    "Wenn Du ein TODO erledigt hast, wische ihn nach links!",
                    now,
                    now,
                    null,
                    0,
                    0
                ),
                TaskEntity(
                    null,
                    "Hattest du keine Zeit ein TODO zu erledigen, wische ihn nach rechts und sortiere ihn zu einem späteren Zeitpunkt neu ein.",
                    now.plusMinutes(1),
                    now.plusMinutes(1),
                    null,
                    0,
                    0
                ),
                TaskEntity(
                    null,
                    "Ist ein TODO erledigt, steht er ganz oben in der Liste - in der Vergangenheit sozusagen. Sollte sich Dein TODO widerholen, wische ihn einfach wieder nach rechts - in die Zukunft ;)",
                    now.plusMinutes(2),
                    now.plusMinutes(2),
                    null,
                    0,
                    0
                ),
                TaskEntity(
                    null,
                    "Löschen kannst du einen TODO, nachdem er erledigt ist und Du ihn nach links wischt.",
                    now.plusMinutes(3),
                    now.plusMinutes(3),
                    null,
                    0,
                    0
                )
            )
            taskDao.addTasks(*tasks)
        }
    }
}