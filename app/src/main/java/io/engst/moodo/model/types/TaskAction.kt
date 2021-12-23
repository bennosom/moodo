package io.engst.moodo.model.types

enum class TaskAction(val action: String) {
    Edit("$actionPrefix.edit"),
    ShiftToTomorrow("$actionPrefix.shift_one_day"),
    ShiftToNextWeek("$actionPrefix.shift_one_week"),
    Done("$actionPrefix.done")
}