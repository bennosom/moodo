package io.engst.moodo.model.persistence.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "tag")
data class TagEntity(
    @PrimaryKey(autoGenerate = true) val tag_id: Long,
    val name: String,
    val color: String
)

@Entity(
    tableName = "tag_task",
    primaryKeys = ["ref_tag_id", "ref_task_id"],
    foreignKeys = [
        ForeignKey(
            entity = TagEntity::class,
            parentColumns = ["tag_id"],
            childColumns = ["ref_tag_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = TaskEntity::class,
            parentColumns = ["task_id"],
            childColumns = ["ref_task_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ]
)
data class TagTaskEntity(
    val ref_tag_id: Long,
    val ref_task_id: Long
)