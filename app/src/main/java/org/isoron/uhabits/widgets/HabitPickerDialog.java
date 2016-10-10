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

package org.isoron.uhabits.widgets;

import android.app.*;
import android.content.*;
import android.os.*;
import android.view.*;
import android.widget.*;

import org.isoron.uhabits.*;
import org.isoron.uhabits.models.*;
import org.isoron.uhabits.preferences.*;

import java.util.*;

import static android.appwidget.AppWidgetManager.*;

public class HabitPickerDialog extends Activity
    implements AdapterView.OnItemClickListener
{
    private HabitList habitList;

    private WidgetPreferences preferences;

    private Integer widgetId;

    private ArrayList<Long> habitIds;

    @Override
    public void onItemClick(AdapterView<?> parent,
                            View view,
                            int position,
                            long id)
    {
        Long habitId = habitIds.get(position);
        preferences.addWidget(widgetId, habitId);

        HabitsApplication app = (HabitsApplication) getApplicationContext();
        app.getComponent().getWidgetUpdater().updateWidgets();

        Intent resultValue = new Intent();
        resultValue.putExtra(EXTRA_APPWIDGET_ID, widgetId);
        setResult(RESULT_OK, resultValue);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.widget_configure_activity);

        HabitsApplication app = (HabitsApplication) getApplicationContext();
        AppComponent component = app.getComponent();
        habitList = component.getHabitList();
        preferences = component.getWidgetPreferences();

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();

        if (extras != null)
            widgetId = extras.getInt(EXTRA_APPWIDGET_ID, INVALID_APPWIDGET_ID);

        ListView listView = (ListView) findViewById(R.id.listView);

        habitIds = new ArrayList<>();
        ArrayList<String> habitNames = new ArrayList<>();

        for (Habit h : habitList)
        {
            habitIds.add(h.getId());
            habitNames.add(h.getName());
        }

        ArrayAdapter<String> adapter =
            new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,
                habitNames);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);
    }

}
