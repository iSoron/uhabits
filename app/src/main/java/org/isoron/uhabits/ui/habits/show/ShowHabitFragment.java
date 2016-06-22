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

import android.os.*;
import android.support.v4.app.*;
import android.view.*;
import android.widget.*;

import org.isoron.uhabits.*;
import org.isoron.uhabits.models.*;
import org.isoron.uhabits.ui.habits.edit.*;
import org.isoron.uhabits.ui.habits.show.views.*;
import org.isoron.uhabits.utils.*;

import java.util.*;

import butterknife.*;

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

    @BindView(R.id.subtitle)
    SubtitleCardView subtitleView;

    @BindView(R.id.overview)
    OverviewCardView overview;

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
        subtitleView.setHabit(habit);
        overview.setHabit(habit);

        dataViews = new LinkedList<>();
        dataViews.add(habitScoreView);
        dataViews.add(habitHistoryView);
        dataViews.add(habitFrequencyView);
        dataViews.add(habitStreakView);

        for (HabitDataView dataView : dataViews)
            dataView.setHabit(habit);
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

    private void setScoreBucketSize(int position)
    {
        if (getView() == null) return;

        habitScoreView.setBucketSize(
            HabitScoreView.DEFAULT_BUCKET_SIZES[position]);

        InterfaceUtils.setDefaultScoreInterval(getContext(), position);
        previousScoreInterval = position;
    }

    @Override
    public void onModelChange()
    {
        activity.runOnUiThread(() -> {
            helper.updateColors();
            helper.updateCardHeaders(getView());
            if (activity != null) activity.setupHabitActionBar();
        });
    }

    @Override
    public void onStart()
    {
        super.onStart();
        habit.getObservable().addListener(this);
    }

    @Override
    public void onPause()
    {
        habit.getObservable().removeListener(this);
        super.onPause();
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
