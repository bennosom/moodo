package io.engst.moodo.ui.tasks

import java.time.LocalDate
import java.time.LocalDateTime

class DateGroup {
    val now: LocalDate = LocalDate.now()
    val today = now..now
    val passed = LocalDate.MIN..now.minusDays(1)
    val tomorrow = now.plusDays(1)..now.plusDays(1)
    val dayAfterTomorrow = now.plusDays(2)..now.plusDays(2)
    val soon = dayAfterTomorrow.endInclusive.plusDays(1)..dayAfterTomorrow.endInclusive.plusDays(14)
    val later = soon.endInclusive.plusDays(1)..LocalDate.MAX

    fun getDateGroupFor(dateTime: LocalDateTime?): LocalDate =
        dateTime?.let {
            when (dateTime.toLocalDate()) {
                in passed, in today -> passed.start
                in tomorrow -> tomorrow.start
                in dayAfterTomorrow -> dayAfterTomorrow.start
                in soon -> soon.start
                in later -> later.start
                else -> now
            }
        } ?: now
}