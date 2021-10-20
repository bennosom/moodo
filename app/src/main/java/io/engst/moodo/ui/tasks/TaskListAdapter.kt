package io.engst.moodo.ui.tasks

import android.graphics.Paint
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import io.engst.moodo.databinding.TaskListItemBinding
import io.engst.moodo.databinding.TaskListItemHeaderBinding
import io.engst.moodo.model.types.Task
import io.engst.moodo.shared.prettyFormat
import io.engst.moodo.shared.prettyFormatRelative
import java.time.LocalDateTime

sealed class ListItem {
    abstract val id: String
}

data class HeaderListItem(
    override val id: String,
    val date: LocalDateTime
) : ListItem()

data class TaskListItem(
    override val id: String,
    val task: Task
) : ListItem()

typealias OnTaskListItemClicked = (task: Task) -> Unit

class TaskListAdapter(private val onClick: OnTaskListItemClicked) :
    ListAdapter<ListItem, RecyclerView.ViewHolder>(ItemDiffer) {

    enum class ViewType {
        Header,
        Task
    }

    sealed class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var item: ListItem? = null

        class TaskViewHolder(
            private val binding: TaskListItemBinding,
            private val onClick: OnTaskListItemClicked
        ) : ViewHolder(binding.root) {
            fun bind(item: TaskListItem) {
                this.item = item

                with(binding) {
                    descriptionText.paintFlags = if (item.task.done) {
                        Paint.STRIKE_THRU_TEXT_FLAG
                    } else {
                        0
                    }
                    descriptionText.setTypeface(
                        null,
                        if (item.task.due) Typeface.BOLD else Typeface.NORMAL
                    )
                    descriptionText.text = item.task.description

                    dueDate.setTypeface(
                        null,
                        if (item.task.done) Typeface.ITALIC else Typeface.NORMAL
                    )
                    dueDate.text = (item.task.doneDate ?: item.task.dueDate)?.prettyFormat

                    root.setOnClickListener {
                        onClick(item.task)
                    }
                }
            }
        }

        class HeaderViewHolder(
            private val binding: TaskListItemHeaderBinding
        ) : ViewHolder(binding.root) {
            fun bind(item: HeaderListItem) {
                this.item = item

                binding.headerText.text =
                    binding.root.context.getString(item.date.prettyFormatRelative)
            }
        }
    }

    override fun getItemViewType(position: Int): Int = when (getItem(position)) {
        is HeaderListItem -> ViewType.Header.ordinal
        is TaskListItem -> ViewType.Task.ordinal
        else -> throw IllegalArgumentException("unknown list item type")
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            ViewType.Header.ordinal ->
                ViewHolder.HeaderViewHolder(
                    TaskListItemHeaderBinding.inflate(inflater, parent, false)
                )
            ViewType.Task.ordinal ->
                ViewHolder.TaskViewHolder(
                    TaskListItemBinding.inflate(inflater, parent, false),
                    onClick
                )
            else -> throw IllegalArgumentException("unknown view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) = when (holder) {
        is ViewHolder.HeaderViewHolder -> {
            holder.bind(getItem(position) as HeaderListItem)
        }
        is ViewHolder.TaskViewHolder -> {
            holder.bind(getItem(position) as TaskListItem)
        }
        else -> throw IllegalArgumentException("unknown view holder type")
    }

    object ItemDiffer : DiffUtil.ItemCallback<ListItem>() {
        override fun areItemsTheSame(oldItem: ListItem, newItem: ListItem): Boolean {
            return when {
                oldItem is HeaderListItem && newItem is HeaderListItem -> oldItem.date == newItem.date
                oldItem is TaskListItem && newItem is TaskListItem -> oldItem.id == newItem.id
                else -> false
            }
        }

        override fun areContentsTheSame(oldItem: ListItem, newItem: ListItem): Boolean {
            return oldItem == newItem
        }
    }
}