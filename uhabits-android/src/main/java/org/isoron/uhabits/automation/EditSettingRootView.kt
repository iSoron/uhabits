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

package org.isoron.uhabits.automation

import android.R.layout.simple_spinner_dropdown_item
import android.R.layout.simple_spinner_item
import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.FrameLayout
import org.isoron.uhabits.R
import org.isoron.uhabits.core.models.Habit
import org.isoron.uhabits.core.models.HabitList
import org.isoron.uhabits.core.models.PaletteColor
import org.isoron.uhabits.databinding.AutomationBinding
import org.isoron.uhabits.utils.currentTheme
import org.isoron.uhabits.utils.setupToolbar
import java.util.LinkedList

@SuppressLint("ViewConstructor")
class EditSettingRootView(
    context: Context,
    private val habitList: HabitList,
    private val onSave: (habit: Habit, action: Int) -> Unit,
    args: SettingUtils.Arguments?
) : FrameLayout(context) {

    private var binding = AutomationBinding.inflate(LayoutInflater.from(context))

    init {
        addView(binding.root)
        setupToolbar(
            toolbar = binding.toolbar,
            title = resources.getString(R.string.app_name),
            color = PaletteColor(11),
            displayHomeAsUpEnabled = false,
            theme = currentTheme(),
        )
        populateHabitSpinner()
        binding.habitSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                populateActionSpinner(habitList.getByPosition(position).isNumerical)
            }
        }
        binding.buttonSave.setOnClickListener {
            val habit = habitList.getByPosition(binding.habitSpinner.selectedItemPosition)
            val action = mapSpinnerPositionToAction(
                isNumerical = habit.isNumerical,
                itemPosition = binding.actionSpinner.selectedItemPosition,
            )
            onSave(habit, action)
        }
        args?.let {
            binding.habitSpinner.setSelection(habitList.indexOf(it.habit))
            populateActionSpinner(it.habit.isNumerical)
            binding.actionSpinner.setSelection(mapActionToSpinnerPosition(it.action))
        }
    }

    private fun mapSpinnerPositionToAction(isNumerical: Boolean, itemPosition: Int): Int {
        return if (isNumerical) {
            when (itemPosition) {
                0 -> ACTION_INCREMENT
                else -> ACTION_DECREMENT
            }
        } else {
            when (itemPosition) {
                0 -> ACTION_CHECK
                1 -> ACTION_UNCHECK
                else -> ACTION_TOGGLE
            }
        }
    }

    private fun mapActionToSpinnerPosition(action: Int): Int {
        return when (action) {
            ACTION_CHECK -> 0
            ACTION_UNCHECK -> 1
            ACTION_TOGGLE -> 2
            ACTION_INCREMENT -> 0
            ACTION_DECREMENT -> 1
            else -> 0
        }
    }

    private fun populateHabitSpinner() {
        val names = habitList.mapTo(LinkedList()) { it.name }
        val adapter = ArrayAdapter(context, simple_spinner_item, names)
        adapter.setDropDownViewResource(simple_spinner_dropdown_item)
        binding.habitSpinner.adapter = adapter
    }

    private fun populateActionSpinner(isNumerical: Boolean) {
        val entries = (if (isNumerical) R.array.actions_numerical else R.array.actions_yes_no)
        val adapter = ArrayAdapter.createFromResource(context, entries, simple_spinner_item)
        adapter.setDropDownViewResource(simple_spinner_dropdown_item)
        binding.actionSpinner.adapter = adapter
    }
}
