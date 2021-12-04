package io.engst.moodo.ui

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

val LocalDate.prettyFormat: String
    get() = format(DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL))

val LocalTime.prettyFormat: String
    get() = format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))

val LocalDateTime.prettyFormat: String
    get() = format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT))