package io.engst.moodo.model

import io.engst.moodo.model.persistence.TaskDao
import io.engst.moodo.model.persistence.entity.TaskListOrderEntity
import io.engst.moodo.model.types.DateShift
import io.engst.moodo.model.types.Tag
import io.engst.moodo.model.types.Task
import io.engst.moodo.model.types.TaskAction
import io.engst.moodo.shared.Logger
import io.engst.moodo.shared.injectLogger
import io.engst.moodo.ui.tasks.TaskListGroupHelper
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

class TaskRepository(
    private val taskDao: TaskDao,
    private val taskFactory: TaskFactory,
    private val tagFactory: TagFactory,
    private val clock: Clock,
    private val locale: Locale
) {
    private val logger: Logger by injectLogger(tag = "repository", prefix = "TaskRepository")

    private val forceTaskUpdate = MutableSharedFlow<Unit>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    private val taskOrder: Flow<List<Long>> = taskDao.taskOrder()
        .flowOn(Dispatchers.IO)
        .map { taskListOrderEntity ->
            taskListOrderEntity?.let {
                Json.decodeFromString<List<Long>>(it.list_order)
            } ?: emptyList()
        }
        .onEach { logger.debug { "taskOrder=$it" } }

    val tags: Flow<List<Tag>> = taskDao.tags()
        .flowOn(Dispatchers.IO)
        .map { tagEntities ->
            tagEntities.map { tagFactory.createTag(it) }
        }
        .onEach { logger.debug { "tags=$it" } }

    val tasks: Flow<List<Task>> = taskDao.tasks()
        .flowOn(Dispatchers.IO)
        .combine(forceTaskUpdate) { tasks, _ -> tasks }
        .map { taskWithTags ->
            taskWithTags.map { entry ->
                val tags = entry.value.map { tagFactory.createTag(it) }
                taskFactory.createTask(entry.key, tags)
            }
        }
        .combine(taskOrder) { tasks, order ->
            val orderById = order.withIndex().associate { it.value to it.index }
            tasks.sortedBy { orderById[it.id] }
        }
        .onEach { logger.debug { "tasks=$it" } }
        .shareIn(GlobalScope, SharingStarted.Eagerly, 1)

    fun getTask(id: Long): Task? {
        return runBlocking(Dispatchers.IO) {
            val result = taskDao.getTask(id)
            val (taskEntity, tagEntities) = result.entries.first().toPair()
            val tags = tagEntities.map { tagFactory.createTag(it) }
            val task = taskEntity.let { taskFactory.createTask(it, tags) }
            task
        }
    }

    suspend fun addTask(task: Task) {
        withContext(Dispatchers.IO) {
            logger.debug { "addTask task=$task" }

            val taskEntity = task.toEntity()

            // update existing tags
            val existingTags = task.tags.filter { it.id != null }
            val existingTagEntities = existingTags.map { it.toEntity() }.toTypedArray()

            // add new tags
            val newTags = task.tags.filter { it.id == null }
            val newTagEntities = newTags.map { it.toEntity() }.toTypedArray()

            taskDao.addTaskAndTags(taskEntity, existingTagEntities, newTagEntities)
        }
    }

    suspend fun updateTask(task: Task) {
        withContext(Dispatchers.IO) {
            logger.debug { "updateTask task=$task" }

            val taskEntity = task.toEntity()

            // update existing tags
            val existingTags = task.tags.filter { it.id != null }
            val existingTagEntities = existingTags.map { it.toEntity() }.toTypedArray()

            // add new tags
            val newTags = task.tags.filter { it.id == null }
            val newTagEntities = newTags.map { it.toEntity() }.toTypedArray()

            taskDao.updateTaskAndTags(taskEntity, existingTagEntities, newTagEntities)
        }
    }

    suspend fun updateOrder(order: List<Long>) {
        withContext(Dispatchers.IO) {
            val orderAsString = Json.encodeToString(order)
            val entity = TaskListOrderEntity(0L, orderAsString)
            logger.debug { "updateOrder order=$order" }
            taskDao.updateOrder(entity)
        }
    }

    suspend fun deleteTask(task: Task) {
        withContext(Dispatchers.IO) {
            logger.debug { "deleteTask task=$task" }
            taskDao.deleteTask(task.toEntity())
        }
    }

    fun shiftTo(taskId: Long, shiftTo: TaskAction) {
        GlobalScope.launch(Dispatchers.IO) {
            logger.debug { "shiftTo taskId=$taskId shiftTo=$shiftTo" }

            val helper = TaskListGroupHelper(LocalDateTime.now(clock), locale)
            val updatedDueDate = when (shiftTo) {
                TaskAction.ShiftToTomorrow -> helper.tomorrow.atTime(9, 0)
                TaskAction.ShiftToNextWeek -> helper.startOfNextWeek.atTime(9, 0)
                else -> throw IllegalStateException("failed to shift task to $shiftTo")
            }

            taskDao.getTask(taskId).let {
                assert(it.size <= 1)
                val entry = it.entries.first()
                val task = entry.key
                taskDao.updateTask(
                    task.copy(
                        dueDate = updatedDueDate,
                        doneDate = null
                    )
                )
            }
        }
    }

    fun shiftBy(taskId: Long, shiftBy: DateShift) {
        GlobalScope.launch(Dispatchers.IO) {
            logger.debug { "shiftBy taskId=$taskId shiftBy=$shiftBy" }

            taskDao.getTask(taskId).let {
                assert(it.size <= 1)
                val entry = it.entries.first()
                val task = entry.key
                val baseDate = when {
                    task.doneDate != null -> LocalDateTime.of(
                        LocalDate.now(clock),
                        task.dueDate!!.toLocalTime()
                    )
                    task.dueDate != null -> task.dueDate
                    else -> LocalDateTime.now(clock)
                }
                val updatedDueDate = when (shiftBy) {
                    DateShift.None -> baseDate
                    DateShift.OneDay -> baseDate.plusDays(1)
                    DateShift.TwoDays -> baseDate.plusDays(2)
                    DateShift.OneWeek -> baseDate.plusWeeks(1)
                }

                taskDao.updateTask(
                    task.copy(
                        dueDate = updatedDueDate,
                        doneDate = null
                    )
                )
            }
        }
    }

    fun setDone(taskId: Long) {
        GlobalScope.launch(Dispatchers.IO) {
            logger.debug { "setDone taskId=$taskId" }
            taskDao.getTask(taskId).let {
                assert(it.size <= 1)
                val entry = it.entries.first()
                val task = entry.key
                taskDao.updateTask(task.copy(doneDate = LocalDateTime.now()))
            }
        }
    }

    fun forceUpdate() {
        forceTaskUpdate.tryEmit(Unit)
    }

    /**
     * TODO: Replace with Database Pre-Initialization step of Database-Builder
     */
    suspend fun insertDummyData() {
        withContext(Dispatchers.IO) {
            val now = LocalDateTime.now()
            val tasks = listOf(
                Task(
                    description = "Willkommen bei Moodo! Organisiere Deine TODO's mit Moodoo ganz einfach und intuitiv. Du behälst stets den Überblick und verpasst keine wichtigen Dinge mehr. Alle TODO's werden chronologisch sortiert nach ihrem Fälligkeitsdatum!",
                    createdDate = now,
                    isDue = false,
                    priority = 0
                ),
                Task(
                    description = "Wenn Du ein TODO erledigt hast, wische ihn nach links!",
                    createdDate = now,
                    isDue = false,
                    priority = 0
                ),
                Task(
                    description = "Hattest du keine Zeit ein TODO zu erledigen, wische ihn nach rechts und sortiere ihn zu einem späteren Zeitpunkt neu ein.",
                    createdDate = now,
                    isDue = false,
                    priority = 0
                ),
                Task(
                    description = "Ist ein TODO erledigt, steht er ganz oben in der Liste - in der Vergangenheit sozusagen. Sollte sich Dein TODO widerholen, wische ihn einfach wieder nach rechts - in die Zukunft ;)",
                    createdDate = now,
                    isDue = false,
                    priority = 0
                ),
                Task(
                    description = "Löschen kannst du einen TODO, nachdem er erledigt ist und Du ihn nach links wischt.",
                    createdDate = now,
                    isDue = false,
                    priority = 0
                )
            ).map { it.toEntity() }.toTypedArray()
            taskDao.addTask(*tasks)
        }
    }
}