package io.engst.moodo.ui.tasks

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import io.engst.moodo.R
import io.engst.moodo.databinding.TaskListItemBinding
import io.engst.moodo.databinding.TaskListItemHeaderBinding
import io.engst.moodo.model.api.Task
import io.engst.moodo.shared.Logger
import io.engst.moodo.shared.injectLogger
import io.engst.moodo.ui.tasks.task.convertDateRelativeFormatted
import io.engst.moodo.ui.tasks.task.convertDateTimeFormatted
import java.time.LocalDateTime

sealed class ListItem {
    abstract val id: Long
}

data class HeaderListItem(
    override val id: Long,
    val date: LocalDateTime
) : ListItem()

data class TaskListItem(
    override val id: Long,
    val task: Task
) : ListItem()

typealias OnTaskClick = (task: Task) -> Unit

class TaskListAdapter(private val onClick: OnTaskClick) :
    ListAdapter<ListItem, RecyclerView.ViewHolder>(ItemDiffer) {

    enum class ViewType {
        Header,
        Task
    }

    class TaskViewHolder(
        private val binding: TaskListItemBinding,
        private val onClick: OnTaskClick
    ) : RecyclerView.ViewHolder(binding.root) {
        var task: Task? = null

        fun bind(item: TaskListItem) {
            task = item.task
            with(binding) {
                descriptionText.text = item.task.description
                dueDate.text = item.task.doneDate?.let { doneDate ->
                    convertDateTimeFormatted(doneDate)
                } ?: convertDateTimeFormatted(item.task.dueDate)
                val textColor = root.resources.getColor(
                    if (item.task.isExpired) R.color.task_due_date
                    else R.color.task_date,
                    root.context.theme
                )
                dueDate.setTextColor(textColor)
                root.setOnClickListener {
                    onClick(item.task)
                }
            }
        }
    }

    class HeaderViewHolder(
        private val binding: TaskListItemHeaderBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: HeaderListItem) {
            binding.headerText.text = convertDateRelativeFormatted(item.date)
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
                HeaderViewHolder(
                    TaskListItemHeaderBinding.inflate(inflater, parent, false)
                )
            ViewType.Task.ordinal ->
                TaskViewHolder(
                    TaskListItemBinding.inflate(inflater, parent, false),
                    onClick
                )
            else -> throw IllegalArgumentException("unknown view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) = when (holder) {
        is HeaderViewHolder -> {
            holder.bind(getItem(position) as HeaderListItem)
        }
        is TaskViewHolder -> {
            holder.bind(getItem(position) as TaskListItem)
        }
        else -> throw IllegalArgumentException("unknown view holder type")
    }

    object ItemDiffer : DiffUtil.ItemCallback<ListItem>() {
        private val logger: Logger by injectLogger()

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
