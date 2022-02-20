package io.engst.moodo.model.persistence.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "task_list_order",
/*    foreignKeys = [
        ForeignKey(entity = TaskListEntity::class,
            parentColumns = ["id"],
            childColumns = ["list_id"],
            onDelete = ForeignKey.CASCADE)
    ]*/
)
data class TaskListOrderEntity(
    @PrimaryKey val list_id: Long,
    val list_order: String
)
/*

@Entity(
    tableName = "task_list"
)
data class TaskListEntity(
    @PrimaryKey(autoGenerate = true) val id: Long,
    val list_name: String
)*/
