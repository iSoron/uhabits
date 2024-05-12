package org.isoron.uhabits.activities.habits.list.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.text.TextPaint
import android.view.View
import android.view.View.MeasureSpec.EXACTLY
import org.isoron.uhabits.R
import org.isoron.uhabits.activities.habits.list.ListHabitsActivity
import org.isoron.uhabits.core.models.HabitGroup
import org.isoron.uhabits.utils.getFontAwesome
import org.isoron.uhabits.utils.sp
import org.isoron.uhabits.utils.sres
import org.isoron.uhabits.utils.toMeasureSpec

class AddButtonView(
    context: Context,
    var habitGroup: HabitGroup?
) : View(context),
    View.OnClickListener {

    private var drawer = Drawer()

    init {
        setOnClickListener(this)
    }

    override fun onClick(v: View) {
        (context as ListHabitsActivity).component.listHabitsMenu.behavior.onCreateHabit(habitGroup!!.id)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawer.draw(canvas)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val height = resources.getDimensionPixelSize(R.dimen.checkmarkHeight)
        val width = resources.getDimensionPixelSize(R.dimen.checkmarkWidth)
        super.onMeasure(
            width.toMeasureSpec(EXACTLY),
            height.toMeasureSpec(EXACTLY)
        )
    }

    private inner class Drawer {
        private val rect = RectF()
        private val highContrastColor = sres.getColor(R.attr.contrast80)

        private val paint = TextPaint().apply {
            typeface = getFontAwesome()
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }

        fun draw(canvas: Canvas) {
            paint.color = highContrastColor
            val id = R.string.fa_plus
            paint.textSize = sp(12.0f)
            paint.strokeWidth = 0f
            paint.style = Paint.Style.FILL

            val label = resources.getString(id)
            val em = paint.measureText("m")

            rect.set(0f, 0f, width.toFloat(), height.toFloat())
            rect.offset(0f, 0.4f * em)
            canvas.drawText(label, rect.centerX(), rect.centerY(), paint)
        }
    }
}
