/*
 * Copyright (C) 2026 Christoph Schmidtmeier
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

package org.isoron.uhabits.activities.common.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import org.isoron.platform.gui.FontAwesome
import org.isoron.uhabits.R
import org.isoron.uhabits.utils.ColorUtils.setAlpha
import org.isoron.uhabits.utils.dp
import org.isoron.uhabits.utils.getFontAwesome
import org.isoron.uhabits.utils.sp
import org.isoron.uhabits.utils.sres
import kotlin.math.max

class StreakFlameView : View {
    var streak: Int = 0
        set(value) {
            if (field != value) {
                field = value
                requestLayout()
                invalidate()
            }
        }

    var color: Int = Color.BLACK
        set(value) {
            if (field != value) {
                field = value
                invalidate()
            }
        }

    var isArchived: Boolean = false
        set(value) {
            if (field != value) {
                field = value
                invalidate()
            }
        }

    private lateinit var pFlame: TextPaint
    private lateinit var pText: TextPaint

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init()
    }

    private fun init() {
        pFlame = TextPaint().apply {
            isAntiAlias = true
            typeface = getFontAwesome()
            textAlign = Paint.Align.LEFT
            textSize = sp(13f)
        }

        pText = TextPaint().apply {
            isAntiAlias = true
            typeface = Typeface.DEFAULT_BOLD
            textAlign = Paint.Align.LEFT
            textSize = sp(12f)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val streakStr = streak.toString()
        val flameWidth = pFlame.measureText(FontAwesome.FIRE)
        val textWidth = pText.measureText(streakStr)
        val spacing = dp(3f)
        val horizontalPadding = dp(4f)

        val desiredWidth = (flameWidth + spacing + textWidth + horizontalPadding * 2).toInt()

        val flameMetrics = pFlame.fontMetrics
        val textMetrics = pText.fontMetrics
        val flameHeight = flameMetrics.descent - flameMetrics.ascent
        val textHeight = textMetrics.descent - textMetrics.ascent
        val desiredHeight = max(flameHeight, textHeight).toInt()

        val width = resolveSize(desiredWidth, widthMeasureSpec)
        val height = resolveSize(desiredHeight, heightMeasureSpec)
        setMeasuredDimension(width, height)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val displayColor = when {
            isArchived -> sres.getColor(R.attr.contrast60)
            streak > 0 -> color
            else -> setAlpha(color, 0.4f)
        }

        pFlame.color = displayColor
        pText.color = displayColor

        val streakStr = streak.toString()
        val flameWidth = pFlame.measureText(FontAwesome.FIRE)
        val textWidth = pText.measureText(streakStr)
        val spacing = dp(3f)
        val totalContentWidth = flameWidth + spacing + textWidth

        val startX = (width - totalContentWidth) / 2f

        val centerY = height / 2f
        val flameMetrics = pFlame.fontMetrics
        val flameY = centerY - (flameMetrics.ascent + flameMetrics.descent) / 2f

        val textMetrics = pText.fontMetrics
        val textY = centerY - (textMetrics.ascent + textMetrics.descent) / 2f

        canvas.drawText(FontAwesome.FIRE, startX, flameY, pFlame)
        canvas.drawText(streakStr, startX + flameWidth + spacing, textY, pText)
    }
}
