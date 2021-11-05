package io.engst.moodo.ui.tasks

import android.graphics.Color
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
import java.time.LocalDateTime

sealed class ListItem {
    abstract val id: String
    abstract val index: Int
}

data class GroupListItem(
    override val id: String,
    override val index: Int,
    val labelResId: Int,
    val date: LocalDateTime
) : ListItem()

data class TaskListItem(
    override val id: String,
    override val index: Int,
    val dateText: String,
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
                    descriptionText.paintFlags = if (item.task.isDone) {
                        descriptionText.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                    } else {
                        descriptionText.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                    }
                    descriptionText.typeface = if (item.task.isDue && !item.task.isDone) {
                        Typeface.DEFAULT_BOLD
                    } else {
                        Typeface.DEFAULT
                    }
                    descriptionText.text = item.task.description
                    descriptionText.setTextColor(if (item.task.isDone) Color.GRAY else Color.BLACK)

                    dueDate.text = item.dateText
                    dueDate.setTextColor(if (item.task.isDone) Color.GRAY else Color.BLACK)

                    root.setOnClickListener {
                        onClick(item.task)
                    }
                }
            }
        }

        class HeaderViewHolder(
            private val binding: TaskListItemHeaderBinding
        ) : ViewHolder(binding.root) {
            fun bind(item: GroupListItem) {
                this.item = item

                binding.headerText.text = binding.root.context.getString(item.labelResId)
            }
        }
    }

    override fun getItemViewType(position: Int): Int = when (getItem(position)) {
        is GroupListItem -> ViewType.Header.ordinal
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
            holder.bind(getItem(position) as GroupListItem)
        }
        is ViewHolder.TaskViewHolder -> {
            holder.bind(getItem(position) as TaskListItem)
        }
        else -> throw IllegalArgumentException("unknown view holder type")
    }

    object ItemDiffer : DiffUtil.ItemCallback<ListItem>() {
        override fun areItemsTheSame(oldItem: ListItem, newItem: ListItem): Boolean {
            return when {
                oldItem is GroupListItem && newItem is GroupListItem -> oldItem.date == newItem.date
                oldItem is TaskListItem && newItem is TaskListItem -> oldItem.id == newItem.id
                else -> false
            }
        }

        override fun areContentsTheSame(oldItem: ListItem, newItem: ListItem): Boolean {
            return oldItem == newItem
        }
    }
}