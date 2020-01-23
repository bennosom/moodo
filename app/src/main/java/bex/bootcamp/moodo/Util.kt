package bex.bootcamp.moodo

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

fun convertDateTimeFormatted(dateTime: LocalDateTime) = dateTime.format(
    DateTimeFormatter.ofPattern("HH:mm dd.MM.yyyy")
)

fun convertDateRelativeFormatted(date: LocalDate): String {
    var group = DateGroup(LocalDate.now())
    return when (date) {
        group.today -> "Heute"
        group.tomorrow -> "Morgen"
        group.dayAfterTomorrow -> "Übermorgen"
        group.soon -> "Bald"
        group.later -> "Irgendwann"
        else -> "Ooops"
    }
}

class DateGroup(val today: LocalDate) {
    var passed: LocalDate
    var yesterday: LocalDate
    var tomorrow: LocalDate
    var dayAfterTomorrow: LocalDate
    var soon: LocalDate
    var later: LocalDate

    init {
        passed = LocalDate.MIN
        try {
            yesterday = today.minusDays(1)
            tomorrow = today.plusDays(1)
            dayAfterTomorrow = today.plusDays(2)
            soon = today.plusDays(3)
            later = today.plusWeeks(2)
        } catch (e: Exception) {
            yesterday = today
            tomorrow = today
            dayAfterTomorrow = today
            soon = today
            later = today
        }
    }

    val asList: List<LocalDate>
        get() = listOf(
            today,
            tomorrow,
            dayAfterTomorrow,
            soon,
            later
        )

    fun getDateFor(date: LocalDate): LocalDate? {
        return when (date) {
            today -> today
            tomorrow -> tomorrow
            dayAfterTomorrow -> dayAfterTomorrow
            in soon..later.minusDays(1) -> soon
            in later..LocalDate.MAX -> later
            else -> null
        }
    }
}