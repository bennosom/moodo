package io.engst.moodo.ui.tasks.touchhelper

import io.engst.moodo.ui.tasks.ListItem

interface ListItemDragCallback {
    fun canDrag(position: Int, item: ListItem): Boolean
    fun onDragStart(position: Int, item: ListItem)
    fun onDragMove(position: Int, item: ListItem, targetPosition: Int, targetItem: ListItem)
    fun canDrop(position: Int, item: ListItem, targetPosition: Int, targetItem: ListItem): Boolean
    fun onDrop(position: Int, item: ListItem)
}