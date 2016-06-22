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

import org.isoron.uhabits.*;
import org.isoron.uhabits.models.*;
import org.isoron.uhabits.ui.habits.edit.*;
import org.isoron.uhabits.ui.habits.show.views.*;

import java.util.*;

import butterknife.*;

public class ShowHabitFragment extends Fragment
    implements ModelObservable.Listener
{
    Habit habit;

    int activeColor;

    int inactiveColor;

    private ShowHabitHelper helper;

    protected ShowHabitActivity activity;

    private List<HabitDataView> dataViews;

    @BindView(R.id.historyView)
    HistoryView historyView;

    @BindView(R.id.punchcardView)
    FrequencyChart frequencyChart;

    @BindView(R.id.streakChart)
    StreakChart streakChart;

    @BindView(R.id.subtitleCard)
    SubtitleCard subtitleCard;

    @BindView(R.id.overviewCard)
    OverviewCard overviewCard;

    @BindView(R.id.strengthCard)
    ScoreCard scoreCard;

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
        subtitleCard.setHabit(habit);
        overviewCard.setHabit(habit);
        scoreCard.setHabit(habit);

        dataViews = new LinkedList<>();
        dataViews.add(historyView);
        dataViews.add(frequencyChart);
        dataViews.add(streakChart);

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
}
