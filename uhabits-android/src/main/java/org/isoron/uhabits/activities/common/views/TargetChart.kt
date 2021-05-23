/*
 * Copyright (C) 2016-2021 Álinson Santos Xavier <git@axavier.org>
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
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import org.isoron.uhabits.R
import org.isoron.uhabits.activities.habits.list.views.toShortString
import org.isoron.uhabits.utils.InterfaceUtils.dpToPixels
import org.isoron.uhabits.utils.InterfaceUtils.getDimension
import org.isoron.uhabits.utils.StyledResources
import kotlin.math.max
import kotlin.math.min

class TargetChart : View {
    private var paint: Paint? = null
    private var baseSize = 0
    private var primaryColor = 0
    private var mediumContrastTextColor = 0
    private var highContrastReverseTextColor = 0
    private var lowContrastTextColor = 0
    private val rect = RectF()
    private val barRect = RectF()
    private var values = emptyList<Double>()
    private var labels = emptyList<String>()
    private var targets = emptyList<Double>()
    private var maxLabelSize = 0f
    private var tinyTextSize = 0f

    constructor(context: Context?) : super(context) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    fun setColor(color: Int) {
        primaryColor = color
        postInvalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (labels.isEmpty()) return
        maxLabelSize = 0f
        for (label in labels) {
            paint!!.textSize = tinyTextSize
            val len = paint!!.measureText(label)
            maxLabelSize = max(maxLabelSize, len)
        }
        val marginTop = (height - baseSize * labels.size) / 2.0f
        rect[0f, marginTop, width.toFloat()] = marginTop + baseSize
        for (i in labels.indices) {
            drawRow(canvas, i, rect)
            rect.offset(0f, baseSize.toFloat())
        }
    }

    override fun onMeasure(widthSpec: Int, heightSpec: Int) {
        var widthSpec = widthSpec
        var heightSpec = heightSpec
        baseSize = resources.getDimensionPixelSize(R.dimen.baseSize)
        val width = MeasureSpec.getSize(widthSpec)
        var height = labels.size * baseSize
        val params = layoutParams
        if (params != null && params.height == ViewGroup.LayoutParams.MATCH_PARENT) {
            height = MeasureSpec.getSize(heightSpec)
            if (labels.isNotEmpty()) baseSize = height / labels.size
        }
        heightSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY)
        widthSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY)
        setMeasuredDimension(widthSpec, heightSpec)
    }

    private fun drawRow(canvas: Canvas, row: Int, rect: RectF) {
        val padding = dpToPixels(context, 4f)
        val round = dpToPixels(context, 2f)
        val stop = maxLabelSize + padding * 2
        paint!!.color = mediumContrastTextColor

        // Draw label
        paint!!.textSize = tinyTextSize
        paint!!.textAlign = Paint.Align.RIGHT
        var yTextAdjust = (paint!!.descent() + paint!!.ascent()) / 2.0f
        canvas.drawText(
            labels[row],
            rect.left + stop - padding,
            rect.centerY() - yTextAdjust,
            paint!!
        )

        // Draw background box
        paint!!.color = lowContrastTextColor
        barRect[rect.left + stop + padding, rect.top + baseSize * 0.05f, rect.right - padding] =
            rect.bottom - baseSize * 0.05f
        canvas.drawRoundRect(barRect, round, round, paint!!)
        var percentage = if (targets[row] > 0) {
            (values[row] / targets[row]).toFloat()
        } else {
            1.0f
        }
        percentage = min(1.0f, percentage)

        // Draw completed box
        var completedWidth = percentage * barRect.width()
        if (completedWidth > 0 && completedWidth < 2 * round) {
            completedWidth = 2 * round
        }
        val remainingWidth = barRect.width() - completedWidth
        paint!!.color = primaryColor
        barRect[barRect.left, barRect.top, barRect.left + completedWidth] = barRect.bottom
        canvas.drawRoundRect(barRect, round, round, paint!!)

        // Draw values
        paint!!.color = Color.WHITE
        paint!!.textSize = tinyTextSize
        paint!!.textAlign = Paint.Align.CENTER
        yTextAdjust = (paint!!.descent() + paint!!.ascent()) / 2.0f
        val remaining = targets[row] - values[row]
        val completedText = values[row].toShortString()
        val remainingText = remaining.toShortString()
        if (completedWidth > paint!!.measureText(completedText) + 2 * padding) {
            paint!!.color = highContrastReverseTextColor
            canvas.drawText(
                completedText,
                barRect.centerX(),
                barRect.centerY() - yTextAdjust,
                paint!!
            )
        }
        if (remainingWidth > paint!!.measureText(remainingText) + 2 * padding) {
            paint!!.color = mediumContrastTextColor
            barRect[rect.left + stop + padding + completedWidth, barRect.top, rect.right - padding] =
                barRect.bottom
            canvas.drawText(
                remainingText,
                barRect.centerX(),
                barRect.centerY() - yTextAdjust,
                paint!!
            )
        }
    }

    private fun init() {
        paint = Paint()
        paint!!.textAlign = Paint.Align.CENTER
        paint!!.isAntiAlias = true
        val res = StyledResources(context)
        lowContrastTextColor = res.getColor(R.attr.contrast20)
        mediumContrastTextColor = res.getColor(R.attr.contrast60)
        highContrastReverseTextColor = res.getColor(R.attr.contrast0)
        tinyTextSize = getDimension(context, R.dimen.tinyTextSize)
    }

    fun setValues(values: List<Double>) {
        this.values = values
        requestLayout()
    }

    fun setLabels(labels: List<String>) {
        this.labels = labels
        requestLayout()
    }

    fun setTargets(targets: List<Double>) {
        this.targets = targets
        requestLayout()
    }
}
