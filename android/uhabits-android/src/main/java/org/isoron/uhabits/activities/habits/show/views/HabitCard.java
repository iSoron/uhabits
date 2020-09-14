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

package org.isoron.uhabits.activities.habits.show.views;

import android.content.*;
import android.util.*;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.isoron.uhabits.*;
import org.isoron.uhabits.core.models.*;
import org.isoron.uhabits.core.models.memory.*;
import org.isoron.uhabits.core.tasks.*;

public abstract class HabitCard extends LinearLayout
    implements ModelObservable.Listener
{
    @NonNull
    private Habit habit;

    @Nullable
    private TaskRunner taskRunner;

    @Nullable
    private Task currentRefreshTask;

    protected HabitsApplication app;
    protected HabitsApplicationComponent component;

    public HabitCard(Context context)
    {
        super(context);
        init();
    }

    public HabitCard(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init();
    }

    @NonNull
    public Habit getHabit()
    {
        return habit;
    }

    public void setHabit(@NonNull Habit habit)
    {
        detachFrom(this.habit);
        attachTo(habit);

        this.habit = habit;
    }

    @Override
    public void onModelChange()
    {
        post(() -> refreshData());
    }

    @Override
    protected void onAttachedToWindow()
    {
        if(isInEditMode()) return;

        super.onAttachedToWindow();
        refreshData();
        attachTo(habit);
    }

    @Override
    protected void onDetachedFromWindow()
    {
        detachFrom(habit);
        super.onDetachedFromWindow();
    }

    protected void refreshData()
    {
        if(taskRunner == null) return;
        if(currentRefreshTask != null) currentRefreshTask.cancel();
        currentRefreshTask = createRefreshTask();
        taskRunner.execute(currentRefreshTask);
    }

    protected abstract Task createRefreshTask();

    private void attachTo(Habit habit)
    {
        habit.getObservable().addListener(this);
        habit.getRepetitions().getObservable().addListener(this);
    }

    private void detachFrom(Habit habit)
    {
        habit.getRepetitions().getObservable().removeListener(this);
        habit.getObservable().removeListener(this);
    }

    private void init()
    {
        if(!isInEditMode()) habit = new MemoryModelFactory().buildHabit();
        Context appContext = getContext().getApplicationContext();
        app = (HabitsApplication) appContext;
        component = app.getComponent();
        taskRunner = component.getTaskRunner();
    }
}
