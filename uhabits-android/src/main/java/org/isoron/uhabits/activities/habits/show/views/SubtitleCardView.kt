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
package org.isoron.uhabits.activities.habits.show.views

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import org.isoron.platform.gui.toInt
import org.isoron.uhabits.R
import org.isoron.uhabits.activities.habits.edit.formatFrequency
import org.isoron.uhabits.activities.habits.list.views.toShortString
import org.isoron.uhabits.core.models.NumericalHabitType
import org.isoron.uhabits.core.ui.screens.habits.show.views.SubtitleCardState
import org.isoron.uhabits.databinding.ShowHabitSubtitleBinding
import org.isoron.uhabits.utils.InterfaceUtils
import org.isoron.uhabits.utils.formatTime

class SubtitleCardView(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

    private val binding = ShowHabitSubtitleBinding.inflate(LayoutInflater.from(context), this)

    init {
        val fontAwesome = InterfaceUtils.getFontAwesome(context)
        binding.targetIcon.typeface = fontAwesome
        binding.frequencyIcon.typeface = fontAwesome
        binding.reminderIcon.typeface = fontAwesome
    }

    @SuppressLint("SetTextI18n")
    fun setState(state: SubtitleCardState) {
        val color = state.theme.color(state.color).toInt()
        val reminder = state.reminder
        binding.frequencyLabel.text = formatFrequency(
            state.frequency.numerator,
            state.frequency.denominator,
            resources,
        )
        binding.questionLabel.setTextColor(color)
        binding.questionLabel.text = state.question
        binding.reminderLabel.text = if (reminder != null) {
            formatTime(context, reminder.hour, reminder.minute)
        } else {
            resources.getString(R.string.reminder_off)
        }
        binding.targetText.text = "${state.targetValue.toShortString()} ${state.unit}"

        binding.questionLabel.visibility = View.VISIBLE
        binding.targetIcon.visibility = View.VISIBLE
        binding.targetText.visibility = View.VISIBLE
        if (state.isNumerical) {
            binding.targetIcon.text = when (state.targetType) {
                NumericalHabitType.AT_LEAST -> resources.getString(R.string.fa_arrow_circle_up)
                else -> resources.getString(R.string.fa_arrow_circle_down)
            }
        } else {
            binding.targetIcon.visibility = View.GONE
            binding.targetText.visibility = View.GONE
        }
        if (state.question.isEmpty()) {
            binding.questionLabel.visibility = View.GONE
        }

        postInvalidate()
    }
}
