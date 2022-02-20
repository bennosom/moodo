package io.engst.moodo.model

import io.engst.moodo.model.persistence.TagEntity
import io.engst.moodo.model.types.Tag

class TagFactory {

    private fun createTag(entity: TagEntity): Tag = Tag(
        id = entity.id!!,
        name = entity.name,
        color = entity.color
    )

    fun createTagList(list: List<TagEntity>): List<Tag> = list.map { createTag(it) }
}