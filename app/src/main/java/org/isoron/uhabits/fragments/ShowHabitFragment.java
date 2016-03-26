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
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import org.isoron.uhabits.helpers.ColorHelper;
import org.isoron.uhabits.helpers.DialogHelper;
import org.isoron.uhabits.HabitBroadcastReceiver;
import org.isoron.uhabits.R;
import org.isoron.uhabits.ShowHabitActivity;
import org.isoron.uhabits.commands.Command;
import org.isoron.uhabits.dialogs.HistoryEditorDialog;
import org.isoron.uhabits.helpers.ReminderHelper;
import org.isoron.uhabits.models.Habit;
import org.isoron.uhabits.models.Score;
import org.isoron.uhabits.views.HabitDataView;
import org.isoron.uhabits.views.HabitFrequencyView;
import org.isoron.uhabits.views.HabitHistoryView;
import org.isoron.uhabits.views.HabitScoreView;
import org.isoron.uhabits.views.HabitStreakView;
import org.isoron.uhabits.views.RepetitionCountView;
import org.isoron.uhabits.views.RingView;

import java.util.LinkedList;
import java.util.List;

public class ShowHabitFragment extends Fragment
        implements DialogHelper.OnSavedListener, HistoryEditorDialog.Listener,
        Spinner.OnItemSelectedListener
{
    @Nullable
    protected ShowHabitActivity activity;

    @Nullable
    private Habit habit;

    @Nullable
    private List<HabitDataView> dataViews;

    @Nullable
    private HabitScoreView scoreView;

    @Nullable
    private SharedPreferences prefs;

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
        Spinner sStrengthInterval = (Spinner) view.findViewById(R.id.sStrengthInterval);

        scoreView = (HabitScoreView) view.findViewById(R.id.scoreView);

        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        int defaultScoreInterval = prefs.getInt("pref_score_view_interval", 1);
        if(defaultScoreInterval > 5 || defaultScoreInterval < 0) defaultScoreInterval = 1;
        setScoreBucketSize(defaultScoreInterval);
        sStrengthInterval.setSelection(defaultScoreInterval);
        sStrengthInterval.setOnItemSelectedListener(this);

        dataViews.add((HabitStreakView) view.findViewById(R.id.streakView));
        dataViews.add((HabitStreakView) view.findViewById(R.id.smallStreakView));
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
        if(habit == null) return;

        RingView scoreRing = (RingView) view.findViewById(R.id.scoreRing);
        scoreRing.setColor(habit.color);
        scoreRing.setPercentage((float) habit.scores.getTodayValue() / Score.MAX_VALUE);
    }

    private void updateHeaders(View view)
    {
        if(habit == null | activity == null) return;

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
        if(habit == null) return;

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
        if(habit == null) return false;

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
        if(activity == null) return;
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
        if(dataViews == null) return;
        updateScoreRing(getView());

        for(HabitDataView view : dataViews)
            view.refreshData();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
    {
        if(parent.getId() == R.id.sStrengthInterval)
            setScoreBucketSize(position);
    }

    private void setScoreBucketSize(int position)
    {
        int sizes[] = { 1, 7, 31, 92, 365 };
        int size = sizes[position];

        if(scoreView != null)
        {
            scoreView.setBucketSize(size);
            scoreView.refreshData();
        }

        if(prefs != null)
            prefs.edit().putInt("pref_score_view_interval", position).apply();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent)
    {

    }
}
