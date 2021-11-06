package io.engst.moodo.model.persistence

import android.graphics.Color
import androidx.room.TypeConverter
import java.time.LocalDateTime

class TaskConverter {
    @TypeConverter
    fun toDateTime(dateString: String?): LocalDateTime? =
        dateString?.let { LocalDateTime.parse(dateString) }

    @TypeConverter
    fun fromDateTime(date: LocalDateTime?): String? = date?.toString()

    @TypeConverter
    fun toColor(colorString: String?): Int? =
        colorString?.let { Color.parseColor(it) }

    @TypeConverter
    fun fromColor(color: Int?): String? = color?.let {
        String.format("#%06X", 0xFFFFFF and Color.valueOf(it).toArgb())
    }
}