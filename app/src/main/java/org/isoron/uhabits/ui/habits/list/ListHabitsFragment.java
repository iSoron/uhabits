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

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.isoron.uhabits.R;
import org.isoron.uhabits.commands.Command;
import org.isoron.uhabits.commands.ToggleRepetitionCommand;
import org.isoron.uhabits.models.Habit;
import org.isoron.uhabits.ui.BaseActivity;
import org.isoron.uhabits.ui.HintManager;
import org.isoron.uhabits.ui.habits.edit.EditHabitDialogFragment;
import org.isoron.uhabits.utils.InterfaceUtils;
import org.isoron.uhabits.utils.InterfaceUtils.OnSavedListener;
import org.isoron.uhabits.utils.ReminderUtils;

public class ListHabitsFragment extends Fragment implements OnSavedListener, OnClickListener,
        HabitListSelectionCallback.Listener, ListHabitsController.Screen
{
    private ActionMode actionMode;
    private HintManager hintManager;
    private ListHabitsHelper helper;
    private Listener habitClickListener;
    private BaseActivity activity;

    private HabitListView listView;
    private LinearLayout llButtonsHeader;
    private ProgressBar progressBar;
    private View llEmpty;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.list_habits_fragment, container, false);

        View llHint = view.findViewById(R.id.llHint);
        llButtonsHeader = (LinearLayout) view.findViewById(R.id.llButtonsHeader);
        llEmpty = view.findViewById(R.id.llEmpty);
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        listView = (HabitListView) view.findViewById(R.id.listView);
        TextView tvStarEmpty = (TextView) view.findViewById(R.id.tvStarEmpty);

        helper = new ListHabitsHelper(activity, listView.getLoader());
        hintManager = new HintManager(activity, llHint);

        llHint.setOnClickListener(this);
        tvStarEmpty.setTypeface(InterfaceUtils.getFontAwesome(activity));
        listView.setListener(new HabitListViewListener());
        setHasOptionsMenu(true);

        if(savedInstanceState != null)
        {
            EditHabitDialogFragment frag = (EditHabitDialogFragment) getFragmentManager()
                    .findFragmentByTag("editHabit");
            if(frag != null) frag.setOnSavedListener(this);
        }

        return view;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        this.activity = (BaseActivity) activity;
        habitClickListener = (Listener) activity;
    }

    @Override
    public void onResume()
    {
        super.onResume();

        listView.refreshData(null);
        helper.updateEmptyMessage(llEmpty);
        helper.updateHeader(llButtonsHeader);
        hintManager.showHintIfAppropriate();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.list_habits_fragment, menu);
        MenuItem showArchivedItem = menu.findItem(R.id.action_show_archived);
        showArchivedItem.setChecked(listView.getShowArchived());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.action_add:
                showCreateHabitScreen();
                return true;

            case R.id.action_show_archived:
                toggleShowArchived();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void toggleShowArchived()
    {
        listView.toggleShowArchived();
        activity.invalidateOptionsMenu();
    }

    private void showCreateHabitScreen()
    {
        EditHabitDialogFragment frag = EditHabitDialogFragment.createHabitFragment();
        frag.setOnSavedListener(this);
        frag.show(getFragmentManager(), "editHabit");
    }

    private void startActionMode()
    {
        HabitListSelectionCallback callback =
                new HabitListSelectionCallback(activity, listView.getLoader());
        callback.setSelectedPositions(listView.getSelectedPositions());
        callback.setOnSavedListener(this);
        callback.setListener(this);
        actionMode = activity.startSupportActionMode(callback);
    }

    private void finishActionMode()
    {
        if(actionMode != null) actionMode.finish();
    }

    @Override
    public void onActionModeDestroyed(ActionMode mode)
    {
        actionMode = null;
        listView.cancelSelection();
    }

    @Override
    public void onSaved(Command command, Object savedObject)
    {
        Habit h = (Habit) savedObject;

        if (h == null) activity.executeCommand(command, null);
        else activity.executeCommand(command, h.getId());

        listView.refreshData(null);

        ReminderUtils.createReminderAlarms(activity);

        finishActionMode();
    }

    private void executeCommand(Command c, Long refreshKey)
    {
        activity.executeCommand(c, refreshKey);
    }

    @Override
    public void onClick(View v)
    {
        if (v.getId() == R.id.llHint)
            hintManager.dismissHint();
    }

    public void onPostExecuteCommand(Long refreshKey)
    {
        listView.refreshData(refreshKey);
    }

    public ProgressBar getProgressBar()
    {
        return progressBar;
    }

    public void refresh(Long refreshKey)
    {
        listView.refreshData(refreshKey);
    }

    public interface Listener
    {
        void onHabitClick(Habit habit);
    }

    private class HabitListViewListener implements HabitListView.Listener
    {
        @Override
        public void onToggleCheckmark(Habit habit, long timestamp)
        {
            executeCommand(new ToggleRepetitionCommand(habit, timestamp), habit.getId());
        }

        @Override
        public void onHabitClick(Habit habit)
        {
            habitClickListener.onHabitClick(habit);
        }

        @Override
        public void onHabitSelectionStart()
        {
            if(actionMode == null) startActionMode();
        }

        @Override
        public void onHabitSelectionFinish()
        {
            finishActionMode();
        }

        @Override
        public void onHabitSelectionChange()
        {
            if(actionMode != null) actionMode.invalidate();
        }

        @Override
        public void onInvalidToggle()
        {
            activity.showMessage(R.string.long_press_to_toggle);
        }

        @Override
        public void onDatasetChanged()
        {
            helper.updateEmptyMessage(llEmpty);
        }
    }
}
