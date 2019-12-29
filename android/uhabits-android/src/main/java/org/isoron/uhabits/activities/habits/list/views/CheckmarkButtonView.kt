/*
 * Copyright (C) 2016 Álinson Santos Xavier <isoron@gmail.com>
 *
 * This file is part of Loop Habit Tracker.
 *
 * Loop Habit Tracker is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Loop Habit Tracker is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.isoron.uhabits.activities.habits.list.views

import android.content.*
import android.graphics.*
import android.text.*
import android.view.*
import android.view.View.MeasureSpec.*
import com.google.auto.factory.*
import org.isoron.androidbase.activities.*
import org.isoron.uhabits.*
import org.isoron.uhabits.core.models.Checkmark.*
import org.isoron.uhabits.core.preferences.*
import org.isoron.uhabits.utils.*

@AutoFactory
class CheckmarkButtonView(
        @Provided @ActivityContext context: Context,
        @Provided val preferences: Preferences
) : View(context),
    View.OnClickListener,
    View.OnLongClickListener {

    var color: Int = Color.BLACK
        set(value) {
            field = value
            invalidate()
        }

    var value: Int = 0
        set(value) {
            field = value
            invalidate()
        }

    var onToggle: () -> Unit = {}
    private var drawer = Drawer()

    init {
        isFocusable = false
        setOnClickListener(this)
        setOnLongClickListener(this)
    }

    fun performToggle() {
        onToggle()
        value = when (value) {
            CHECKED_EXPLICITLY -> UNCHECKED
            else -> CHECKED_EXPLICITLY
        }
        performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        invalidate()
    }

    override fun onClick(v: View) {
        if (preferences.isShortToggleEnabled) performToggle()
        else showMessage(R.string.long_press_to_toggle)
    }

    override fun onLongClick(v: View): Boolean {
        performToggle()
        return true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawer.draw(canvas)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val height = resources.getDimensionPixelSize(R.dimen.checkmarkHeight)
        val width = resources.getDimensionPixelSize(R.dimen.checkmarkWidth)
        super.onMeasure(width.toMeasureSpec(EXACTLY),
                        height.toMeasureSpec(EXACTLY))
    }

    private inner class Drawer {
        private val rect = RectF()
        private val lowContrastColor = sres.getColor(R.attr.lowContrastTextColor)

        private val paint = TextPaint().apply {
            typeface = getFontAwesome()
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
            textSize = dim(R.dimen.smallTextSize)
        }

        fun draw(canvas: Canvas) {
            paint.color = when (value) {
                CHECKED_EXPLICITLY -> color
                else -> lowContrastColor
            }
            val id = when (value) {
                UNCHECKED -> R.string.fa_times
                else -> R.string.fa_check
            }
            val label = resources.getString(id)
            val em = paint.measureText("m")

            rect.set(0f, 0f, width.toFloat(), height.toFloat())
            rect.offset(0f, 0.4f * em)
            canvas.drawText(label, rect.centerX(), rect.centerY(), paint)
        }
    }
}
