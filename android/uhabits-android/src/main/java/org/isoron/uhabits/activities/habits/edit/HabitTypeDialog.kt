/*
 * Copyright (C) 2016-2020 Álinson Santos Xavier <isoron@gmail.com>
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

package org.isoron.uhabits.activities.habits.edit

import android.os.*
import android.view.*
import androidx.appcompat.app.*
import org.isoron.uhabits.*
import org.isoron.uhabits.databinding.*
import org.isoron.uhabits.intents.*

class HabitTypeDialog : AppCompatDialogFragment() {
    override fun getTheme() = R.style.Translucent

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val binding = SelectHabitTypeBinding.inflate(inflater, container, false)

        binding.buttonYesNo.setOnClickListener {
            val intent = IntentFactory().startEditActivity(activity!!)
            startActivity(intent)
            dismiss()
        }

        binding.buttonMeasurable.setOnClickListener {
            dismiss()
        }

        binding.buttonSubjective.setOnClickListener{
            dismiss()
        }

        binding.background.setOnClickListener {
            dismiss()
        }

        return binding.root
    }
}