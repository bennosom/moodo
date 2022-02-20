package io.engst.moodo.model.persistence

import androidx.room.*
import io.engst.moodo.model.persistence.entity.TagEntity
import io.engst.moodo.model.persistence.entity.TaskEntity
import io.engst.moodo.model.persistence.entity.TaskListOrderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    // tasks

    @Query("SELECT * FROM task JOIN tag ON task.id = tag.taskId")
    fun tasksWithTags(): Map<TaskEntity, List<TagEntity>>

    @Query("SELECT * FROM task ORDER BY date(dueDate) ASC")
    fun tasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM task WHERE id == :id")
    fun getTaskById(id: Long): TaskEntity?

    @Insert
    suspend fun addTask(vararg task: TaskEntity): List<Long>

    @Update
    suspend fun updateTask(vararg task: TaskEntity)

    @Delete
    suspend fun deleteTask(vararg task: TaskEntity)


    // task order

    @Query("SELECT * FROM task_list_order WHERE list_id == 0")
    fun taskOrder(): Flow<TaskListOrderEntity?>

    @Update
    suspend fun updateOrder(listOrderEntity: TaskListOrderEntity)


    // tags

    @Query("SELECT * FROM tag")
    fun tags(): Flow<List<TagEntity>>

    @Query("SELECT * FROM tag WHERE id == :id")
    fun getTag(id: Long): TagEntity?

    @Insert
    suspend fun addTag(vararg tag: TagEntity): List<Long>

    @Update
    suspend fun updateTag(vararg tag: TagEntity)

    @Delete
    suspend fun deleteTag(vararg tag: TagEntity)
}