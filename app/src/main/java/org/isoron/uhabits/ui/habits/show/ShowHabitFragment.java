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
import org.isoron.uhabits.ui.habits.show.views.cards.*;

import butterknife.*;

public class ShowHabitFragment extends Fragment
{
    Habit habit;

    protected ShowHabitActivity activity;

    @BindView(R.id.frequencyCard)
    FrequencyCard frequencyCard;

    @BindView(R.id.streakCard)
    StreakCard streakCard;

    @BindView(R.id.subtitleCard)
    SubtitleCard subtitleCard;

    @BindView(R.id.overviewCard)
    OverviewCard overviewCard;

    @BindView(R.id.strengthCard)
    ScoreCard scoreCard;

    @BindView(R.id.historyCard)
    HistoryCard historyCard;

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
//        inflater.inflate(R.menu.show_habit_fragment, menu);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.show_habit, container, false);
        ButterKnife.bind(this, view);

        activity = (ShowHabitActivity) getActivity();
        habit = activity.getHabit();

        subtitleCard.setHabit(habit);
        overviewCard.setHabit(habit);
        scoreCard.setHabit(habit);
        historyCard.setHabit(habit);
        streakCard.setHabit(habit);
        frequencyCard.setHabit(habit);

        setHasOptionsMenu(true);
        return view;
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
}
