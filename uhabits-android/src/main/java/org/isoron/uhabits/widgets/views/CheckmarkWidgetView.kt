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
package org.isoron.uhabits.widgets.views

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.widget.TextView
import org.isoron.uhabits.HabitsApplication
import org.isoron.uhabits.R
import org.isoron.uhabits.activities.common.views.RingView
import org.isoron.uhabits.activities.habits.list.views.toShortString
import org.isoron.uhabits.core.models.Entry.Companion.NO
import org.isoron.uhabits.core.models.Entry.Companion.SKIP
import org.isoron.uhabits.core.models.Entry.Companion.UNKNOWN
import org.isoron.uhabits.core.models.Entry.Companion.YES_AUTO
import org.isoron.uhabits.core.models.Entry.Companion.YES_MANUAL
import org.isoron.uhabits.core.preferences.Preferences
import org.isoron.uhabits.inject.HabitsApplicationComponent
import org.isoron.uhabits.utils.InterfaceUtils.getDimension
import org.isoron.uhabits.utils.PaletteUtils.getAndroidTestColor
import org.isoron.uhabits.utils.StyledResources
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

class CheckmarkWidgetView : HabitWidgetView {
    var activeColor: Int = 0

    var percentage = 0f
    var name: String? = null
    private lateinit var ring: RingView
    private lateinit var label: TextView
    var entryValue = 0
    var entryState = 0
    var isNumerical = false
    private var preferences: Preferences? = null

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
        when (entryState) {
            YES_MANUAL, SKIP -> {
                bgColor = activeColor
                fgColor = res.getColor(R.attr.contrast0)
                setShadowAlpha(0x4f)
                backgroundPaint!!.color = bgColor
                frame!!.setBackgroundDrawable(background)
            }
            YES_AUTO, NO, UNKNOWN -> {
                bgColor = res.getColor(R.attr.cardBgColor)
                fgColor = res.getColor(R.attr.contrast60)
                setShadowAlpha(0x00)
            }
            else -> {
                bgColor = res.getColor(R.attr.cardBgColor)
                fgColor = res.getColor(R.attr.contrast60)
                setShadowAlpha(0x00)
            }
        }
        ring.setPercentage(percentage)
        ring.setColor(fgColor)
        ring.setBackgroundColor(bgColor)
        ring.setText(text)
        label.text = name
        label.setTextColor(fgColor)
        requestLayout()
        postInvalidate()
    }

    private val text: String
        get() = if (isNumerical) {
            (max(0, entryValue) / 1000.0).toShortString()
        } else when (entryState) {
            YES_MANUAL, YES_AUTO -> resources.getString(R.string.fa_check)
            SKIP -> resources.getString(R.string.fa_skipped)
            UNKNOWN -> {
                run {
                    if (preferences!!.areQuestionMarksEnabled) {
                        return resources.getString(R.string.fa_question)
                    } else {
                        resources.getString(R.string.fa_times)
                    }
                }
                resources.getString(R.string.fa_times)
            }
            NO -> resources.getString(R.string.fa_times)
            else -> resources.getString(R.string.fa_times)
        }

    override val innerLayoutId: Int
        get() = R.layout.widget_checkmark

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var width = MeasureSpec.getSize(widthMeasureSpec)
        var height = MeasureSpec.getSize(heightMeasureSpec)
        if (height >= width) {
            height = min(height, (width * 1.5).roundToInt())
        } else {
            width = min(width, height)
        }
        val textSize = min(0.2f * width, getDimension(context, R.dimen.smallerTextSize))
        label.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize)
        if (isNumerical) {
            ring.setTextSize(textSize * 0.9f)
        } else {
            ring.setTextSize(textSize)
        }
        ring.setThickness(0.03f * width)
        super.onMeasure(
            MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY)
        )
    }

    private fun init() {
        val appComponent: HabitsApplicationComponent = (context.applicationContext as HabitsApplication).component
        preferences = appComponent.preferences
        ring = findViewById<View>(R.id.scoreRing) as RingView
        label = findViewById<View>(R.id.label) as TextView
        ring.setIsTransparencyEnabled(true)
        if (isInEditMode) {
            percentage = 0.75f
            name = "Wake up early"
            activeColor = getAndroidTestColor(6)
            entryValue = YES_MANUAL
            refresh()
        }
    }
}
