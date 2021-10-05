package io.engst.moodo.model

import io.engst.moodo.R

enum class DateSuggestion {
    Tomorrow,
    In2Days,
    NextMonday,
    Custom
}

val DateSuggestion.textId: Int
    get() = when (this) {
        DateSuggestion.Tomorrow -> R.string.due_date_suggestion_tomorrow
        DateSuggestion.In2Days -> R.string.due_date_suggestion_in2days
        DateSuggestion.NextMonday -> R.string.due_date_suggestion_next_monday
        DateSuggestion.Custom -> R.string.due_date_custom
    }