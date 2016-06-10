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

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.isoron.uhabits.HabitsApplication;
import org.isoron.uhabits.R;
import org.isoron.uhabits.models.Habit;
import org.isoron.uhabits.models.HabitList;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class HabitPickerDialog extends Activity implements AdapterView.OnItemClickListener
{
    @Inject
    HabitList habitList;

    private Integer widgetId;

    private ArrayList<Long> habitIds;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.widget_configure_activity);
        HabitsApplication.getComponent().inject(this);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();

        if (extras != null) widgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);

        ListView listView = (ListView) findViewById(R.id.listView);

        habitIds = new ArrayList<>();
        ArrayList<String> habitNames = new ArrayList<>();

        List<Habit> habits = habitList.getAll(false);
        for (Habit h : habits)
        {
            habitIds.add(h.getId());
            habitNames.add(h.getName());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,
            habitNames);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
        Long habitId = habitIds.get(position);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(
            getApplicationContext());
        prefs
            .edit()
            .putLong(BaseWidgetProvider.getHabitIdKey(widgetId), habitId)
            .commit();

        WidgetManager.updateWidgets(this);

        Intent resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        setResult(RESULT_OK, resultValue);
        finish();
    }

}
