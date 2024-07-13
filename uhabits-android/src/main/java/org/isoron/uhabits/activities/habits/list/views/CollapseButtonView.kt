package org.isoron.uhabits.activities.habits.list.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.text.TextPaint
import android.view.View
import org.isoron.uhabits.R
import org.isoron.uhabits.activities.habits.list.ListHabitsActivity
import org.isoron.uhabits.core.models.HabitGroup
import org.isoron.uhabits.core.models.ModelObservable
import org.isoron.uhabits.utils.getFontAwesome
import org.isoron.uhabits.utils.sp
import org.isoron.uhabits.utils.sres
import org.isoron.uhabits.utils.toMeasureSpec

class CollapseButtonView(
    context: Context,
    var habitGroup: HabitGroup?
) : View(context),
    View.OnClickListener,
    ModelObservable.Listener {

    private var drawer = Drawer()

    var collapsed = false

    init {
        setOnClickListener(this)
    }

    override fun onClick(v: View) {
        collapsed = !collapsed
        habitGroup!!.collapsed = collapsed
        drawer.rotate()
        invalidate()
        (context as ListHabitsActivity).component.listHabitsMenu.behavior.onPreferencesChanged()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawer.draw(canvas)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val height = resources.getDimensionPixelSize(R.dimen.checkmarkHeight)
        val width = resources.getDimensionPixelSize(R.dimen.checkmarkWidth)
        super.onMeasure(
            width.toMeasureSpec(MeasureSpec.EXACTLY),
            height.toMeasureSpec(MeasureSpec.EXACTLY)
        )
    }

    private inner class Drawer {
        private val rect = RectF()
        private val highContrastColor = sres.getColor(R.attr.contrast100)

        private var rotationAngle = 0f
        private var offset_y = 0.4f
        private var offset_x = 0f
        private val paint = TextPaint().apply {
            typeface = getFontAwesome()
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }

        fun rotate() {
            if (rotationAngle == 0f) {
                rotationAngle = 90f
                offset_y = 0f
                offset_x = -0.4f
            } else {
                rotationAngle = 0f
                offset_y = 0.4f
                offset_x = 0f
            }
        }

        fun draw(canvas: Canvas) {
            paint.color = highContrastColor
            val id = R.string.fa_angle_down
            paint.textSize = sp(12.0f)
            paint.strokeWidth = 0f
            paint.style = Paint.Style.FILL

            val label = resources.getString(id)
            val em = paint.measureText("m")

            rect.set(0f, 0f, width.toFloat(), height.toFloat())
            rect.offset(offset_x * em, offset_y * em)

            canvas.save() // Save the current state of the canvas
            canvas.rotate(rotationAngle, rect.centerX(), rect.centerY()) // Rotate the canvas
            canvas.drawText(label, rect.centerX(), rect.centerY(), paint)
            canvas.restore()
        }
    }

    override fun onModelChange() {}
}
