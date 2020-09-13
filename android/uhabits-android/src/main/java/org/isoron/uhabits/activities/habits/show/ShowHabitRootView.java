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

package org.isoron.uhabits.activities.habits.show;

import android.content.*;
import android.os.*;
import android.view.*;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.*;

import org.isoron.androidbase.activities.*;
import org.isoron.androidbase.utils.*;
import org.isoron.uhabits.R;
import org.isoron.uhabits.activities.habits.show.views.*;
import org.isoron.uhabits.core.models.*;
import org.isoron.uhabits.utils.*;

import javax.inject.*;

import butterknife.*;

@ActivityScope
public class ShowHabitRootView extends BaseRootView
    implements ModelObservable.Listener
{
    @NonNull
    private Habit habit;

    @BindView(R.id.frequencyCard)
    FrequencyCard frequencyCard;

    @BindView(R.id.streakCard)
    StreakCard streakCard;

    @BindView(R.id.subtitleCard)
    SubtitleCard subtitleCard;

    @BindView(R.id.notesCard)
    NotesCard notesCard;

    @BindView(R.id.habitNotes)
    TextView habitNotes;

    @BindView(R.id.overviewCard)
    OverviewCard overviewCard;

    @BindView(R.id.scoreCard)
    ScoreCard scoreCard;

    @BindView(R.id.historyCard)
    HistoryCard historyCard;

    @BindView(R.id.barCard)
    BarCard barCard;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.targetCard)
    TargetCard targetCard;

    @NonNull
    private Controller controller;

    @Inject
    public ShowHabitRootView(@NonNull @ActivityContext Context context,
                             @NonNull Habit habit)
    {
        super(context);
        this.habit = habit;

        addView(inflate(getContext(), R.layout.show_habit, null));
        ButterKnife.bind(this);

        controller = new Controller() {};
        setDisplayHomeAsUp(true);
        initCards();
        initToolbar();
    }

    @Override
    public int getToolbarColor()
    {
        StyledResources res = new StyledResources(getContext());
        if (!res.getBoolean(R.attr.useHabitColorAsPrimary))
            return super.getToolbarColor();

        return PaletteUtils.getColor(getContext(), habit.getColor());
    }

    @Override
    public void onModelChange()
    {
        new Handler(Looper.getMainLooper()).post(() -> {
            toolbar.setTitle(habit.getName());
        });

        controller.onToolbarChanged();
    }

    public void setController(@NonNull Controller controller)
    {
        this.controller = controller;
        historyCard.setController(controller);
    }

    @Override
    protected void initToolbar()
    {
        super.initToolbar();
        toolbar.setTitle(habit.getName());
    }

    @Override
    protected void onAttachedToWindow()
    {
        super.onAttachedToWindow();
        habit.getObservable().addListener(this);
    }

    @Override
    protected void onDetachedFromWindow()
    {
        habit.getObservable().removeListener(this);
        super.onDetachedFromWindow();
    }

    private void initCards()
    {
        subtitleCard.setHabit(habit);
        notesCard.setHabit(habit);
        overviewCard.setHabit(habit);
        scoreCard.setHabit(habit);
        historyCard.setHabit(habit);
        streakCard.setHabit(habit);
        frequencyCard.setHabit(habit);
        barCard.setHabit(habit);
        targetCard.setHabit(habit);

        if(habit.isNumerical()) {
            overviewCard.setVisibility(View.GONE);
            streakCard.setVisibility(View.GONE);
        } else {
            targetCard.setVisibility(View.GONE);
        }
    }

    public interface Controller extends HistoryCard.Controller
    {
        default void onToolbarChanged() {}
    }
}
