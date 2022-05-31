package io.engst.moodo.ui.tasks

import android.graphics.Paint
import android.graphics.Typeface
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import io.engst.moodo.R
import io.engst.moodo.databinding.TaskListItemBinding
import io.engst.moodo.databinding.TaskListItemHeaderBinding
import io.engst.moodo.shared.dp

sealed class ListItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    var item: ListItem? = null

    class TaskViewHolder(
        private val binding: TaskListItemBinding,
        private val clickListener: ListItemClickListener
    ) : ListItemViewHolder(binding.root) {

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
                with(tags) {
                    removeAllViews()
                    item.task.tags.forEach {
                        val tagView = TextView(context)
                        tagView.layoutParams = ViewGroup.MarginLayoutParams(12.dp, 12.dp).apply {
                            marginStart = 4.dp
                            marginEnd = 4.dp
                        }
                        tagView.background = AppCompatResources.getDrawable(context, R.drawable.task_list_item_tag_round)?.apply {
                            setTint(it.color)
                        }
                        addView(tagView)
                    }
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
    ) : ListItemViewHolder(binding.root) {
        fun bind(item: ListItem.GroupItem) {
            this.item = item
            binding.headerText.text = binding.root.context.getString(item.labelResId)
            binding.messageText.text = item.message
            binding.messageText.isVisible = item.message != null
        }
    }
}