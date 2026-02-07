/*
 * Copyright (C) 2016-2025 Álinson Santos Xavier <git@axavier.org>
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
package org.isoron.uhabits.widgets.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import kotlin.math.max
import kotlin.math.min

class WeeklyHeatmapView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    var cells: BooleanArray = BooleanArray(7)
        set(value) {
            field = value.copyOf(7)
            invalidate()
        }

    var activeColor: Int = 0
        set(value) {
            field = value
            invalidate()
        }

    var inactiveColor: Int = 0
        set(value) {
            field = value
            invalidate()
        }

    /**
     * Index of the cell to highlight (0..6). Set to a value outside the range to disable.
     */
    var highlightIndex: Int = 6
        set(value) {
            field = value
            invalidate()
        }

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
    }

    private val rect = RectF()

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val w = width.toFloat()
        val h = height.toFloat()
        if (w <= 0f || h <= 0f) return

        val minDim = min(w, h)
        val targetGap = minDim * 0.10f
        val availableW = w - targetGap * 6f
        val cellSize = min(h, if (availableW > 0f) availableW / 7f else w / 7f)
        val gap = max(0f, if (w > cellSize * 7f) (w - cellSize * 7f) / 6f else 0f)
        val y = (h - cellSize) / 2f
        val radius = cellSize * 0.22f

        strokePaint.strokeWidth = max(1f, cellSize * 0.12f)

        var x = 0f
        for (i in 0 until 7) {
            rect.set(x, y, x + cellSize, y + cellSize)
            fillPaint.color = if (cells[i]) activeColor else inactiveColor
            canvas.drawRoundRect(rect, radius, radius, fillPaint)

            if (i == highlightIndex) {
                strokePaint.color = activeColor
                canvas.drawRoundRect(rect, radius, radius, strokePaint)
            }

            x += cellSize + gap
        }
    }
}
