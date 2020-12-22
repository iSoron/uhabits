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
import org.isoron.androidbase.utils.*
import org.isoron.uhabits.activities.*
import org.isoron.uhabits.activities.habits.show.*
import org.isoron.uhabits.databinding.*
import org.isoron.uhabits.utils.*

class SubtitleCard(context: Context, attrs: AttributeSet) : DataView<ShowHabitViewModel>(context, attrs) {

    private val binding = ShowHabitSubtitleBinding.inflate(LayoutInflater.from(context), this)

    init {
        val fontAwesome = InterfaceUtils.getFontAwesome(context)
        binding.targetIcon.typeface = fontAwesome
        binding.frequencyIcon.typeface = fontAwesome
        binding.reminderIcon.typeface = fontAwesome
    }

    override fun onData(data: ShowHabitViewModel) {
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