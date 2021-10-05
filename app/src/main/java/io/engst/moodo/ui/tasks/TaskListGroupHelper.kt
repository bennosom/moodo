package io.engst.moodo.ui.tasks

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class TaskListGroupHelper(val today: LocalDate) {

    // specific dates
    private val yesterday: LocalDate = today.minusDays(1)
    val tomorrow: LocalDate = today.plusDays(1)
    val dayAfterTomorrow: LocalDate = tomorrow.plusDays(1)
    val soon: LocalDate = dayAfterTomorrow.plusDays(1)
    private val soonEnd: LocalDate = soon.plusDays(14)
    val later: LocalDate = soonEnd.plusDays(1)

    // date ranges
    val rangePassed: ClosedRange<LocalDateTime> =
        LocalDateTime.of(LocalDate.MIN, LocalTime.MIN)..LocalDateTime.of(yesterday, LocalTime.MAX)
    val rangeToday: ClosedRange<LocalDateTime> =
        LocalDateTime.of(today, LocalTime.MIN)..LocalDateTime.of(today, LocalTime.MAX)
    val rangeTomorrow: ClosedRange<LocalDateTime> =
        LocalDateTime.of(tomorrow, LocalTime.MIN)..LocalDateTime.of(tomorrow, LocalTime.MAX)
    val rangeDayAfterTomorrow: ClosedRange<LocalDateTime> =
        LocalDateTime.of(dayAfterTomorrow, LocalTime.MIN)..LocalDateTime.of(
            dayAfterTomorrow,
            LocalTime.MAX
        )
    val rangeSoon: ClosedRange<LocalDateTime> =
        LocalDateTime.of(soon, LocalTime.MIN)..LocalDateTime.of(soonEnd, LocalTime.MAX)
    val rangeLater: ClosedRange<LocalDateTime> =
        LocalDateTime.of(later, LocalTime.MIN)..LocalDateTime.of(LocalDate.MAX, LocalTime.MAX)

    fun getDateGroupFor(dateTime: LocalDateTime?): LocalDate =
        dateTime?.let {
            when (it) {
                in rangeTomorrow -> tomorrow
                in rangeDayAfterTomorrow -> dayAfterTomorrow
                in rangeSoon -> soon
                in rangeLater -> later
                else -> today
            }
        } ?: today
}