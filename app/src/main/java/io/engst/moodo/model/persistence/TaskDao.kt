package io.engst.moodo.model.persistence

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM task ORDER BY date(dueDate) ASC")
    fun getTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM task WHERE id == :id")
    fun getTaskById(id: Long): TaskEntity

    @Insert
    suspend fun addTask(vararg task: TaskEntity): List<Long>

    @Update
    suspend fun updateTask(vararg task: TaskEntity)

    @Delete
    suspend fun deleteTask(vararg task: TaskEntity)

    @Query("SELECT * FROM task_list_order WHERE list_id == 0")
    fun getTaskOrder(): Flow<TaskListOrderEntity>

    @Update
    suspend fun updateOrder(listOrderEntity: TaskListOrderEntity)
}
