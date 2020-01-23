package bex.bootcamp.moodo.tasklist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import bex.bootcamp.moodo.DateGroup
import bex.bootcamp.moodo.databinding.ListItemHeaderBinding
import bex.bootcamp.moodo.databinding.ListItemTaskBinding
import bex.bootcamp.moodo.domain.Task
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class TaskListAdapter : ListAdapter<ListItem, RecyclerView.ViewHolder>(TaskDiffCallback()) {

    private val localScope = CoroutineScope(Dispatchers.Default)

    enum class ListItemType {
        Header,
        Task
    }

    class DefaultViewHolder private constructor(val binding: ListItemTaskBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(task: Task) {
            binding.task = task
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): DefaultViewHolder {
                val inflater = LayoutInflater.from(parent.context)
                val binding = ListItemTaskBinding.inflate(inflater, parent, false)
                return DefaultViewHolder(binding)
            }
        }
    }

    class HeaderViewHolder private constructor(val binding: ListItemHeaderBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(date: LocalDate) {
            binding.date = date
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): HeaderViewHolder {
                val inflater = LayoutInflater.from(parent.context)
                val binding = ListItemHeaderBinding.inflate(inflater, parent, false)
                return HeaderViewHolder(binding)
            }
        }
    }

    override fun getItemViewType(position: Int): Int = when (getItem(position)) {
        is ListItem.HeaderListItem -> ListItemType.Header.ordinal
        else -> ListItemType.Task.ordinal
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        when (viewType) {
            ListItemType.Header.ordinal -> HeaderViewHolder.from(parent)
            else -> DefaultViewHolder.from(parent)
        }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) = when (holder) {
        is HeaderViewHolder -> {
            val headerItem = getItem(position) as ListItem.HeaderListItem
            holder.bind(headerItem.date)
        }
        is DefaultViewHolder -> {
            val taskItem = getItem(position) as ListItem.TaskListItem
            holder.bind(taskItem.task)
        }
        else -> throw IllegalArgumentException("unknown list item type")
    }

    fun submit(list: List<Task>?) {
        localScope.launch {
            val group = DateGroup(LocalDate.now())

            val listItems = mutableListOf<ListItem>()
            val headerDates = mutableSetOf<LocalDate>()
            list?.forEach { task ->
                task.due?.let {
                    group.getDateFor(it.toLocalDate())?.also {
                        headerDates.add(it)
                    }
                }
                listItems.add(ListItem.TaskListItem(task))
            }
            listItems.addAll(headerDates.map { ListItem.HeaderListItem(it) })
            listItems.sortBy { it.dateTime }

            withContext(Dispatchers.Main) {
                submitList(listItems)
            }
        }
    }
}

class TaskDiffCallback : DiffUtil.ItemCallback<ListItem>() {
    override fun areItemsTheSame(oldItem: ListItem, newItem: ListItem): Boolean {
        // TODO: compare task ids
        return false
    }

    override fun areContentsTheSame(oldItem: ListItem, newItem: ListItem): Boolean {
        // TODO: compare task contents
        return false
    }
}

sealed class ListItem(val dateTime: LocalDateTime? = LocalDateTime.now()) {
    abstract val id: Long

    data class HeaderListItem(val date: LocalDate) :
        ListItem(LocalDateTime.of(date, LocalTime.MIN)) {
        override val id = Long.MIN_VALUE
    }

    data class TaskListItem(val task: Task) : ListItem(task.due) {
        override val id = 1L
    }
}