package bex.bootcamp.moodo.ui.tasks

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import bex.bootcamp.moodo.R
import bex.bootcamp.moodo.databinding.ListItemHeaderBinding
import bex.bootcamp.moodo.databinding.ListItemTaskBinding
import bex.bootcamp.moodo.model.api.Task
import bex.bootcamp.moodo.ui.tasks.task.convertDateRelativeFormatted
import bex.bootcamp.moodo.ui.tasks.task.convertDateTimeFormatted
import java.time.LocalDateTime

sealed class ListItem {
    abstract val id: Long
}

data class HeaderListItem(
    override val id: Long = -1L,
    val date: LocalDateTime
) : ListItem()

data class TodoListItem(
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
        private val binding: ListItemTaskBinding,
        private val onClick: OnTaskClick
    ) : RecyclerView.ViewHolder(binding.root) {
        var task: Task? = null

        fun bind(item: TodoListItem) {
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
        private val binding: ListItemHeaderBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: HeaderListItem) {
            binding.headerText.text = convertDateRelativeFormatted(item.date)
        }
    }

    override fun getItemViewType(position: Int): Int = when (getItem(position)) {
        is HeaderListItem -> ViewType.Header.ordinal
        is TodoListItem -> ViewType.Task.ordinal
        else -> throw IllegalArgumentException("unknown list item type")
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            ViewType.Header.ordinal ->
                HeaderViewHolder(
                    ListItemHeaderBinding.inflate(inflater, parent, false)
                )
            ViewType.Task.ordinal ->
                TaskViewHolder(
                    ListItemTaskBinding.inflate(inflater, parent, false),
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
            holder.bind(getItem(position) as TodoListItem)
        }
        else -> throw IllegalArgumentException("unknown view holder type")
    }

    object ItemDiffer : DiffUtil.ItemCallback<ListItem>() {
        override fun areItemsTheSame(oldItem: ListItem, newItem: ListItem): Boolean {
            println("${oldItem.id} == ${newItem.id}")
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ListItem, newItem: ListItem): Boolean {
            println("$oldItem == $newItem")
            return oldItem == newItem
        }
    }
}
