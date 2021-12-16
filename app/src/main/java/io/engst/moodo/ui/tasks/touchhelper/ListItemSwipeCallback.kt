package io.engst.moodo.ui.tasks.touchhelper

import io.engst.moodo.model.types.DateShift

interface ListItemSwipeCallback {
    fun onDone(position: Int)
    fun onRemoved(position: Int)
    fun onShift(position: Int, shiftBy: DateShift)
}