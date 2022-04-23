package io.engst.moodo.model.persistence

import androidx.room.*
import io.engst.moodo.model.persistence.entity.TagEntity
import io.engst.moodo.model.persistence.entity.TaskEntity
import io.engst.moodo.model.persistence.entity.TaskListOrderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    // tasks

    @Query(
        """
        SELECT * FROM task
        LEFT JOIN ref_tag_task on ref_tag_task.ref_task_id == task.task_id
        LEFT JOIN tag on ref_tag_task.ref_tag_id == tag.tag_id
        ORDER BY date(dueDate) ASC
    """
    )
    fun tasks(): Flow<Map<TaskEntity, List<TagEntity>>>

    @Query(
        """
        SELECT * FROM task
        LEFT JOIN ref_tag_task on ref_tag_task.ref_task_id == task.task_id
        LEFT JOIN tag on ref_tag_task.ref_tag_id == tag.tag_id
        WHERE task_id == :id
        """
    )
    fun getTask(id: Long): TaskEntity?

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

    @Query("SELECT * FROM tag WHERE tag_id == :id")
    fun getTag(id: Long): TagEntity?

    @Insert
    suspend fun addTag(vararg tag: TagEntity): List<Long>

    @Update
    suspend fun updateTag(vararg tag: TagEntity)

    @Delete
    suspend fun deleteTag(vararg tag: TagEntity)
}