package bex.bootcamp.moodo.tasklist

import android.content.Context
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.text.TextPaint
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.ACTION_STATE_SWIPE
import androidx.recyclerview.widget.RecyclerView
import bex.bootcamp.moodo.R

const val DP16 = 16.0f
fun dpToPx(dp: Float, scale: Float): Int = (dp * scale + 0.5f).toInt()
const val swipeThreshold = 0.25f

abstract class SwipeTaskCallback(val context: Context) :
    ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

    private var lastTimeShiftAction = ""
    private var moreIcon = ContextCompat.getDrawable(context, R.drawable.ic_more_vert_black_24dp)!!
    private val intrinsicWidth = moreIcon.intrinsicWidth
    private val intrinsicHeight = moreIcon.intrinsicHeight
    private val background = ColorDrawable()
    private val backgroundColorSwipeLeft = Color.parseColor("#00ff00")
    private val backgroundColorSwipeRight = Color.parseColor("#0000ff")

    private val margin16dp = dpToPx(DP16, context.resources.displayMetrics.density)

    private val clearPaint = Paint().apply { xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR) }

    // disable swipe for header items
    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        if (viewHolder?.itemViewType == TaskListAdapter.ListItemType.Header.ordinal)
            return 0
        return super.getMovementFlags(recyclerView, viewHolder)
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        viewHolder2: RecyclerView.ViewHolder
    ): Boolean {
        return false
    }

    override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder): Float {
        return swipeThreshold
    }

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        val itemView = viewHolder.itemView
        val itemHeight = itemView.bottom - itemView.top
        val isCanceled = dX == 0f && !isCurrentlyActive
        val swipeRight = dX > 0

        if (isCanceled) {
            clearCanvas(
                c,
                itemView.right + dX,
                itemView.top.toFloat(),
                itemView.right.toFloat(),
                itemView.bottom.toFloat()
            )
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            return
        }

        // Draw background
        if (swipeRight) {
            background.color = backgroundColorSwipeRight
            background.setBounds(
                itemView.left,
                itemView.top,
                itemView.left + dX.toInt(),
                itemView.bottom
            )
        } else {
            background.color = backgroundColorSwipeLeft
            background.setBounds(
                itemView.right + dX.toInt(),
                itemView.top,
                itemView.right,
                itemView.bottom
            )
        }
        background.draw(c)

        val swipeProgress = dX / itemView.width
        if (swipeProgress > swipeThreshold) {
            // draw text
            val textPaint = TextPaint()
            textPaint.setColor(Color.BLACK)
            textPaint.textSize = 45f

            val text = if (isCurrentlyActive) getTimeShiftAmount(swipeProgress) else lastTimeShiftAction
            val textBound = Rect()
            textPaint.getTextBounds(text, 0, text.length, textBound)
            val textMarginTop = (itemHeight + textBound.height()) / 2
            val textX = itemView.left + margin16dp
            val textY = itemView.top + textMarginTop
            c.drawText(text, textX.toFloat(), textY.toFloat(), textPaint)
        }

        // Calculate position of delete icon
        val deleteIconTop = itemView.top + (itemHeight - intrinsicHeight) / 2
        val deleteIconMargin = (itemHeight - intrinsicHeight) / 2
        val deleteIconLeft = itemView.right - deleteIconMargin - intrinsicWidth
        val deleteIconRight = itemView.right - deleteIconMargin
        val deleteIconBottom = deleteIconTop + intrinsicHeight

        // Draw the icon
        moreIcon.setBounds(deleteIconLeft, deleteIconTop, deleteIconRight, deleteIconBottom)
        moreIcon.draw(c)

        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }

    private fun getTimeShiftAmount(progress: Float): String = when ((progress * 100).toInt()) {
        in 0..44 -> "Morgen"
        in 44..63 -> "2 Tage"
        in 63..82 -> "Bald"
        else -> "Irgendwann"
    }.also { lastTimeShiftAction = it }

    private fun clearCanvas(c: Canvas?, left: Float, top: Float, right: Float, bottom: Float) {
        c?.drawRect(left, top, right, bottom, clearPaint)
    }
}
