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
import android.content.res.Resources
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import org.isoron.uhabits.R
import org.isoron.uhabits.activities.habits.list.views.toShortString
import org.isoron.uhabits.core.models.Frequency
import org.isoron.uhabits.core.ui.screens.habits.show.views.SubtitleCardState
import org.isoron.uhabits.databinding.ShowHabitSubtitleBinding
import org.isoron.uhabits.utils.InterfaceUtils
import org.isoron.uhabits.utils.formatTime
import org.isoron.uhabits.utils.toThemedAndroidColor
import java.util.Locale

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
        val color = state.color.toThemedAndroidColor(context)
        val reminder = state.reminder
        binding.frequencyLabel.text = state.frequency.format(resources)
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
        if (!state.isNumerical) {
            binding.targetIcon.visibility = View.GONE
            binding.targetText.visibility = View.GONE
        }
        if (state.question.isEmpty()) {
            binding.questionLabel.visibility = View.GONE
        }

        postInvalidate()
    }

    @SuppressLint("StringFormatMatches")
    private fun Frequency.format(resources: Resources): String {
        val num = this.numerator
        val den = this.denominator
        if (num == den) {
            return resources.getString(R.string.every_day)
        }
        if (den == 7) {
            return resources.getString(R.string.x_times_per_week, num)
        }
        if (den == 30 || den == 31) {
            return resources.getString(R.string.x_times_per_month, num)
        }
        if (num == 1) {
            if (den == 7) {
                return resources.getString(R.string.every_week)
            }
            if (den % 7 == 0) {
                return resources.getString(R.string.every_x_weeks, den / 7)
            }
            if (den == 30 || den == 31) {
                return resources.getString(R.string.every_month)
            }
            return resources.getString(R.string.every_x_days, den)
        }
        return String.format(
            Locale.US,
            "%d %s %d %s",
            num,
            resources.getString(R.string.times_every),
            den,
            resources.getString(R.string.days),
        )
    }
}
