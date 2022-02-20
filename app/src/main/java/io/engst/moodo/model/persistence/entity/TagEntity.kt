package io.engst.moodo.model.persistence.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "tag"
)
data class TagEntity(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val name: String,
    val color: Int
)

