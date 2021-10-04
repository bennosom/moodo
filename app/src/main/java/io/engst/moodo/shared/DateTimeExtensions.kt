package io.engst.moodo.shared

import io.engst.moodo.R
import io.engst.moodo.ui.tasks.DateGroup
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

val LocalDate.prettyFormat: String
    get() = format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))

val LocalTime.prettyFormat: String
    get() = format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))

val dateGroup = DateGroup()

val LocalDateTime.prettyFormat: String
    get() {
        val pattern = when (this.toLocalDate()) {
            in dateGroup.today, in dateGroup.tomorrow, in dateGroup.dayAfterTomorrow ->
                DateTimeFormatter.ofPattern("HH:mm")
            in dateGroup.soon, in dateGroup.later ->
                DateTimeFormatter.ofPattern("dd.MM.yyyy")
            else ->
                DateTimeFormatter.ofPattern("HH:mm dd.MM.yyyy")
        }
        return this.format(pattern)
    }

val LocalDateTime.prettyFormatRelative: Int
    get() {
        val group = DateGroup()
        return when (this.toLocalDate()) {
            in group.passed, in group.today -> R.string.today
            in group.tomorrow -> R.string.tomorrow
            in group.dayAfterTomorrow -> R.string.day_after_tomorrow
            in group.soon -> R.string.soon
            else -> R.string.later
        }
    }