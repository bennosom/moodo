package io.engst.moodo.model.service

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import io.engst.moodo.model.api.Task
import io.engst.moodo.model.service.persistence.TaskDao
import io.engst.moodo.model.service.persistence.TaskEntity
import io.engst.moodo.model.service.persistence.asDomainModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDateTime

class TaskRepository(private val taskDao: TaskDao) {

    val tasks: LiveData<List<Task>> = Transformations.map(taskDao.getTasks()) {
        it.asDomainModel()
    }

    suspend fun addTask(task: Task) {
        return withContext(Dispatchers.IO) {
            taskDao.addTask(TaskEntity.from(task))
        }
    }

    suspend fun updateTask(task: Task) {
        withContext(Dispatchers.IO) {
            taskDao.updateTask(TaskEntity.from(task))
        }
    }

    suspend fun deleteTask(task: Task) {
        withContext(Dispatchers.IO) {
            taskDao.deleteTask(TaskEntity.from(task))
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