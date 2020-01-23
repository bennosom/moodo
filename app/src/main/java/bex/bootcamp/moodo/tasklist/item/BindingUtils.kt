package bex.bootcamp.moodo.tasklist.item

import android.widget.TextView
import androidx.databinding.BindingAdapter
import bex.bootcamp.moodo.convertDateRelativeFormatted
import bex.bootcamp.moodo.convertDateTimeFormatted
import java.time.LocalDate
import java.time.LocalDateTime

@BindingAdapter("dateTimeFormatted")
fun TextView.setDateTimeFormatted(dateTime: LocalDateTime) {
    text = convertDateTimeFormatted(dateTime)
}

@BindingAdapter("dateRelativeFormatted")
fun TextView.setDateRelativeFormatted(date: LocalDate) {
    text = convertDateRelativeFormatted(date)
}