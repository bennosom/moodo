package io.engst.moodo.ui.tasks

import android.graphics.Paint
import android.graphics.Typeface
import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import io.engst.moodo.R
import io.engst.moodo.databinding.TaskListItemBinding
import io.engst.moodo.databinding.TaskListItemHeaderBinding

sealed class TaskListViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    var item: ListItem? = null

    class TaskViewHolder(
        private val binding: TaskListItemBinding,
        private val clickListener: TaskItemClickListener
    ) : TaskListViewHolder(binding.root) {

        var selected: Boolean = false
            set(value) {
                field = value
                binding.root.isDragged = field
            }

        fun bind(item: ListItem.TaskItem) {
            this.item = item
            with(binding) {
                with(descriptionText) {
                    paintFlags = if (item.task.isDone) {
                        paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                    } else {
                        paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                    }
                    typeface = if (item.task.isDue && !item.task.isDone) {
                        Typeface.DEFAULT_BOLD
                    } else {
                        Typeface.DEFAULT
                    }
                    setTextColor(
                        if (item.task.isDone) context.getColor(R.color.text2Color)
                        else context.getColor(R.color.textOnSurface)
                    )
                    text = item.task.description
                }
                with(dueDate) {
                    setTextColor(
                        if (item.task.isDone) context.getColor(R.color.text2Color)
                        else context.getColor(R.color.textOnSurface)
                    )
                    text = item.dateText
                }
                root.setOnClickListener {
                    clickListener.onClick(item.task)
                }
            }
        }
    }

    class HeaderViewHolder(
        private val binding: TaskListItemHeaderBinding
    ) : TaskListViewHolder(binding.root) {
        fun bind(item: ListItem.GroupItem) {
            this.item = item
            binding.headerText.text = binding.root.context.getString(item.labelResId)
            binding.messageText.text = item.message
            binding.messageText.isVisible = item.message != null
        }
    }
}