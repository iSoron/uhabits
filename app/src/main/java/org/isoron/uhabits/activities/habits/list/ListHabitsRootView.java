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

package org.isoron.uhabits.activities.habits.list;

import android.content.*;
import android.content.res.*;
import android.support.annotation.*;
import android.support.v7.widget.Toolbar;
import android.view.*;
import android.widget.*;

import org.isoron.uhabits.R;
import org.isoron.uhabits.activities.*;
import org.isoron.uhabits.activities.common.views.*;
import org.isoron.uhabits.activities.habits.list.controllers.*;
import org.isoron.uhabits.activities.habits.list.model.*;
import org.isoron.uhabits.activities.habits.list.views.*;
import org.isoron.uhabits.models.*;
import org.isoron.uhabits.tasks.*;
import org.isoron.uhabits.utils.*;

import javax.inject.*;

import butterknife.*;

@ActivityScope
public class ListHabitsRootView extends BaseRootView
    implements ModelObservable.Listener, TaskRunner.Listener
{
    public static final int MAX_CHECKMARK_COUNT = 60;

    @BindView(R.id.listView)
    HabitCardListView listView;

    @BindView(R.id.llEmpty)
    ViewGroup llEmpty;

    @BindView(R.id.tvStarEmpty)
    TextView tvStarEmpty;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.progressBar)
    ProgressBar progressBar;

    @BindView(R.id.hintView)
    HintView hintView;

    @BindView(R.id.header)
    HeaderView header;

    @NonNull
    private final HabitCardListAdapter listAdapter;

    private final TaskRunner runner;

    @Inject
    public ListHabitsRootView(@ActivityContext Context context,
                              @NonNull HintListFactory hintListFactory,
                              @NonNull HabitCardListAdapter listAdapter,
                              @NonNull TaskRunner runner)
    {
        super(context);
        addView(inflate(getContext(), R.layout.list_habits, null));
        ButterKnife.bind(this);

        this.listAdapter = listAdapter;
        listView.setAdapter(listAdapter);
        listAdapter.setListView(listView);

        this.runner = runner;
        progressBar.setIndeterminate(true);
        tvStarEmpty.setTypeface(InterfaceUtils.getFontAwesome(getContext()));

        String hints[] =
            getContext().getResources().getStringArray(R.array.hints);
        HintList hintList = hintListFactory.create(hints);
        hintView.setHints(hintList);

        initToolbar();
    }

    @NonNull
    @Override
    public Toolbar getToolbar()
    {
        return toolbar;
    }

    @Override
    public void onModelChange()
    {
        updateEmptyView();
    }

    @Override
    public void onTaskFinished(Task task)
    {
        updateProgressBar();
    }

    @Override
    public void onTaskStarted(Task task)
    {
        updateProgressBar();
    }

    public void setController(@NonNull ListHabitsController controller,
                              @NonNull ListHabitsSelectionMenu menu)
    {
        HabitCardListController listController =
            new HabitCardListController(listAdapter);

        listController.setHabitListener(controller);
        listController.setSelectionListener(menu);
        listView.setController(listController);
        menu.setListController(listController);
        header.setScrollController(new ScrollableChart.ScrollController() {
            @Override
            public void onDataOffsetChanged(int newDataOffset)
            {
                listView.setDataOffset(newDataOffset);
            }
        });
    }

    @Override
    protected void onAttachedToWindow()
    {
        super.onAttachedToWindow();
        runner.addListener(this);
        updateProgressBar();
        listAdapter.getObservable().addListener(this);
    }

    @Override
    protected void onDetachedFromWindow()
    {
        listAdapter.getObservable().removeListener(this);
        runner.removeListener(this);
        super.onDetachedFromWindow();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh)
    {
        int count = getCheckmarkCount();
        header.setButtonCount(count);
        header.setMaxDataOffset(Math.max(MAX_CHECKMARK_COUNT - count, 0));
        listView.setCheckmarkCount(count);
        super.onSizeChanged(w, h, oldw, oldh);
    }

    private int getCheckmarkCount()
    {
        Resources res = getResources();
        float labelWidth = Math.max(getMeasuredWidth() / 3, res.getDimension(R.dimen.habitNameWidth));
        float buttonWidth = res.getDimension(R.dimen.checkmarkWidth);
        return Math.min(MAX_CHECKMARK_COUNT, Math.max(0,
            (int) ((getMeasuredWidth() - labelWidth) / buttonWidth)));
    }

    private void updateEmptyView()
    {
        llEmpty.setVisibility(
            listAdapter.getItemCount() > 0 ? View.GONE : View.VISIBLE);
    }

    private void updateProgressBar()
    {
        postDelayed(() -> {
            int activeTaskCount = runner.getActiveTaskCount();
            int newVisibility = activeTaskCount > 0 ? VISIBLE : GONE;
            if (progressBar.getVisibility() != newVisibility)
                progressBar.setVisibility(newVisibility);
        }, 500);
    }
}
