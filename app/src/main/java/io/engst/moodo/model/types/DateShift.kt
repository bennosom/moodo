package io.engst.moodo.model.types

import android.content.Context
import io.engst.moodo.R

enum class DateShift {
    None,
    OneDay,
    TwoDays,
    OneWeek;

    companion object {
        fun toText(context: Context, shiftBy: DateShift): String = when (shiftBy) {
            None -> context.getString(R.string.shift_by_nothing)
            OneDay -> context.getString(R.string.shift_by_one_day)
            TwoDays -> context.getString(R.string.shift_by_two_days)
            OneWeek -> context.getString(R.string.shift_by_one_week)
        }
    }
}
