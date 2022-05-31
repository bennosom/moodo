package io.engst.moodo.model.persistence

import androidx.room.*
import io.engst.moodo.model.persistence.entity.TagEntity
import io.engst.moodo.model.persistence.entity.TagTaskEntity
import io.engst.moodo.model.persistence.entity.TaskEntity
import io.engst.moodo.model.persistence.entity.TaskListOrderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    // tasks

    @Query(
        """
        SELECT * FROM task
        LEFT JOIN tag_task on tag_task.ref_task_id == task.task_id
        LEFT JOIN tag on tag_task.ref_tag_id == tag.tag_id
        ORDER BY date(dueDate) ASC
    """
    )
    fun tasks(): Flow<Map<TaskEntity, List<TagEntity>>>

    @Query(
        """
        SELECT * FROM task
        LEFT JOIN tag_task on tag_task.ref_task_id == task.task_id
        LEFT JOIN tag on tag_task.ref_tag_id == tag.tag_id
        WHERE task_id == :id
        """
    )
    fun getTask(id: Long): Map<TaskEntity, List<TagEntity>>

    @Insert
    suspend fun addTask(vararg entity: TaskEntity): List<Long>

    @Transaction
    suspend fun addTaskAndTags(taskEntity: TaskEntity, existingTagEntities: Array<TagEntity>, newTagEntities: Array<TagEntity>) {
        // update existing tags
        updateTag(*existingTagEntities)

        // add new tags
        val newTagIds = addTag(*newTagEntities)

        val taskId = addTask(taskEntity).first()

        // add refs
        val tagTaskEntities =
            (existingTagEntities.map { it.tag_id } + newTagIds).map { TagTaskEntity(it, taskId) }.toTypedArray()
        addTagTask(*tagTaskEntities)
    }

    @Update
    suspend fun updateTask(vararg entity: TaskEntity)

    @Transaction
    suspend fun updateTaskAndTags(taskEntity: TaskEntity, existingTagEntities: Array<TagEntity>, newTagEntities: Array<TagEntity>) {
        // remove refs
        val existingTagTaskEntities = getTagTasks(taskEntity.task_id).toTypedArray()
        deleteTagTask(*existingTagTaskEntities)

        // update existing tags
        updateTag(*existingTagEntities)

        // add new tags
        val newTagIds = addTag(*newTagEntities)

        // add refs
        val tagTaskEntities =
            (existingTagEntities.map { it.tag_id } + newTagIds).map { TagTaskEntity(it, taskEntity.task_id) }.toTypedArray()
        addTagTask(*tagTaskEntities)

        updateTask(taskEntity)
    }

    @Delete
    suspend fun deleteTask(vararg entity: TaskEntity)


    // task order

    @Query("SELECT * FROM task_list_order WHERE list_id == 0")
    fun taskOrder(): Flow<TaskListOrderEntity?>

    @Update
    suspend fun updateOrder(entity: TaskListOrderEntity)


    // tags

    @Query("SELECT * FROM tag")
    fun tags(): Flow<List<TagEntity>>

    @Query("SELECT * FROM tag WHERE tag_id == :id")
    fun getTag(id: Long): TagEntity?

    @Insert
    suspend fun addTag(vararg entity: TagEntity): List<Long>

    @Update
    suspend fun updateTag(vararg entity: TagEntity)

    @Delete
    suspend fun deleteTag(vararg entity: TagEntity)


    // tag/task references

    @Query("SELECT * FROM tag_task WHERE ref_task_id == :taskId")
    suspend fun getTagTasks(taskId: Long): List<TagTaskEntity>

    @Insert
    suspend fun addTagTask(vararg entity: TagTaskEntity): List<Long>

    @Update
    suspend fun updateTagTask(vararg entity: TagTaskEntity)

    @Delete
    suspend fun deleteTagTask(vararg entity: TagTaskEntity)
}