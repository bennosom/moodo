package io.engst.moodo.model.persistence.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "task_list_order")
data class TaskListOrderEntity(
    @PrimaryKey val list_id: Long,
    val list_order: String
)