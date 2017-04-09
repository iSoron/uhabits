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

package org.isoron.uhabits.activities.common.dialogs;

import android.app.*;
import android.content.*;
import android.os.*;
import android.support.annotation.*;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.*;
import android.util.*;

import org.isoron.uhabits.*;
import org.isoron.uhabits.activities.common.views.*;
import org.isoron.uhabits.models.*;
import org.isoron.uhabits.tasks.*;
import org.isoron.uhabits.utils.*;

public class HistoryEditorDialog extends AppCompatDialogFragment
    implements DialogInterface.OnClickListener, ModelObservable.Listener
{
    @Nullable
    private Habit habit;

    @Nullable
    HistoryChart historyChart;

    @NonNull
    private Controller controller;

    private HabitList habitList;

    private TaskRunner taskRunner;

    public HistoryEditorDialog()
    {
        this.controller = new Controller() {};
    }

    @Override
    public void onClick(DialogInterface dialog, int which)
    {
        dismiss();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState)
    {
        Context context = getActivity();

        HabitsApplication app =
            (HabitsApplication) getActivity().getApplicationContext();
        habitList = app.getComponent().getHabitList();
        taskRunner = app.getComponent().getTaskRunner();

        historyChart = new HistoryChart(context);
        historyChart.setController(controller);

        if (savedInstanceState != null)
        {
            long id = savedInstanceState.getLong("habit", -1);
            if (id > 0) this.habit = habitList.getById(id);
            historyChart.onRestoreInstanceState(
                savedInstanceState.getParcelable("historyChart"));
        }

        int padding =
            (int) getResources().getDimension(R.dimen.history_editor_padding);

        historyChart.setPadding(padding, 0, padding, 0);
        historyChart.setIsEditable(true);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder
            .setTitle(R.string.history)
            .setView(historyChart)
            .setPositiveButton(android.R.string.ok, this);

        return builder.create();
    }

    @Override
    public void onModelChange()
    {
        refreshData();
    }

    @Override
    public void onPause()
    {
        habit.getCheckmarks().observable.removeListener(this);
        super.onPause();
    }

    @Override
    public void onResume()
    {
        super.onResume();

        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int maxHeight = getResources().getDimensionPixelSize(
            R.dimen.history_editor_max_height);
        int width = metrics.widthPixels;
        int height = Math.min(metrics.heightPixels, maxHeight);

        getDialog().getWindow().setLayout(width, height);

        refreshData();
        habit.getCheckmarks().observable.addListener(this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        outState.putLong("habit", habit.getId());
        outState.putParcelable("historyChart", historyChart.onSaveInstanceState());
    }

    public void setController(@NonNull Controller controller)
    {
        this.controller = controller;
        if (historyChart != null) historyChart.setController(controller);
    }

    public void setHabit(@Nullable Habit habit)
    {
        this.habit = habit;
    }

    private void refreshData()
    {
        if (habit == null) return;
        taskRunner.execute(new RefreshTask());
    }

    public interface Controller extends HistoryChart.Controller {}

    private class RefreshTask implements Task
    {
        public int[] checkmarks;

        @Override
        public void doInBackground()
        {
            checkmarks = habit.getCheckmarks().getAllValues();
        }

        @Override
        public void onPostExecute()
        {
            if (getContext() == null || habit == null || historyChart == null)
                return;

            int color = ColorUtils.getColor(getContext(), habit.getColor());
            historyChart.setColor(color);
            historyChart.setCheckmarks(checkmarks);
        }
    }
}
