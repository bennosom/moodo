package io.engst.moodo.ui.tasks.touchhelper

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.os.VibrationEffect
import android.os.Vibrator
import android.text.TextPaint
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.getSystemService
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.*
import androidx.recyclerview.widget.RecyclerView
import io.engst.moodo.R
import io.engst.moodo.model.types.DateShift
import io.engst.moodo.ui.tasks.ListItem
import io.engst.moodo.ui.tasks.ListItemAdapter
import io.engst.moodo.ui.tasks.ListItemViewHolder

const val swipeThreshold = 0.3f

class TouchHelperCallback(
    private val context: Context,
    private val swipeContract: ListItemSwipeCallback,
    private val dragContract: ListItemDragCallback
) : ItemTouchHelper.Callback() {

    private var dateShiftAmount: DateShift = DateShift.OneDay

    private val shiftByArea = SwipeArea(
        arrayOf(
            AppCompatResources.getDrawable(context, R.drawable.ic_more_time_24),
            AppCompatResources.getDrawable(context, R.drawable.ic_refresh_24)
        ),
        arrayOf(Color.parseColor("#ffffff"), Color.parseColor("#ffffff")),
        arrayOf(Color.parseColor("#6052d1"), Color.parseColor("#ebc934")),
        Paint.Align.RIGHT
    )

    private val resolveArea = SwipeArea(
        arrayOf(
            AppCompatResources.getDrawable(context, R.drawable.ic_done_24),
            AppCompatResources.getDrawable(context, R.drawable.ic_outline_delete_24)
        ),
        arrayOf(Color.parseColor("#ffffff"), Color.parseColor("#ffffff")),
        arrayOf(Color.parseColor("#52d165"), Color.parseColor("#d15252")),
        Paint.Align.LEFT
    )

    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        super.onSelectedChanged(viewHolder, actionState)

        (viewHolder as? ListItemViewHolder.TaskViewHolder)?.item?.let { item ->
            when (actionState) {
                ACTION_STATE_DRAG -> {
                    dragContract.onDragStart(viewHolder.absoluteAdapterPosition, item)
                    viewHolder.selected = true
                }
                ACTION_STATE_SWIPE -> {
                    //swipeContract.onStartSwipe(viewHolder.adapterPosition, item)
                }
                ACTION_STATE_IDLE -> {
                }
            }
        }
    }

    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        return when (viewHolder.itemViewType) {
            ListItemAdapter.ViewType.Header.ordinal -> 0 // don't drag or swipe header items
            ListItemAdapter.ViewType.Task.ordinal -> {
                val holder = viewHolder as ListItemViewHolder.TaskViewHolder
                val item = holder.item
                val task = (item as ListItem.TaskItem).task
                when {
                    task.isDone -> makeMovementFlags(0, LEFT or RIGHT)
                    task.isScheduled -> makeMovementFlags(0, LEFT or RIGHT)
                    !(task.isScheduled && task.isDone) -> {
                        if (dragContract.canDrag(holder.absoluteAdapterPosition, item)) {
                            makeMovementFlags(UP or DOWN, LEFT)
                        } else {
                            0
                        }
                    }
                    else -> 0
                }
            }
            else -> 0
        }
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        when (direction) {
            LEFT -> {
                val holder = viewHolder as ListItemViewHolder.TaskViewHolder
                val task = (holder.item as ListItem.TaskItem).task
                if (task.isDone) {
                    swipeContract.onRemoved(viewHolder.absoluteAdapterPosition)
                } else {
                    swipeContract.onDone(viewHolder.absoluteAdapterPosition)
                }
            }
            else -> swipeContract.onShift(viewHolder.absoluteAdapterPosition, dateShiftAmount)
        }
    }

    override fun canDropOver(
        recyclerView: RecyclerView,
        current: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        require(current is ListItemViewHolder.TaskViewHolder)
        return when (target.itemViewType) {
            ListItemAdapter.ViewType.Header.ordinal -> false
            ListItemAdapter.ViewType.Task.ordinal -> {
                val targetHolder = target as ListItemViewHolder.TaskViewHolder
                val targetTask = (targetHolder.item as ListItem.TaskItem).task
                when {
                    targetTask.isBacklog -> dragContract.canDrop(
                        current.absoluteAdapterPosition,
                        current.item!!,
                        target.absoluteAdapterPosition,
                        targetHolder.item!!
                    )
                    else -> false
                }
            }
            else -> false
        }
    }

    override fun onMove(
        recyclerView: RecyclerView,
        current: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return (recyclerView.adapter as ListItemAdapter).run {
            val canMove = when (target.itemViewType) {
                ListItemAdapter.ViewType.Header.ordinal -> false
                ListItemAdapter.ViewType.Task.ordinal -> {
                    val holder = target as ListItemViewHolder.TaskViewHolder
                    val targetTask = (holder.item as ListItem.TaskItem).task
                    when {
                        targetTask.isBacklog -> true
                        else -> false
                    }
                }
                else -> false
            }

            if (canMove) {
                current as ListItemViewHolder.TaskViewHolder
                target as ListItemViewHolder.TaskViewHolder
                val from = current.absoluteAdapterPosition
                val to = target.absoluteAdapterPosition
                dragContract.onDragMove(from, current.item!!, to, target.item!!)
                true
            } else {
                false
            }
        }
    }

    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        (viewHolder as? ListItemViewHolder.TaskViewHolder)?.let { holder ->
            holder.selected = false
            holder.item?.let { item ->
                dragContract.onDrop(viewHolder.absoluteAdapterPosition, item)
            }
        }

        super.clearView(recyclerView, viewHolder)

        context.getSystemService<Vibrator>()
            ?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK))
    }

    override fun getMoveThreshold(viewHolder: RecyclerView.ViewHolder): Float = 0.3f

    override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder): Float = swipeThreshold

    override fun onChildDraw(
        canvas: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        val taskViewHolder = viewHolder as ListItemViewHolder.TaskViewHolder
        val task = (taskViewHolder.item as ListItem.TaskItem).task
        val itemView = taskViewHolder.itemView
        val dRatio = dX / itemView.width
        val tx = dX.toInt()
        val swipeRight = tx >= 0
        val mode = if (task.isDone) 1 else 0

        if (tx == 0 || tx == -itemView.width) {
            return super.onChildDraw(
                canvas,
                recyclerView,
                viewHolder,
                dX,
                dY,
                actionState,
                isCurrentlyActive
            )
        }

        if (swipeRight) {
            if (isCurrentlyActive) {
                dateShiftAmount = convertToDateShift(dRatio)
            }

            val shiftByRect = Rect(
                itemView.left,
                itemView.top,
                itemView.left + tx,
                itemView.bottom
            )
            val text =
                if (dRatio > swipeThreshold) DateShift.toText(context, dateShiftAmount) else ""
            shiftByArea.draw(canvas, shiftByRect, text, mode)
        } else {
            val resolveRect = Rect(
                itemView.right + tx,
                itemView.top,
                itemView.right,
                itemView.bottom
            )
            resolveArea.draw(canvas, resolveRect, "", mode)
        }

        super.onChildDraw(canvas, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }

    private fun convertToDateShift(ratio: Float): DateShift {
        val offset = (1f - swipeThreshold) / 0.03f
        return when (ratio) {
            in swipeThreshold..swipeThreshold + offset * 0.01f -> DateShift.OneDay
            in swipeThreshold + offset * 0.01f..swipeThreshold + offset * 0.02f -> DateShift.TwoDays
            in swipeThreshold + offset * 0.02f..swipeThreshold + offset * 0.03f -> DateShift.OneWeek
            else -> DateShift.None
        }
    }

    class SwipeArea(
        private val image: Array<Drawable?>,
        private val color: Array<Int>,
        private val backgroundColor: Array<Int>,
        private val alignment: Paint.Align
    ) {
        fun draw(canvas: Canvas, rect: Rect, text: String = "", mode: Int) {
            val paint = Paint()

            val imageWidth = image[mode]?.intrinsicWidth ?: 0
            val imageHeight = image[mode]?.intrinsicHeight ?: 0

            // background
            paint.color = backgroundColor[mode]
            canvas.drawRect(rect, paint)

            // image
            val imageTop = rect.top + (rect.height() - imageHeight) / 2
            val imageMargin = 24
            val imageRight =
                if (alignment == Paint.Align.RIGHT) rect.right - imageMargin else rect.left + imageWidth + imageMargin
            val imageLeft =
                if (alignment == Paint.Align.RIGHT) imageRight - imageWidth else rect.left + imageMargin
            val imageBottom = imageTop + imageHeight
            image[mode]?.setBounds(imageLeft, imageTop, imageRight, imageBottom)
            image[mode]?.draw(canvas)

            // text
            if (mode == 0) {
                val textPaint = TextPaint(paint)
                textPaint.color = color[mode]
                textPaint.textSize = 45f

                val textBounds = Rect()
                textPaint.getTextBounds(text, 0, text.length, textBounds)
                val textMarginTop = (rect.height() + textBounds.height()) / 2
                val textX = rect.left + 24
                val textY = rect.top + textMarginTop
                canvas.drawText(text, textX.toFloat(), textY.toFloat(), textPaint)
            }
        }
    }
}
