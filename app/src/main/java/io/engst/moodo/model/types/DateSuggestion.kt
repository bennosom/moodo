package io.engst.moodo.model.types

import io.engst.moodo.R

enum class DateSuggestion {
    Today,
    Tomorrow,
    NextWeek,
    Later,
    Custom
}

val DateSuggestion.textId: Int
    get() = when (this) {
        DateSuggestion.Today -> R.string.today
        DateSuggestion.Tomorrow -> R.string.tomorrow
        DateSuggestion.NextWeek -> R.string.next_week
        DateSuggestion.Later -> R.string.later
        DateSuggestion.Custom -> R.string.due_date_custom
    }