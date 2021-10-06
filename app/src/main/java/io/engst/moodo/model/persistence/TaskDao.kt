package io.engst.moodo.model.persistence

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM task")
    fun getTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM task WHERE id == :id")
    fun getTaskById(id: Long): TaskEntity

    @Insert
    suspend fun addTask(task: TaskEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addTasks(vararg task: TaskEntity): List<Long>

    @Update
    suspend fun updateTask(vararg task: TaskEntity)

    @Delete
    suspend fun deleteTask(vararg task: TaskEntity)
}
