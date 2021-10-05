package io.engst.moodo.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import io.engst.moodo.model.persistence.TaskDao
import io.engst.moodo.model.persistence.TaskEntity
import io.engst.moodo.model.persistence.asDomainModel
import io.engst.moodo.shared.Logger
import io.engst.moodo.shared.injectLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDateTime

class TaskRepository(private val taskDao: TaskDao) {

    private val logger: Logger by injectLogger("model")

    val tasks: LiveData<List<Task>> = Transformations.map(taskDao.getTasks()) {
        it.asDomainModel()
    }

    suspend fun addTask(task: Task) {
        return withContext(Dispatchers.IO) {
            val entity = TaskEntity.from(task)
            taskDao.addTask(entity)
            logger.debug { "addTask $entity" }
        }
    }

    suspend fun updateTask(task: Task) {
        withContext(Dispatchers.IO) {
            val entity = TaskEntity.from(task)
            taskDao.updateTask(entity)
            logger.debug { "updateTask $entity" }
        }
    }

    suspend fun deleteTask(task: Task) {
        withContext(Dispatchers.IO) {
            val entity = TaskEntity.from(task)
            taskDao.deleteTask(entity)
            logger.debug { "deleteTask $entity" }
        }
    }

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