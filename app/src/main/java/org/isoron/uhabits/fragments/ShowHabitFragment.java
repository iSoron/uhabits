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

package org.isoron.uhabits.fragments;

import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.isoron.helpers.ColorHelper;
import org.isoron.helpers.Command;
import org.isoron.helpers.DialogHelper;
import org.isoron.uhabits.HabitBroadcastReceiver;
import org.isoron.uhabits.R;
import org.isoron.uhabits.ShowHabitActivity;
import org.isoron.uhabits.dialogs.HistoryEditorDialog;
import org.isoron.uhabits.helpers.ReminderHelper;
import org.isoron.uhabits.models.Habit;
import org.isoron.uhabits.models.Score;
import org.isoron.uhabits.views.HabitDataView;
import org.isoron.uhabits.views.HabitHistoryView;
import org.isoron.uhabits.views.HabitFrequencyView;
import org.isoron.uhabits.views.HabitScoreView;
import org.isoron.uhabits.views.HabitStreakView;
import org.isoron.uhabits.views.RepetitionCountView;
import org.isoron.uhabits.views.RingView;

import java.util.LinkedList;
import java.util.List;

public class ShowHabitFragment extends Fragment
        implements DialogHelper.OnSavedListener, HistoryEditorDialog.Listener
{
    protected ShowHabitActivity activity;
    private Habit habit;
    private HabitStreakView streakView;
    private HabitScoreView scoreView;
    private HabitHistoryView historyView;
    private HabitFrequencyView punchcardView;

    private List<HabitDataView> dataViews;

    @Override
    public void onStart()
    {
        super.onStart();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.show_habit, container, false);
        activity = (ShowHabitActivity) getActivity();
        habit = activity.habit;

        dataViews = new LinkedList<>();

        Button btEditHistory = (Button) view.findViewById(R.id.btEditHistory);
        streakView = (HabitStreakView) view.findViewById(R.id.streakView);
        scoreView = (HabitScoreView) view.findViewById(R.id.scoreView);
        historyView = (HabitHistoryView) view.findViewById(R.id.historyView);
        punchcardView = (HabitFrequencyView) view.findViewById(R.id.punchcardView);

        dataViews.add((HabitStreakView) view.findViewById(R.id.streakView));
        dataViews.add((HabitScoreView) view.findViewById(R.id.scoreView));
        dataViews.add((HabitHistoryView) view.findViewById(R.id.historyView));
        dataViews.add((HabitFrequencyView) view.findViewById(R.id.punchcardView));

        LinearLayout llRepetition = (LinearLayout) view.findViewById(R.id.llRepetition);
        for(int i = 0; i < llRepetition.getChildCount(); i++)
            dataViews.add((RepetitionCountView) llRepetition.getChildAt(i));

        updateHeaders(view);
        updateScoreRing(view);

        for(HabitDataView dataView : dataViews)
            dataView.setHabit(habit);

        btEditHistory.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                HistoryEditorDialog frag = new HistoryEditorDialog();
                frag.setHabit(habit);
                frag.setListener(ShowHabitFragment.this);
                frag.show(getFragmentManager(), "historyEditor");
            }
        });

        if(savedInstanceState != null)
        {
            EditHabitFragment fragEdit = (EditHabitFragment) getFragmentManager()
                    .findFragmentByTag("editHabit");
            HistoryEditorDialog fragEditor = (HistoryEditorDialog) getFragmentManager()
                    .findFragmentByTag("historyEditor");

            if(fragEdit != null) fragEdit.setOnSavedListener(this);
            if(fragEditor != null) fragEditor.setListener(this);
        }

        setHasOptionsMenu(true);
        return view;
    }

    private void updateScoreRing(View view)
    {
        RingView scoreRing = (RingView) view.findViewById(R.id.scoreRing);
        scoreRing.setColor(habit.color);
        scoreRing.setPercentage((float) habit.scores.getNewestValue() / Score.MAX_SCORE);
    }

    private void updateHeaders(View view)
    {
        if (android.os.Build.VERSION.SDK_INT >= 21)
        {
            int darkerHabitColor = ColorHelper.mixColors(habit.color, Color.BLACK, 0.75f);
            activity.getWindow().setStatusBarColor(darkerHabitColor);
        }

        updateColor(view, R.id.tvHistory);
        updateColor(view, R.id.tvOverview);
        updateColor(view, R.id.tvStrength);
        updateColor(view, R.id.tvStreaks);
        updateColor(view, R.id.tvWeekdayFreq);
        updateColor(view, R.id.tvCount);
    }

    private void updateColor(View view, int viewId)
    {
        TextView textView = (TextView) view.findViewById(viewId);
        textView.setTextColor(habit.color);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        inflater.inflate(R.menu.show_habit_fragment_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.action_edit_habit:
            {
                EditHabitFragment frag = EditHabitFragment.editSingleHabitFragment(habit.getId());
                frag.setOnSavedListener(this);
                frag.show(getFragmentManager(), "editHabit");
                return true;
            }
        }

        return false;
    }

    @Override
    public void onSaved(Command command, Object savedObject)
    {
        Habit h = (Habit) savedObject;

        if (h == null) activity.executeCommand(command, null);
        else activity.executeCommand(command, h.getId());

        ReminderHelper.createReminderAlarms(activity);
        activity.recreate();
    }

    @Override
    public void onHistoryEditorClosed()
    {
        refreshData();
        HabitBroadcastReceiver.sendRefreshBroadcast(getActivity());
    }

    public void refreshData()
    {
        for(HabitDataView view : dataViews)
            view.refreshData();
    }
}
