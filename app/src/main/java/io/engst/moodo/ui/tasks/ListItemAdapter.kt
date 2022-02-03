package io.engst.moodo.ui.tasks

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import io.engst.moodo.databinding.TaskListItemBinding
import io.engst.moodo.databinding.TaskListItemHeaderBinding
import io.engst.moodo.model.types.DateShift
import io.engst.moodo.model.types.Task
import io.engst.moodo.shared.Logger
import io.engst.moodo.shared.injectLogger
import io.engst.moodo.ui.tasks.touchhelper.ListItemDragCallback
import io.engst.moodo.ui.tasks.touchhelper.ListItemSwipeCallback
import io.engst.moodo.ui.tasks.touchhelper.TouchHelperCallback
import java.util.*

class ListItemAdapter(
    private val clickListener: ListItemClickListener,
    private val swipeListener: ListItemSwipeListener,
    private val dragListener: ListItemDragListener
) : RecyclerView.Adapter<ListItemViewHolder>() {

    enum class ViewType {
        Header,
        Task
    }

    private val logger: Logger by injectLogger("view")

    private var recyclerView: RecyclerView? = null

    private val currentItems: MutableList<ListItem> = mutableListOf()

    private val diffCallback = object : DiffUtil.Callback() {
        val oldList: List<ListItem> = currentItems
        var newList: List<ListItem> = emptyList()

        override fun getOldListSize(): Int = oldList.size

        override fun getNewListSize(): Int = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
            oldList[oldItemPosition].id == newList[newItemPosition].id

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
            oldList[oldItemPosition] == newList[newItemPosition]
    }

    private var itemTouchHelper: ItemTouchHelper? = null

    private var dragItem: ListItem? = null
    private var dropItem: ListItem? = null

    private val swipeContract = object : ListItemSwipeCallback {
        override fun onDone(position: Int) {
            findTaskForAdapterPosition(position)?.let { task ->
                swipeListener.onDone(task)
            }
        }

        override fun onShift(position: Int, shiftBy: DateShift) {
            findTaskForAdapterPosition(position)?.let { task ->
                swipeListener.onShift(task, shiftBy)
            }
        }

        override fun onRemoved(position: Int) {
            findTaskForAdapterPosition(position)?.let { task ->
                swipeListener.onRemoved(task)
            }
        }
    }

    private val dragContract = object : ListItemDragCallback {
        override fun canDrag(position: Int, item: ListItem): Boolean {
            logger.debug { "canDrag ${item.id} at $position" }
            return (item as? ListItem.TaskItem)?.task?.let { dragTask ->
                dragListener.canDrag(dragTask)
            } ?: false
        }

        override fun onDragStart(position: Int, item: ListItem) {
            logger.debug { "onDragStart #${item.id} at $position" }
            (item as? ListItem.TaskItem)?.task?.let { dragTask ->
                dragListener.onDragStart(dragTask)
            }
            dragItem = item
        }

        override fun canDrop(
            position: Int,
            item: ListItem,
            targetPosition: Int,
            targetItem: ListItem
        ): Boolean {
            logger.debug { "canDrop ${item.id} at $targetPosition" }
            return (item as? ListItem.TaskItem)?.task?.let { dragTask ->
                (targetItem as? ListItem.TaskItem)?.task?.let { dropTask ->
                    dragListener.canDrop(dragTask, dropTask)
                }
            } ?: false
        }

        override fun onDragMove(
            position: Int,
            item: ListItem,
            targetPosition: Int,
            targetItem: ListItem
        ) {
            logger.debug { "onDragMove #${item.id} to $targetPosition" }
            (item as? ListItem.TaskItem)?.task?.let { dragTask ->
                (targetItem as? ListItem.TaskItem)?.task?.let { dropTask ->
                    Collections.swap(currentItems, position, targetPosition)
                    notifyItemMoved(position, targetPosition)
                    dragListener.onDragMove(dragTask, dropTask)
                    dropItem = targetItem
                }
            }
        }

        override fun onDrop(position: Int, item: ListItem) {
            logger.debug { "onDrop #${item.id}" }
            (dragItem as? ListItem.TaskItem)?.task?.let { dragTask ->
                (dropItem as? ListItem.TaskItem)?.task?.let { dropTask ->
                    dragListener.onDrop(dragTask, dropTask)
                }
            }
            dragItem = null
            dropItem = null
        }
    }

    override fun getItemViewType(position: Int): Int = when (getItem(position)) {
        is ListItem.GroupItem -> ViewType.Header.ordinal
        is ListItem.TaskItem -> ViewType.Task.ordinal
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListItemViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            ViewType.Header.ordinal ->
                ListItemViewHolder.HeaderViewHolder(
                    TaskListItemHeaderBinding.inflate(inflater, parent, false)
                )
            ViewType.Task.ordinal ->
                ListItemViewHolder.TaskViewHolder(
                    TaskListItemBinding.inflate(inflater, parent, false),
                    clickListener
                )
            else -> throw IllegalArgumentException("unknown view type")
        }
    }

    override fun onBindViewHolder(holder: ListItemViewHolder, position: Int) = when (holder) {
        is ListItemViewHolder.HeaderViewHolder -> holder.bind(getItem(position) as ListItem.GroupItem)
        is ListItemViewHolder.TaskViewHolder -> holder.bind(getItem(position) as ListItem.TaskItem)
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        this.recyclerView = recyclerView
        super.onAttachedToRecyclerView(recyclerView)
        attachTouchHelper(recyclerView)
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        detachTouchHelper()
        super.onDetachedFromRecyclerView(recyclerView)
        this.recyclerView = null
    }

    override fun getItemCount(): Int = diffCallback.oldList.size

    fun submitList(updatedList: List<ListItem>) {
        diffCallback.newList = updatedList
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        currentItems.clear()
        currentItems.addAll(updatedList)
        diffResult.dispatchUpdatesTo(this)
    }

    fun getCurrentItems(): List<ListItem> = currentItems

    fun startDrag(holder: ListItemViewHolder) {
        dragItem = holder.item
        itemTouchHelper?.startDrag(holder)
    }

    fun isDragActive(): Boolean = dragItem != null

    fun stopDrag() {
        detachTouchHelper()
        recyclerView?.let { attachTouchHelper(it) }
        dragItem = null
    }

    private fun getItem(position: Int) = diffCallback.oldList[position]

    private fun attachTouchHelper(recyclerView: RecyclerView) {
        itemTouchHelper = ItemTouchHelper(
            TouchHelperCallback(
                recyclerView.context,
                swipeContract,
                dragContract
            )
        ).apply {
            attachToRecyclerView(recyclerView)
        }
    }

    private fun detachTouchHelper() {
        itemTouchHelper?.attachToRecyclerView(null)
        itemTouchHelper = null
    }

    private fun findTaskForAdapterPosition(position: Int): Task? =
        recyclerView?.findViewHolderForAdapterPosition(position)?.let {
            (it as ListItemViewHolder.TaskViewHolder).let { taskViewHolder ->
                (taskViewHolder.item as ListItem.TaskItem).task
            }
        }
}