package io.engst.moodo.model.types

import androidx.annotation.ColorInt

data class Tag(
    val id: Long? = null,
    val name: String,
    @ColorInt val color: Int
)