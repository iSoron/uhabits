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

package org.isoron.uhabits.widgets.activities

import android.app.*
import android.appwidget.AppWidgetManager.*
import android.content.*
import android.os.*
import android.widget.*
import android.widget.AbsListView.*
import org.isoron.uhabits.*
import org.isoron.uhabits.core.preferences.*
import org.isoron.uhabits.widgets.*
import java.util.*

open class HabitPickerDialog : Activity() {

    private var widgetId = 0
    private lateinit var widgetPreferences: WidgetPreferences
    private lateinit var widgetUpdater: WidgetUpdater

    protected open fun shouldHideNumerical() = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val component = (applicationContext as HabitsApplication).component
        val habitList = component.habitList
        val preferences = component.preferences
        widgetPreferences = component.widgetPreferences
        widgetUpdater = component.widgetUpdater
        widgetId = intent.extras?.getInt(EXTRA_APPWIDGET_ID, INVALID_APPWIDGET_ID) ?: 0

        val habitIds = ArrayList<Long>()
        val habitNames = ArrayList<String>()
        for (h in habitList) {
            if (h.isArchived) continue
            if (h.isNumerical and shouldHideNumerical()) continue
            habitIds.add(h.id!!)
            habitNames.add(h.name)
        }

        setContentView(R.layout.widget_configure_activity)
        val listView = findViewById<ListView>(R.id.listView)
        val saveButton = findViewById<Button>(R.id.buttonSave)

        if(preferences.isWidgetStackEnabled) {
            with(listView) {
                adapter = ArrayAdapter(context,
                                       android.R.layout.simple_list_item_multiple_choice,
                                       habitNames)
                choiceMode = CHOICE_MODE_MULTIPLE
                itemsCanFocus = false
            }
            saveButton.setOnClickListener {
                val selectedIds = mutableListOf<Long>()
                for (i in 0..listView.count) {
                    if (listView.isItemChecked(i)) {
                        selectedIds.add(habitIds[i])
                    }
                }
                confirm(selectedIds)
            }
        } else {
            saveButton.visibility = GONE
            with(listView) {
                adapter = ArrayAdapter(context,
                                       android.R.layout.simple_list_item_1,
                                       habitNames)
                choiceMode = CHOICE_MODE_SINGLE
                itemsCanFocus = false
            }
            listView.setOnItemClickListener { _, _, position, _ ->
                confirm(listOf(habitIds[position]))
            }
        }
    }

    fun confirm(selectedIds: List<Long>) {
        widgetPreferences.addWidget(widgetId, selectedIds.toLongArray())
        widgetUpdater.updateWidgets()
        setResult(RESULT_OK, Intent().apply {
            putExtra(EXTRA_APPWIDGET_ID, widgetId)
        })
        finish()
    }
}
