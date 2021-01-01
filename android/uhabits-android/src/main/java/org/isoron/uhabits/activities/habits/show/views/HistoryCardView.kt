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
package org.isoron.uhabits.activities.habits.show.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import org.isoron.platform.time.JavaLocalDateFormatter
import org.isoron.uhabits.core.ui.screens.habits.show.views.HistoryCardViewModel
import org.isoron.uhabits.core.ui.views.HistoryChart
import org.isoron.uhabits.databinding.ShowHabitHistoryBinding
import org.isoron.uhabits.utils.toThemedAndroidColor
import java.util.Locale

class HistoryCardView(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

    private var binding = ShowHabitHistoryBinding.inflate(LayoutInflater.from(context), this)

    var onClickEditButton: () -> Unit = {}

    init {
        binding.edit.setOnClickListener { onClickEditButton() }
    }

    fun update(data: HistoryCardViewModel) {

        val androidColor = data.color.toThemedAndroidColor(context)
        binding.title.setTextColor(androidColor)
        binding.chart.view = HistoryChart(
            today = data.today,
            paletteColor = data.color,
            theme = data.theme,
            dateFormatter = JavaLocalDateFormatter(Locale.getDefault()),
            series = data.series,
            firstWeekday = data.firstWeekday,
        )

        // binding.historyChart.setSkipEnabled(data.isSkipEnabled)
        // if (data.isNumerical) {
        //     binding.historyChart.setNumerical(true)
        // }
    }
}
