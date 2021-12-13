package io.engst.moodo.model.types

enum class TaskAction(val action: String) {
    Edit("$actionPrefix.edit"),
    ShiftOneDay("$actionPrefix.shift_one_day"),
    ShiftOneWeek("$actionPrefix.shift_one_week"),
    Done("$actionPrefix.done")
}