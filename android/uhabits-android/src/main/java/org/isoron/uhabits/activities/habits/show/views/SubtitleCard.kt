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
import org.isoron.uhabits.core.models.Habit
import org.isoron.uhabits.core.models.PaletteColor
import org.isoron.uhabits.databinding.ShowHabitSubtitleBinding
import org.isoron.uhabits.utils.InterfaceUtils
import org.isoron.uhabits.utils.formatTime
import org.isoron.uhabits.utils.toThemedAndroidColor
import java.util.Locale

data class SubtitleCardViewModel(
    val color: PaletteColor,
    val frequencyText: String,
    val isNumerical: Boolean,
    val question: String,
    val reminderText: String,
    val targetText: String,
)

class SubtitleCardView(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

    private val binding = ShowHabitSubtitleBinding.inflate(LayoutInflater.from(context), this)

    init {
        val fontAwesome = InterfaceUtils.getFontAwesome(context)
        binding.targetIcon.typeface = fontAwesome
        binding.frequencyIcon.typeface = fontAwesome
        binding.reminderIcon.typeface = fontAwesome
    }

    fun update(data: SubtitleCardViewModel) {
        val color = data.color.toThemedAndroidColor(context)
        binding.frequencyLabel.text = data.frequencyText
        binding.questionLabel.setTextColor(color)
        binding.questionLabel.text = data.question
        binding.reminderLabel.text = data.reminderText
        binding.targetText.text = data.targetText

        binding.questionLabel.visibility = View.VISIBLE
        binding.targetIcon.visibility = View.VISIBLE
        binding.targetText.visibility = View.VISIBLE
        if (!data.isNumerical) {
            binding.targetIcon.visibility = View.GONE
            binding.targetText.visibility = View.GONE
        }
        if (data.question.isEmpty()) {
            binding.questionLabel.visibility = View.GONE
        }

        postInvalidate()
    }
}

class SubtitleCardPresenter(
    val habit: Habit,
    val context: Context,
) {
    val resources: Resources = context.resources

    fun present(): SubtitleCardViewModel {
        val reminderText = if (habit.hasReminder()) {
            formatTime(context, habit.reminder!!.hour, habit.reminder!!.minute)!!
        } else {
            resources.getString(R.string.reminder_off)
        }
        return SubtitleCardViewModel(
            color = habit.color,
            frequencyText = habit.frequency.format(),
            isNumerical = habit.isNumerical,
            question = habit.question,
            reminderText = reminderText,
            targetText = "${habit.targetValue.toShortString()} ${habit.unit}",
        )
    }

    @SuppressLint("StringFormatMatches")
    private fun Frequency.format(): String {
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
