package io.engst.moodo.model.types

import io.engst.moodo.R

enum class TimeSuggestion {
    Morning,
    Midday,
    Afternoon,
    Custom
}

val TimeSuggestion.textId: Int
    get() = when (this) {
        TimeSuggestion.Morning -> R.string.due_time_suggestion_morning
        TimeSuggestion.Midday -> R.string.due_time_suggestion_midday
        TimeSuggestion.Afternoon -> R.string.due_time_suggestion_afternoon
        TimeSuggestion.Custom -> R.string.due_time_custom
    }