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
import android.support.v7.widget.*
import android.support.v7.widget.Toolbar
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
        private val controller: EditSettingController
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
        val action = actionSpinner.selectedItemPosition
        val habitPosition = habitSpinner.selectedItemPosition
        val habit = habitList.getByPosition(habitPosition)
        controller.onSave(habit, action)
    }

    private fun populateHabitSpinner() {
        val names = habitList.mapTo(LinkedList<String>()) { it.name }
        val adapter = ArrayAdapter(context, simple_spinner_item, names)
        adapter.setDropDownViewResource(simple_spinner_dropdown_item)
        habitSpinner.adapter = adapter
    }
}
