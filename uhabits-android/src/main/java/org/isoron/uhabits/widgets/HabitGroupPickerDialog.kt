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

package org.isoron.uhabits.widgets

import android.app.Activity
import android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_ID
import android.appwidget.AppWidgetManager.INVALID_APPWIDGET_ID
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.CompoundButton
import org.isoron.uhabits.HabitsApplication
import org.isoron.uhabits.R
import org.isoron.uhabits.core.models.HabitList
import org.isoron.uhabits.core.preferences.WidgetPreferences

class HabitGroupPickerDialog : Activity(), AdapterView.OnItemClickListener {

    private var widgetId = 0
    private lateinit var habitList: HabitList
    private lateinit var preferences: WidgetPreferences
    private lateinit var habitIds: ArrayList<Long>
    private lateinit var widgetUpdater: WidgetUpdater
    private lateinit var habitIdsSelected: ArrayList<Long>
    private lateinit var habitListView: ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val component = (applicationContext as HabitsApplication).component
        habitList = component.habitList
        preferences = component.widgetPreferences
        widgetUpdater = component.widgetUpdater
        widgetId = intent.extras?.getInt(EXTRA_APPWIDGET_ID, INVALID_APPWIDGET_ID) ?: 0
        habitIdsSelected = ArrayList<Long>()

        habitIds = ArrayList<Long>()
        val habitNames = ArrayList<String>()
        for (h in habitList) {
            if (h.isArchived) continue
            habitIds.add(h.getId()!!)
            habitNames.add(h.name)
        }

        setContentView(R.layout.stack_widget_configure_activity)
        habitListView = findViewById(R.id.stackWidgetListView) as ListView
        with(habitListView) {
            adapter = ListAdapter(context, R.layout.habit_checkbox_list_item,
                    R.id.listItemHabitName, R.id.listItemHabitCheckbox, habitNames)
            onItemClickListener = this@HabitGroupPickerDialog
        }
        with(findViewById(R.id.doneConfigureButton) as Button) {
            setOnClickListener {
                if (habitIdsSelected.size == 1) {
                    preferences.addWidget(widgetId, habitIdsSelected.first())
                    widgetUpdater.updateWidgets()
                    setResult(Activity.RESULT_OK, Intent().apply {
                        putExtra(EXTRA_APPWIDGET_ID, widgetId)
                    })
                    finish()
                } else if (!habitIdsSelected.isEmpty()) {
                    preferences.addWidget(widgetId, habitIdsSelected.toString())
                    widgetUpdater.updateWidgets()
                    setResult(Activity.RESULT_OK, Intent().apply {
                        putExtra(EXTRA_APPWIDGET_ID, widgetId)
                    })
                    finish()
                } else {
                    Toast.makeText(context, getString(R.string.select_habit_requirement_prompt),
                            Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onItemClick(parent: AdapterView<*>,
                             view: View,
                             position: Int,
                             id: Long) {
        val checkbox = view.findViewById(R.id.listItemHabitCheckbox) as CheckBox
        checkbox.isChecked = !checkbox.isChecked
    }

    private inner class ListAdapter(context: Context,
                                    private var layoutResource: Int,
                                    private var textViewResourceId: Int,
                                    private var checkBoxResourceId: Int,
                                    private var habitNames: List<String>) :
            ArrayAdapter<String>(context, layoutResource, textViewResourceId, habitNames) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val layoutInflater: LayoutInflater = LayoutInflater.from(context)
            val view = layoutInflater.inflate(layoutResource, null)

            val item = getItem(position)
            if (item != null) {
                val tv = view.findViewById(textViewResourceId) as TextView
                tv.text = habitNames.get(position)
                val cb = view.findViewById(checkBoxResourceId) as CheckBox
                cb.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
                    if (isChecked) {
                        habitIdsSelected.add(habitIds.get(position))
                    } else {
                        habitIdsSelected.remove(habitIds.get(position))
                    }
                })
            }

            return view
        }

    }
}
