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

package org.isoron.uhabits.automation

import android.R.layout.*
import android.content.*
import android.view.*
import androidx.appcompat.widget.*
import androidx.appcompat.widget.Toolbar
import android.widget.*
import butterknife.*
import org.isoron.androidbase.activities.*
import org.isoron.androidbase.utils.*
import org.isoron.uhabits.R
import org.isoron.uhabits.core.models.*
import java.util.*

class EditSettingRootView(
        context: Context,
        private val habitList: HabitList,
        private val controller: EditSettingController,
        args: SettingUtils.Arguments?
) : BaseRootView(context) {

    @BindView(R.id.toolbar)
    lateinit var tbar: Toolbar

    @BindView(R.id.habitSpinner)
    lateinit var habitSpinner: AppCompatSpinner

    @BindView(R.id.actionSpinner)
    lateinit var actionSpinner: AppCompatSpinner

    init {
        addView(inflate(getContext(), R.layout.automation, null))
        ButterKnife.bind(this)
        populateHabitSpinner()
        habitSpinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                populateActionSpinner(habitList.getByPosition(position).isNumerical)
            }
        }
        args?.let {
            habitSpinner.setSelection(habitList.indexOf(it.habit))
            populateActionSpinner(it.habit.isNumerical)
            actionSpinner.setSelection(mapActionToSpinnerPosition(it.action))
        }
    }

    override fun getToolbar(): Toolbar {
        return tbar
    }

    override fun getToolbarColor(): Int {
        val res = StyledResources(context)
        if (!res.getBoolean(R.attr.useHabitColorAsPrimary))
            return super.getToolbarColor()

        return res.getColor(R.attr.aboutScreenColor)
    }

    @OnClick(R.id.buttonSave)
    fun onClickSave() {
        val habit = habitList.getByPosition(habitSpinner.selectedItemPosition)
        val action = mapSpinnerPositionToAction(habit.isNumerical,
                                                actionSpinner.selectedItemPosition)
        controller.onSave(habit, action)
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
        return when(action) {
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
        habitSpinner.adapter = adapter
    }

    private fun populateActionSpinner(isNumerical: Boolean) {
        val entries = (if (isNumerical) R.array.actions_numerical else R.array.actions_yes_no)
        val adapter = ArrayAdapter.createFromResource(context, entries, simple_spinner_item)
        adapter.setDropDownViewResource(simple_spinner_dropdown_item)
        actionSpinner.adapter = adapter
    }
}
