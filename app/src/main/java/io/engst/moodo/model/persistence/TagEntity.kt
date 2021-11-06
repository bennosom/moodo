package io.engst.moodo.model.persistence

import androidx.annotation.ColorInt
import androidx.room.Entity
import androidx.room.PrimaryKey
import io.engst.moodo.model.types.Tag

@Entity(tableName = "tag")
data class TagEntity constructor(
    @PrimaryKey(autoGenerate = true) val id: Long?,
    val name: String,
    @ColorInt val color: Int
) {
    companion object {
        fun from(tag: Tag) = TagEntity(
            id = tag.id,
            name = tag.name,
            color = tag.color
        )
    }
}