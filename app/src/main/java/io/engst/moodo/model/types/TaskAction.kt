package io.engst.moodo.model.types

enum class TaskAction(val action: String) {
    Done("$actionPrefix.done"),
    Snooze("$actionPrefix.snooze")
}