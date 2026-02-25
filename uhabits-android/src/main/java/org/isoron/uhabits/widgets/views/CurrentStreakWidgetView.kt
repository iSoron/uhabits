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
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.widget.TextView
import org.isoron.uhabits.R
import org.isoron.uhabits.utils.StyledResources
import kotlin.math.min
import kotlin.math.roundToInt

class CurrentStreakWidgetView : HabitWidgetView {
    var activeColor: Int = 0
    var currentStreak = 0
    var name: String? = null
    var isCompleted = false

    private lateinit var currentStreakText: TextView
    private lateinit var label: TextView

    constructor(context: Context?) : super(context) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    fun refresh() {
        if (backgroundPaint == null || frame == null) return
        val res = StyledResources(context)
        val bgColor: Int
        val fgColor: Int
        setShadowAlpha(0x4f)

        if (isCompleted) {
            bgColor = activeColor
            fgColor = res.getColor(R.attr.contrast0)
            backgroundPaint!!.color = bgColor
            frame!!.setBackgroundDrawable(background)
        } else {
            bgColor = res.getColor(R.attr.cardBgColor)
            fgColor = res.getColor(R.attr.contrast60)
        }

        currentStreakText.text = currentStreak.toString()
        currentStreakText.setTextColor(fgColor)

        label.text = name
        label.setTextColor(fgColor)

        requestLayout()
        postInvalidate()
    }

    override val innerLayoutId: Int
        get() = R.layout.widget_current_streak

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var width = MeasureSpec.getSize(widthMeasureSpec)
        var height = MeasureSpec.getSize(heightMeasureSpec)
        if (height >= width) {
            height = min(height, (width * 1.5).roundToInt())
        } else {
            width = min(width, height)
        }
        val labelTextSize = min(0.175f * width, org.isoron.uhabits.utils.InterfaceUtils.getDimension(context, R.dimen.smallTextSize))
        label.setTextSize(TypedValue.COMPLEX_UNIT_PX, labelTextSize)

        val streakTextSize = 0.4f * width
        currentStreakText.setTextSize(TypedValue.COMPLEX_UNIT_PX, streakTextSize)

        super.onMeasure(
            MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY)
        )
    }

    private fun init() {
        currentStreakText = findViewById<View>(R.id.current_streak_text) as TextView
        label = findViewById<View>(R.id.label) as TextView
    }
}
