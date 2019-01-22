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

import android.app.*
import android.appwidget.AppWidgetManager.*
import android.content.*
import android.os.*
import android.widget.*
import android.widget.AbsListView.*
import org.isoron.uhabits.*
import org.isoron.uhabits.core.models.*
import org.isoron.uhabits.core.preferences.*
import java.util.*

class HabitPickerDialog : Activity() {

    private var widgetId = 0
    private lateinit var habitList: HabitList
    private lateinit var preferences: WidgetPreferences
    private lateinit var habitIds: ArrayList<Long>
    private lateinit var widgetUpdater: WidgetUpdater
    private lateinit var listView: ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val component = (applicationContext as HabitsApplication).component
        habitList = component.habitList
        preferences = component.widgetPreferences
        widgetUpdater = component.widgetUpdater
        widgetId = intent.extras?.getInt(EXTRA_APPWIDGET_ID, INVALID_APPWIDGET_ID) ?: 0

        habitIds = ArrayList()
        val habitNames = ArrayList<String>()
        for (h in habitList) {
            if (h.isArchived) continue
            habitIds.add(h.id!!)
            habitNames.add(h.name)
        }

        setContentView(R.layout.widget_configure_activity)
        listView = findViewById(R.id.listView) as ListView

        with(listView) {
            adapter = ArrayAdapter(context, android.R.layout.simple_list_item_multiple_choice, habitNames)
            choiceMode = CHOICE_MODE_MULTIPLE
            itemsCanFocus = false
        }

        with(findViewById(R.id.buttonSave) as Button) {
            setOnClickListener({
                val selectedIds = mutableListOf<Long>()
                for (i in 0..listView.count) {
                    if (listView.isItemChecked(i)) {
                        selectedIds.add(habitIds[i])
                    }
                }

                preferences.addWidget(widgetId, selectedIds.toLongArray())
                widgetUpdater.updateWidgets()
                setResult(Activity.RESULT_OK, Intent().apply {
                    putExtra(EXTRA_APPWIDGET_ID, widgetId)
                })
                finish()
            })
        }
    }
}
