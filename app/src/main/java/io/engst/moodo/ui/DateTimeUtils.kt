package io.engst.moodo.shared

import io.engst.moodo.R
import io.engst.moodo.ui.tasks.TaskListGroupHelper
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

val LocalDate.prettyFormat: String
    get() = format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))

val LocalTime.prettyFormat: String
    get() = format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))

val LocalDateTime.prettyFormat: String
    get() {
        val dateGroupHelper = TaskListGroupHelper(LocalDate.now())
        val pattern = when (this) {
            in dateGroupHelper.rangeToday, in dateGroupHelper.rangeTomorrow, in dateGroupHelper.rangeDayAfterTomorrow ->
                DateTimeFormatter.ofPattern("HH:mm")
            in dateGroupHelper.rangeSoon, in dateGroupHelper.rangeLater ->
                DateTimeFormatter.ofPattern("dd.MM.yyyy")
            else ->
                DateTimeFormatter.ofPattern("HH:mm dd.MM.yyyy")
        }
        return this.format(pattern)
    }

val LocalDateTime.prettyFormatRelative: Int
    get() {
        val dateGroupHelper = TaskListGroupHelper(LocalDate.now())
        return when (this) {
            in dateGroupHelper.rangePassed, in dateGroupHelper.rangeToday -> R.string.today
            in dateGroupHelper.rangeTomorrow -> R.string.tomorrow
            in dateGroupHelper.rangeDayAfterTomorrow -> R.string.day_after_tomorrow
            in dateGroupHelper.rangeSoon -> R.string.soon
            else -> R.string.later
        }
    }