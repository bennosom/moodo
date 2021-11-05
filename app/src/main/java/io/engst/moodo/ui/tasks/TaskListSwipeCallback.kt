package io.engst.moodo.ui.tasks

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.text.TextPaint
import androidx.annotation.RequiresApi
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.getSystemService
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import io.engst.moodo.R
import io.engst.moodo.model.types.DateShift

const val swipeThreshold = 0.3f

abstract class SwipeTaskCallback(val context: Context) :
    ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

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

    // disable swipe for header items
    override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) =
        if (viewHolder.itemViewType == TaskListAdapter.ViewType.Header.ordinal) 0
        else super.getMovementFlags(recyclerView, viewHolder)

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        viewHolder2: RecyclerView.ViewHolder
    ) = false

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val holder = viewHolder as TaskListAdapter.ViewHolder.TaskViewHolder
        if (direction == ItemTouchHelper.LEFT) {
            if ((holder.item as TaskListItem).task.isDone) {
                onDelete(viewHolder.adapterPosition)
            } else {
                onDone(viewHolder.adapterPosition)
            }
        } else {
            onShift(viewHolder.adapterPosition, dateShiftAmount)
        }

        context.getSystemService<Vibrator>()
            ?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK))
    }

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
        val taskViewHolder = viewHolder as TaskListAdapter.ViewHolder.TaskViewHolder
        val task = (taskViewHolder.item as TaskListItem).task
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

    abstract fun onDone(position: Int)
    abstract fun onShift(position: Int, shiftBy: DateShift)
    abstract fun onDelete(position: Int)

    private fun convertToDateShift(ratio: Float): DateShift {
        val offset = (1f - swipeThreshold) / 0.04f
        return when (ratio) {
            in swipeThreshold..swipeThreshold + offset * 0.01f -> DateShift.OneDay
            in swipeThreshold + offset * 0.01f..swipeThreshold + offset * 0.02f -> DateShift.TwoDays
            in swipeThreshold + offset * 0.02f..swipeThreshold + offset * 0.03f -> DateShift.OneWeek
            in swipeThreshold + offset * 0.03f..swipeThreshold + offset * 0.04f -> DateShift.OneMonth
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