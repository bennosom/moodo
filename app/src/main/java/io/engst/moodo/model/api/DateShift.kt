package io.engst.moodo.model.api

import android.content.Context
import io.engst.moodo.R

enum class DateShift {
    OneDay,
    TwoDays,
    OneWeek,
    OneMonth;

    companion object {
        fun toText(context: Context, shiftBy: DateShift?): String = when (shiftBy) {
            OneDay -> context.getString(R.string.shift_by_one_day)
            TwoDays -> context.getString(R.string.shift_by_two_days)
            OneWeek -> context.getString(R.string.shift_by_one_week)
            OneMonth -> context.getString(R.string.shift_by_one_month)
            else -> ""
        }
    }
}
