package io.engst.moodo.ui.tasks

import android.content.Context
import android.text.format.DateUtils
import io.engst.moodo.R
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import java.time.temporal.WeekFields
import java.util.*

enum class Group {
    Today,
    Tomorrow,
    Monday,
    Tuesday,
    Wednesday,
    Thursday,
    Friday,
    Saturday,
    Sunday,
    NextWeek,
    Later
}

class TaskListGroupHelper(
    val now: LocalDateTime,
    locale: Locale
) {
    // relative dates
    val today: LocalDate = now.toLocalDate()
    private val dayBeforeYesterday: LocalDate = today.minusDays(2)
    private val yesterday: LocalDate = today.minusDays(1)
    val tomorrow: LocalDate = today.plusDays(1)
    private val startOfYear: LocalDate = today.with(TemporalAdjusters.firstDayOfYear())
    private val startOfWeek: LocalDate = today.with(WeekFields.of(locale).dayOfWeek(), 1)
    private val endOfLastWeek: LocalDate = startOfWeek.minusDays(1)
    private val endOfWeek: LocalDate = startOfWeek.plusDays(6)
    val startOfNextWeek: LocalDate = startOfWeek.plusDays(7)
    private val endOfNextWeek: LocalDate = startOfNextWeek.plusDays(6)
    val later: LocalDate = startOfNextWeek.plusDays(7)
    private val endOfYear: LocalDate = today.with(TemporalAdjusters.lastDayOfYear())

    // week days
    private val monday: LocalDate = today.with(WeekFields.ISO.dayOfWeek(), 1)
    private val tuesday: LocalDate = today.with(WeekFields.ISO.dayOfWeek(), 2)
    private val wednesday: LocalDate = today.with(WeekFields.ISO.dayOfWeek(), 3)
    private val thursday: LocalDate = today.with(WeekFields.ISO.dayOfWeek(), 4)
    private val friday: LocalDate = today.with(WeekFields.ISO.dayOfWeek(), 5)
    private val saturday: LocalDate = today.with(WeekFields.ISO.dayOfWeek(), 6)
    private val sunday: LocalDate = today.with(WeekFields.ISO.dayOfWeek(), 7)

    fun getGroup(date: LocalDateTime): Group = when (date.toLocalDate()) {
        in LocalDate.MIN..yesterday -> Group.Today
        today -> Group.Today
        tomorrow -> Group.Tomorrow
        in startOfNextWeek..endOfNextWeek -> Group.NextWeek
        in later..LocalDate.MAX -> Group.Later
        monday -> Group.Monday
        tuesday -> Group.Tuesday
        wednesday -> Group.Wednesday
        thursday -> Group.Thursday
        friday -> Group.Friday
        saturday -> Group.Saturday
        sunday -> Group.Sunday
        else -> throw IllegalStateException("whooat")
    }

    fun getGroupItem(group: Group): GroupListItem = GroupListItem(
        id = group.name,
        index = 0,
        labelResId = getGroupLabel(group),
        date = when (group) {
            // fix date if Tomorrow and NextWeek point to the same date
            Group.NextWeek -> {
                val date = getGroupDate(group)
                if (date == tomorrow) {
                    date.plusDays(1)
                } else date
            }
            else -> getGroupDate(group)
        }.atStartOfDay()
    )

    private fun getGroupLabel(group: Group): Int = when (group) {
        Group.Today -> R.string.today
        Group.Tomorrow -> R.string.tomorrow
        Group.Monday -> R.string.monday
        Group.Tuesday -> R.string.tuesday
        Group.Wednesday -> R.string.wednesday
        Group.Thursday -> R.string.thursday
        Group.Friday -> R.string.friday
        Group.Saturday -> R.string.saturday
        Group.Sunday -> R.string.sunday
        Group.NextWeek -> R.string.next_week
        Group.Later -> R.string.later
    }

    private fun getGroupDate(group: Group): LocalDate = when (group) {
        Group.Today -> today
        Group.Tomorrow -> tomorrow
        Group.Monday -> monday
        Group.Tuesday -> tuesday
        Group.Wednesday -> wednesday
        Group.Thursday -> thursday
        Group.Friday -> friday
        Group.Saturday -> saturday
        Group.Sunday -> sunday
        Group.NextWeek -> startOfNextWeek
        Group.Later -> later
    }

    fun format(
        context: Context,
        dateTime: LocalDateTime?,
        done: Boolean = false,
        isDue: Boolean
    ): String =
        if (done) {
            dateTime?.let { doneDate ->
                val zonedDateTime = doneDate.atZone(ZoneId.systemDefault())
                val epochMillis = zonedDateTime.toInstant().toEpochMilli()
                DateUtils.getRelativeTimeSpanString(epochMillis).toString()
            } ?: ""
        } else {
            if (isDue) {
                dateTime?.let { dueDate ->
                    when {
                        ChronoUnit.YEARS.between(dueDate, now) > 0L -> {
                            val count = ChronoUnit.YEARS.between(dueDate, now).toInt()
                            context.resources.getQuantityString(
                                R.plurals.since_x_years,
                                count,
                                count
                            )
                        }
                        ChronoUnit.MONTHS.between(dueDate, now) > 0L -> {
                            val count = ChronoUnit.MONTHS.between(dueDate, now).toInt()
                            context.resources.getQuantityString(
                                R.plurals.since_x_months,
                                count,
                                count
                            )
                        }
                        ChronoUnit.WEEKS.between(dueDate, now) > 0L -> {
                            val count = ChronoUnit.WEEKS.between(dueDate, now).toInt()
                            context.resources.getQuantityString(
                                R.plurals.since_x_weeks,
                                count,
                                count
                            )
                        }
                        ChronoUnit.DAYS.between(dueDate, now) > 0L -> {
                            val count = ChronoUnit.DAYS.between(dueDate, now).toInt()
                            context.resources.getQuantityString(
                                R.plurals.since_x_days,
                                count,
                                count
                            )
                        }
                        ChronoUnit.HOURS.between(dueDate, now) > 0L -> {
                            val count = ChronoUnit.HOURS.between(dueDate, now).toInt()
                            context.resources.getQuantityString(
                                R.plurals.since_x_hours,
                                count,
                                count
                            )
                        }
                        else -> context.getString(R.string.now)
                    }
                } ?: ""
            } else {
                dateTime?.let { scheduledDate ->
                    when (scheduledDate.toLocalDate()) {
                        in startOfYear..endOfLastWeek ->
                            scheduledDate.format(DateTimeFormatter.ofPattern("d. MMM"))
                        in startOfWeek..dayBeforeYesterday ->
                            scheduledDate.format(DateTimeFormatter.ofPattern("EEE"))
                        in yesterday..endOfWeek ->
                            scheduledDate.format(DateTimeFormatter.ofPattern("HH:mm"))
                        in startOfNextWeek..endOfNextWeek ->
                            scheduledDate.format(DateTimeFormatter.ofPattern("EEE"))
                        in later..endOfYear ->
                            scheduledDate.format(DateTimeFormatter.ofPattern("d. MMM"))
                        else ->
                            scheduledDate.format(DateTimeFormatter.ofPattern("d. MMM yyyy"))
                    }
                } ?: ""
            }
        }
}