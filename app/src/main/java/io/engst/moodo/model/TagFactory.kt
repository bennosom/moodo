package io.engst.moodo.model

import android.graphics.Color
import io.engst.moodo.model.persistence.entity.TagEntity
import io.engst.moodo.model.types.Tag

class TagFactory {
    fun createTag(entity: TagEntity): Tag = Tag(
        id = entity.tag_id,
        name = entity.name,
        color = toColor(entity.color) ?: Color.TRANSPARENT
    )
}

fun Tag.toEntity() = TagEntity(
    tag_id = id ?: 0,
    name = name,
    color = fromColor(color) ?: "#ff000000"
)

fun toColor(colorString: String?): Int? =
    colorString?.let { Color.parseColor(it) }

fun fromColor(color: Int?): String? = color?.let {
    String.format("#%06X", 0xFFFFFF and Color.valueOf(it).toArgb())
}