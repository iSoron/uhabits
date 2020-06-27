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

import android.annotation.*
import android.content.*
import android.util.*
import android.view.*
import org.isoron.androidbase.utils.*
import org.isoron.uhabits.*
import org.isoron.uhabits.activities.habits.list.views.*
import org.isoron.uhabits.core.models.*
import org.isoron.uhabits.core.tasks.*
import org.isoron.uhabits.databinding.*
import org.isoron.uhabits.utils.*
import org.isoron.uhabits.utils.PaletteUtils.getAndroidTestColor
import org.isoron.uhabits.utils.PaletteUtils.getColor
import java.util.*

class SubtitleCard(context: Context?, attrs: AttributeSet?) : HabitCard(context, attrs) {

    init {
        init()
    }

    private lateinit var binding: ShowHabitSubtitleBinding

    public override fun refreshData() {
        val habit = habit
        val color = getColor(context, habit.color)
        if (habit.isNumerical) {
            binding.targetText.text = "${habit.targetValue.toShortString()} ${habit.unit}"
        } else {
            binding.targetIcon.visibility = View.GONE
            binding.targetText.visibility = View.GONE
        }
        binding.reminderLabel.text = resources.getString(R.string.reminder_off)
        binding.questionLabel.visibility = View.VISIBLE
        binding.questionLabel.setTextColor(color)
        binding.questionLabel.text = habit.question
        binding.frequencyLabel.text = toText(habit.frequency)
        if (habit.hasReminder()) updateReminderText(habit.reminder)
        if (habit.question.isEmpty()) binding.questionLabel.visibility = View.GONE
        invalidate()
    }

    private fun init() {
        val fontAwesome = InterfaceUtils.getFontAwesome(context)
        binding = ShowHabitSubtitleBinding.inflate(LayoutInflater.from(context), this)
        binding.targetIcon.typeface = fontAwesome
        binding.frequencyIcon.typeface = fontAwesome
        binding.reminderIcon.typeface = fontAwesome
        if (isInEditMode) initEditMode()
    }

    @SuppressLint("SetTextI18n")
    private fun initEditMode() {
        binding.questionLabel.setTextColor(getAndroidTestColor(1))
        binding.questionLabel.text = "Have you meditated today?"
        binding.reminderLabel.text = "08:00"
    }

    private fun toText(freq: Frequency): String {
        val resources = resources
        val num = freq.numerator
        val den = freq.denominator
        if (num == den) return resources.getString(R.string.every_day)
        if (num == 1) {
            if (den == 7) return resources.getString(R.string.every_week)
            if (den % 7 == 0) return resources.getString(R.string.every_x_weeks, den / 7)
            return if (den >= 30) resources.getString(R.string.every_month) else resources.getString(R.string.every_x_days, den)
        }
        val times_every = resources.getString(R.string.times_every)
        return String.format(Locale.US, "%d %s %d %s", num, times_every, den,
                resources.getString(R.string.days))
    }

    private fun updateReminderText(reminder: Reminder) {
        binding.reminderLabel.text = AndroidDateUtils.formatTime(context, reminder.hour,
                reminder.minute)
    }

    override fun createRefreshTask(): Task {
        // Never called
        throw IllegalStateException()
    }

}