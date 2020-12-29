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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.*;
import android.util.*;

import org.isoron.uhabits.*;
import org.isoron.uhabits.activities.common.views.*;
import org.isoron.uhabits.core.commands.*;
import org.isoron.uhabits.core.models.*;
import org.isoron.uhabits.core.preferences.*;
import org.isoron.uhabits.core.tasks.*;
import org.isoron.uhabits.core.ui.callbacks.*;
import org.isoron.uhabits.utils.*;
import org.jetbrains.annotations.*;

import static org.isoron.uhabits.utils.InterfaceUtils.*;

public class HistoryEditorDialog extends AppCompatDialogFragment
        implements DialogInterface.OnClickListener, CommandRunner.Listener
{
    @Nullable
    private Habit habit;

    @Nullable
    HistoryChart historyChart;

    @NonNull
    private OnToggleCheckmarkListener onToggleCheckmarkListener;

    private HabitList habitList;

    private TaskRunner taskRunner;

    private Preferences prefs;

    private CommandRunner commandRunner;

    public HistoryEditorDialog()
    {
        this.onToggleCheckmarkListener = new OnToggleCheckmarkListener()
        {
            @Override
            public void onToggleEntry(@NotNull Timestamp timestamp, int value)
            {
            }
        };
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
        commandRunner = app.getComponent().getCommandRunner();
        prefs = app.getComponent().getPreferences();

        historyChart = new HistoryChart(context);
        historyChart.setOnToggleCheckmarkListener(onToggleCheckmarkListener);
        historyChart.setFirstWeekday(prefs.getFirstWeekday());
        historyChart.setSkipEnabled(prefs.isSkipEnabled());

        if (savedInstanceState != null)
        {
            long id = savedInstanceState.getLong("habit", -1);
            if (id > 0) this.habit = habitList.getById(id);
            historyChart.onRestoreInstanceState(
                savedInstanceState.getParcelable("historyChart"));
        }

        int padding =
            (int) getDimension(getContext(), R.dimen.history_editor_padding);

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
    public void onResume()
    {
        super.onResume();

        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int maxHeight = getResources().getDimensionPixelSize(
            R.dimen.history_editor_max_height);
        int width = metrics.widthPixels;
        int height = Math.min(metrics.heightPixels, maxHeight);

        getDialog().getWindow().setLayout(width, height);
        commandRunner.addListener(this);
        refreshData();
    }

    @Override
    public void onPause()
    {
        commandRunner.removeListener(this);
        super.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        outState.putLong("habit", habit.getId());
        outState.putParcelable("historyChart", historyChart.onSaveInstanceState());
    }

    public void setOnToggleCheckmarkListener(@NonNull OnToggleCheckmarkListener onToggleCheckmarkListener)
    {
        this.onToggleCheckmarkListener = onToggleCheckmarkListener;
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

    @Override
    public void onCommandFinished(@NonNull Command command)
    {
        refreshData();
    }

    private class RefreshTask implements Task
    {
        public int[] checkmarks;

        @Override
        public void doInBackground()
        {
            checkmarks = habit.getComputedEntries().getAllValues();
        }

        @Override
        public void onPostExecute()
        {
            if (getContext() == null || habit == null || historyChart == null)
                return;

            int color = PaletteUtilsKt.toThemedAndroidColor(habit.getColor(), getContext());
            historyChart.setColor(color);
            historyChart.setEntries(checkmarks);
            historyChart.setNumerical(habit.isNumerical());
        }
    }
}
