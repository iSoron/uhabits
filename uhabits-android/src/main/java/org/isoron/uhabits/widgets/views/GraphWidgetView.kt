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
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import org.isoron.uhabits.R
import org.isoron.uhabits.utils.InterfaceUtils.dpToPixels
import org.isoron.uhabits.utils.StyledResources
import java.util.Date
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

class GraphWidgetView(context: Context?, val dataView: View) : HabitWidgetView(context) {

    private lateinit var title: TextView
    private lateinit var dataContainer: ViewGroup

    private lateinit var streakBadge: TextView
    private lateinit var weeklyHeatmap: WeeklyHeatmapView
    private lateinit var reminderLabel: TextView

    private val badgeBackground = GradientDrawable().apply {
        shape = GradientDrawable.RECTANGLE
    }

    override val innerLayoutId: Int
        get() = R.layout.widget_graph

    fun setTitle(text: String?) {
        title.text = text
    }

    fun setExtras(
        accentColor: Int,
        streakDays: Int,
        weeklySuccess: BooleanArray,
        nextReminderTimeUtcMillis: Long?
    ) {
        val res = StyledResources(context)

        streakBadge.visibility = View.VISIBLE
        streakBadge.text = max(0, streakDays).toString()

        badgeBackground.setColor(withAlpha(accentColor, 235))
        badgeBackground.cornerRadius = dpToPixels(context, 999f)
        badgeBackground.setStroke(
            dpToPixels(context, 1f).roundToInt(),
            withAlpha(res.getColor(R.attr.contrast0), 70)
        )

        streakBadge.background = badgeBackground
        streakBadge.setTextColor(res.getColor(R.attr.contrast0))

        weeklyHeatmap.cells = weeklySuccess
        weeklyHeatmap.activeColor = accentColor
        weeklyHeatmap.inactiveColor = res.getColor(R.attr.contrast20)
        weeklyHeatmap.highlightIndex = 6

        weeklyHeatmap.visibility = if (shouldShowHeatmap(measuredWidth, measuredHeight)) {
            View.VISIBLE
        } else {
            View.GONE
        }

        if (nextReminderTimeUtcMillis != null && shouldShowReminder(measuredWidth, measuredHeight)) {
            reminderLabel.visibility = View.VISIBLE
            val timeStr = DateFormat.getTimeFormat(context).format(Date(nextReminderTimeUtcMillis))
            reminderLabel.text = resources.getString(R.string.widget_next_reminder_at, timeStr)
            reminderLabel.setTextColor(res.getColor(R.attr.contrast60))
        } else {
            reminderLabel.visibility = View.GONE
        }
    }

    private fun init() {
        val params = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        dataView.layoutParams = params

        dataContainer = findViewById<View>(R.id.dataContainer) as ViewGroup
        dataContainer.addView(dataView)

        title = findViewById<View>(R.id.title) as TextView
        title.visibility = VISIBLE

        streakBadge = findViewById<View>(R.id.streakBadge) as TextView
        weeklyHeatmap = findViewById<View>(R.id.weeklyHeatmap) as WeeklyHeatmapView
        reminderLabel = findViewById<View>(R.id.reminderLabel) as TextView
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

    init {
        init()
    }
}
