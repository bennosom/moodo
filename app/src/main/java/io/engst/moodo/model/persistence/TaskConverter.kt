package io.engst.moodo.model.persistence

import androidx.room.TypeConverter
import java.time.LocalDateTime

class TaskConverter {
    @TypeConverter
    fun toDateTime(dateString: String?): LocalDateTime? =
        if (dateString == null) null
        else LocalDateTime.parse(dateString)

    @TypeConverter
    fun fromDateTime(date: LocalDateTime?): String? = date?.toString()
}
