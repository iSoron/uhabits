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

package org.isoron.uhabits.ui.habits.show;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;

import org.isoron.uhabits.R;
import org.isoron.uhabits.models.Habit;
import org.isoron.uhabits.models.ModelObservable;
import org.isoron.uhabits.tasks.BaseTask;
import org.isoron.uhabits.ui.habits.edit.BaseDialogFragment;
import org.isoron.uhabits.ui.habits.edit.EditHabitDialogFragment;
import org.isoron.uhabits.ui.habits.edit.HistoryEditorDialog;
import org.isoron.uhabits.utils.DateUtils;
import org.isoron.uhabits.utils.InterfaceUtils;
import org.isoron.uhabits.views.HabitDataView;
import org.isoron.uhabits.views.HabitFrequencyView;
import org.isoron.uhabits.views.HabitHistoryView;
import org.isoron.uhabits.views.HabitScoreView;
import org.isoron.uhabits.views.HabitStreakView;

import java.util.LinkedList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ShowHabitFragment extends Fragment
    implements ModelObservable.Listener
{
    Habit habit;

    float todayScore;

    float lastMonthScore;

    float lastYearScore;

    int activeColor;

    int inactiveColor;

    int previousScoreInterval;

    private ShowHabitHelper helper;

    protected ShowHabitActivity activity;

    private List<HabitDataView> dataViews;

    @BindView(R.id.sStrengthInterval)
    Spinner sStrengthInterval;

    @BindView(R.id.scoreView)
    HabitScoreView habitScoreView;

    @BindView(R.id.historyView)
    HabitHistoryView habitHistoryView;

    @BindView(R.id.punchcardView)
    HabitFrequencyView habitFrequencyView;

    @BindView(R.id.streakView)
    HabitStreakView habitStreakView;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.show_habit, container, false);
        ButterKnife.bind(this, view);

        activity = (ShowHabitActivity) getActivity();
        helper = new ShowHabitHelper(this);

        habit = activity.getHabit();
        helper.updateColors();
        helper.updateMainHeader(view);

        int defaultScoreInterval =
            InterfaceUtils.getDefaultScoreInterval(getContext());
        previousScoreInterval = defaultScoreInterval;
        setScoreBucketSize(defaultScoreInterval);

        sStrengthInterval.setSelection(defaultScoreInterval);
        sStrengthInterval.setOnItemSelectedListener(
            new OnItemSelectedListener());

        createDataViews();
        helper.updateCardHeaders(view);
        setHasOptionsMenu(true);

        return view;
    }

    @OnClick(R.id.btEditHistory)
    public void onClickEditHistory()
    {
        HistoryEditorDialog frag = new HistoryEditorDialog();
        frag.setHabit(habit);
        frag.show(getFragmentManager(), "historyEditor");
    }

    private void createDataViews()
    {
        dataViews = new LinkedList<>();
        dataViews.add(habitScoreView);
        dataViews.add(habitHistoryView);
        dataViews.add(habitFrequencyView);
        dataViews.add(habitStreakView);

        for (HabitDataView dataView : dataViews)
            dataView.setHabit(habit);
    }

    @Override
    public void onResume()
    {
        super.onResume();
        refreshData();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
//        inflater.inflate(R.menu.show_habit_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (item.getItemId() == R.id.action_edit_habit)
            return showEditHabitDialog();

        return false;
    }

    private boolean showEditHabitDialog()
    {
        if (habit == null) return false;

        BaseDialogFragment frag =
            EditHabitDialogFragment.newInstance(habit.getId());
        frag.show(getFragmentManager(), "editHabit");
        return true;
    }

    public void refreshData()
    {
        new RefreshTask().execute();
    }

    private void setScoreBucketSize(int position)
    {
        if (getView() == null) return;

        habitScoreView.setBucketSize(
            HabitScoreView.DEFAULT_BUCKET_SIZES[position]);

        if (position != previousScoreInterval) refreshData();

        InterfaceUtils.setDefaultScoreInterval(getContext(), position);
        previousScoreInterval = position;
    }

    @Override
    public void onModelChange()
    {
        refreshData();
        activity.runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                helper.updateColors();
                helper.updateMainHeader(getView());
                helper.updateCardHeaders(getView());
                if (activity != null) activity.setupHabitActionBar();
            }
        });
    }

    @Override
    public void onStart()
    {
        super.onStart();
        habit.observable.addListener(this);
    }

    @Override
    public void onPause()
    {
        habit.observable.removeListener(this);
        super.onPause();
    }

    private class RefreshTask extends BaseTask
    {
        @Override
        protected void doInBackground()
        {
            if (habit == null) return;
            if (dataViews == null) return;

            long today = DateUtils.getStartOfToday();
            long lastMonth = today - 30 * DateUtils.millisecondsInOneDay;
            long lastYear = today - 365 * DateUtils.millisecondsInOneDay;

            todayScore = (float) habit.scores.getTodayValue();
            lastMonthScore = (float) habit.scores.getValue(lastMonth);
            lastYearScore = (float) habit.scores.getValue(lastYear);
        }

        @Override
        protected void onPostExecute(Void aVoid)
        {
            helper.updateScore(getView());
            super.onPostExecute(aVoid);
        }
    }

    private class OnItemSelectedListener
        implements AdapterView.OnItemSelectedListener
    {
        @Override
        public void onItemSelected(AdapterView<?> parent,
                                   View view,
                                   int position,
                                   long id)
        {
            setScoreBucketSize(position);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent)
        {

        }
    }
}
