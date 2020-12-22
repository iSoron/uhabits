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

import android.content.*
import android.util.*
import android.view.*
import android.widget.*
import org.isoron.uhabits.activities.habits.show.*
import org.isoron.uhabits.databinding.*
import org.isoron.uhabits.utils.*

class TargetCard(
        context: Context,
        attrs: AttributeSet,
) : LinearLayout(context, attrs), ShowHabitPresenter.Listener {

    private val binding = ShowHabitTargetBinding.inflate(LayoutInflater.from(context), this)
    lateinit var presenter: ShowHabitPresenter

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        presenter.addListener(this)
        presenter.requestData(this)
    }

    override fun onDetachedFromWindow() {
        presenter.removeListener(this)
        super.onDetachedFromWindow()
    }

    override fun onData(data: ShowHabitViewModel) {
        val androidColor = data.color.toThemedAndroidColor(context)
        binding.targetChart.setValues(data.targetCompleted)
        binding.targetChart.setTargets(data.targetTotal)
        binding.targetChart.setLabels(data.targetLabels)
        binding.title.setTextColor(androidColor)
        binding.targetChart.setColor(androidColor)
    }
}