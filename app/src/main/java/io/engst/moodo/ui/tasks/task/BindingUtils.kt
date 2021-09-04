package io.engst.moodo.ui.tasks.task

import android.os.Build
import androidx.annotation.RequiresApi
import io.engst.moodo.ui.tasks.DateGroup
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
fun convertDateTimeFormatted(dateTime: LocalDateTime): String {
    val group = DateGroup()
    val pattern = when (dateTime.toLocalDate()) {
        in group.today, in group.tomorrow, in group.dayAfterTomorrow -> DateTimeFormatter.ofPattern(
            "HH:mm"
        )
        in group.soon, in group.later -> DateTimeFormatter.ofPattern("dd.MM.yyyy")
        else -> DateTimeFormatter.ofPattern("HH:mm dd.MM.yyyy")
    }
    return dateTime.format(pattern)
}

@RequiresApi(Build.VERSION_CODES.O)
fun convertDateRelativeFormatted(date: LocalDateTime): String {
    val group = DateGroup()
    return when (date.toLocalDate()) {
        in group.passed, in group.today -> "Heute"
        in group.tomorrow -> "Morgen"
        in group.dayAfterTomorrow -> "Übermorgen"
        in group.soon -> "Bald"
        else -> "Irgendwann"
    }
}