package bex.bootcamp.moodo.domain

import java.time.LocalDateTime

data class Task(
    val title: String = "",
    val created: LocalDateTime? = null,
    val recycleCount: Int = 0,
    val due: LocalDateTime? = null,
    val done: LocalDateTime? = null
)