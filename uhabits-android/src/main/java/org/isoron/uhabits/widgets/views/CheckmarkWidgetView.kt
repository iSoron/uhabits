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
import android.graphics.drawable.GradientDrawable
import android.text.format.DateFormat
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
import org.isoron.uhabits.utils.InterfaceUtils.dpToPixels
import org.isoron.uhabits.utils.InterfaceUtils.getDimension
import org.isoron.uhabits.utils.PaletteUtils.getAndroidTestColor
import org.isoron.uhabits.utils.StyledResources
import java.util.Date
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

class CheckmarkWidgetView : HabitWidgetView {
    var activeColor: Int = 0

    var percentage = 0f
    var name: String? = null

    var entryValue = 0
    var entryState = 0
    var isNumerical = false

    var streakDays: Int = 0
    var weeklySuccess: BooleanArray = BooleanArray(7)
    var nextReminderTimeUtcMillis: Long? = null

    private lateinit var ring: RingView
    private lateinit var label: TextView
    private lateinit var streakBadge: TextView
    private lateinit var weeklyHeatmap: WeeklyHeatmapView
    private lateinit var reminderLabel: TextView

    private var preferences: Preferences? = null

    private val badgeBackground = GradientDrawable().apply {
        shape = GradientDrawable.RECTANGLE
    }

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
        val isActive = when (entryState) {
            YES_MANUAL, SKIP, YES_AUTO -> true
            else -> false
        }

        setShadowAlpha(0x4f)
        when (entryState) {
            YES_MANUAL, SKIP, YES_AUTO -> {
                bgColor = activeColor
                fgColor = res.getColor(R.attr.contrast0)
                backgroundPaint!!.color = bgColor
                frame!!.setBackgroundDrawable(background)
            }
            NO, UNKNOWN -> {
                bgColor = res.getColor(R.attr.cardBgColor)
                fgColor = res.getColor(R.attr.contrast60)
            }
            else -> {
                bgColor = res.getColor(R.attr.cardBgColor)
                fgColor = res.getColor(R.attr.contrast60)
            }
        }

        ring.setPercentage(percentage)
        ring.setColor(fgColor)
        ring.setBackgroundColor(bgColor)
        ring.setText(text)
        ring.setIsStrokedTextEnabled(strokedTextEnabled)

        label.text = name
        label.setTextColor(fgColor)

        // Streak badge
        streakBadge.visibility = View.VISIBLE
        streakBadge.text = max(0, streakDays).toString()

        val badgeBg = if (isActive) fgColor else activeColor
        val badgeFg = if (isActive) bgColor else res.getColor(R.attr.contrast0)

        badgeBackground.setColor(withAlpha(badgeBg, if (isActive) 220 else 235))
        badgeBackground.cornerRadius = dpToPixels(context, 999f)
        badgeBackground.setStroke(
            dpToPixels(context, 1f).roundToInt(),
            withAlpha(badgeFg, 70)
        )

        streakBadge.background = badgeBackground
        streakBadge.setTextColor(badgeFg)

        // Weekly heatmap
        weeklyHeatmap.cells = weeklySuccess
        weeklyHeatmap.activeColor = if (isActive) fgColor else activeColor
        weeklyHeatmap.inactiveColor = if (isActive) withAlpha(fgColor, 60) else res.getColor(R.attr.contrast20)
        weeklyHeatmap.highlightIndex = 6

        val showHeatmap = shouldShowHeatmap(measuredWidth, measuredHeight)
        weeklyHeatmap.visibility = if (showHeatmap) View.VISIBLE else View.GONE

        // Next reminder label
        val reminderTime = nextReminderTimeUtcMillis
        if (reminderTime != null && shouldShowReminder(measuredWidth, measuredHeight)) {
            reminderLabel.visibility = View.VISIBLE
            val timeStr = DateFormat.getTimeFormat(context).format(Date(reminderTime))
            reminderLabel.text = resources.getString(R.string.widget_next_reminder_at, timeStr)
            reminderLabel.setTextColor(fgColor)
        } else {
            reminderLabel.visibility = View.GONE
        }

        requestLayout()
        postInvalidate()
    }

    private val strokedTextEnabled: Boolean
        get() = if (isNumerical) {
            false
        } else {
            when (entryState) {
                YES_AUTO -> true
                else -> false
            }
        }

    private val text: String
        get() = if (isNumerical) {
            (max(0, entryValue) / 1000.0).toShortString()
        } else {
            when (entryState) {
                YES_MANUAL, YES_AUTO -> resources.getString(R.string.fa_check)
                SKIP -> resources.getString(R.string.fa_skipped)
                UNKNOWN -> if (preferences?.areQuestionMarksEnabled == true) {
                    resources.getString(R.string.fa_question)
                } else {
                    resources.getString(R.string.fa_times)
                }
                NO -> resources.getString(R.string.fa_times)
                else -> resources.getString(R.string.fa_times)
            }
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

        val textSize = min(0.175f * width, getDimension(context, R.dimen.smallTextSize))
        label.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize)
        reminderLabel.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize * 0.70f)
        streakBadge.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize * 0.75f)

        if (isNumerical) {
            ring.setTextSize(textSize * 0.9f)
        } else {
            ring.setTextSize(textSize)
        }

        ring.setThickness(0.03f * width)

        // Scale the heatmap height a bit with the widget size.
        weeklyHeatmap.layoutParams = weeklyHeatmap.layoutParams.apply {
            height = max(
                dpToPixels(context, 12f).roundToInt(),
                (0.08f * width).roundToInt()
            )
        }

        super.onMeasure(
            MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY)
        )
    }

    private fun init() {
        val appComponent: HabitsApplicationComponent =
            (context.applicationContext as HabitsApplication).component

        preferences = appComponent.preferences

        ring = findViewById<View>(R.id.scoreRing) as RingView
        label = findViewById<View>(R.id.label) as TextView
        streakBadge = findViewById<View>(R.id.streakBadge) as TextView
        weeklyHeatmap = findViewById<View>(R.id.weeklyHeatmap) as WeeklyHeatmapView
        reminderLabel = findViewById<View>(R.id.reminderLabel) as TextView

        ring.setIsTransparencyEnabled(true)

        if (isInEditMode) {
            percentage = 0.75f
            name = "Wake up early"
            activeColor = getAndroidTestColor(6)
            entryValue = 0
            entryState = YES_MANUAL
            streakDays = 12
            weeklySuccess = booleanArrayOf(true, true, false, true, true, true, false)
            nextReminderTimeUtcMillis = System.currentTimeMillis() + 2 * 60 * 60 * 1000
            refresh()
        }
    }

    private fun shouldShowHeatmap(width: Int, height: Int): Boolean {
        if (width <= 0 || height <= 0) return false
        val minDim = min(width, height).toFloat()
        val bigEnough = minDim >= dpToPixels(context, 90f)
        val tallEnough = height.toFloat() / width.toFloat() >= 1.05f
        return bigEnough || tallEnough
    }

    private fun shouldShowReminder(width: Int, height: Int): Boolean {
        if (width <= 0 || height <= 0) return false
        val minDim = min(width, height).toFloat()
        val bigEnough = minDim >= dpToPixels(context, 100f)
        val tallEnough = height.toFloat() / width.toFloat() >= 1.15f
        return bigEnough || tallEnough
    }

    private fun withAlpha(color: Int, alpha: Int): Int {
        val a = alpha.coerceIn(0, 255)
        return (color and 0x00ffffff) or (a shl 24)
    }
}
