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

package org.isoron.uhabits.ui.habits.list;

import android.content.*;
import android.content.res.*;
import android.support.annotation.*;
import android.support.v7.widget.Toolbar;
import android.view.*;
import android.widget.*;

import org.isoron.uhabits.*;
import org.isoron.uhabits.models.*;
import org.isoron.uhabits.ui.*;
import org.isoron.uhabits.ui.habits.list.controllers.*;
import org.isoron.uhabits.ui.habits.list.model.*;
import org.isoron.uhabits.ui.habits.list.views.*;
import org.isoron.uhabits.utils.*;

import butterknife.*;

public class ListHabitsRootView extends BaseRootView
    implements ModelObservable.Listener
{
    public static final int MAX_CHECKMARK_COUNT = 21;

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

    @Nullable
    private HabitCardListAdapter listAdapter;

    public ListHabitsRootView(@NonNull Context context)
    {
        super(context);
        init();
    }

    public static int getCheckmarkCount(View v)
    {
        Resources res = v.getResources();
        float labelWidth = res.getDimension(R.dimen.habitNameWidth);
        float buttonWidth = res.getDimension(R.dimen.checkmarkWidth);
        return Math.min(MAX_CHECKMARK_COUNT, Math.max(0,
            (int) ((v.getMeasuredWidth() - labelWidth) / buttonWidth)));
    }

    @Override
    @NonNull
    public ProgressBar getProgressBar()
    {
        return progressBar;
    }

    public void setShowArchived(boolean showArchived)
    {
        if (listAdapter == null) return;
        listAdapter.setShowArchived(showArchived);
    }

    @NonNull
    @Override
    public Toolbar getToolbar()
    {
        return toolbar;
    }

    @Override
    public int getToolbarColor()
    {
        return InterfaceUtils.getStyledColor(getContext(), R.attr.colorPrimary);
    }

    @Override
    public void onModelChange()
    {
        updateEmptyView();
    }

    public void setController(@Nullable ListHabitsController controller,
                              @Nullable ListHabitsSelectionMenu menu)
    {
        listView.setController(null);
        if (controller == null || menu == null || listAdapter == null) return;

        HabitCardListController listController =
            new HabitCardListController(listAdapter, listView);

        listController.setHabitListener(controller);
        listController.setSelectionListener(menu);
        listView.setController(listController);
        menu.setListController(listController);
    }

    public void setListAdapter(@NonNull HabitCardListAdapter listAdapter)
    {
        if (this.listAdapter != null)
            listAdapter.getObservable().removeListener(this);

        this.listAdapter = listAdapter;
        listView.setAdapter(listAdapter);
        listAdapter.setListView(listView);
    }

    @Override
    protected void onAttachedToWindow()
    {
        super.onAttachedToWindow();
        if (listAdapter != null) listAdapter.getObservable().addListener(this);
    }

    @Override
    protected void onDetachedFromWindow()
    {
        if (listAdapter != null)
            listAdapter.getObservable().removeListener(this);
        super.onDetachedFromWindow();
    }

    private void init()
    {
        addView(inflate(getContext(), R.layout.list_habits, null));
        ButterKnife.bind(this);

        tvStarEmpty.setTypeface(InterfaceUtils.getFontAwesome(getContext()));
        initToolbar();

        String hints[] =
            getContext().getResources().getStringArray(R.array.hints);
        HintList hintList = new HintList(hints);
        hintView.setHints(hintList);
    }

    private void updateEmptyView()
    {
        if (listAdapter == null) return;

        llEmpty.setVisibility(
            listAdapter.getCount() > 0 ? View.GONE : View.VISIBLE);
    }
}
