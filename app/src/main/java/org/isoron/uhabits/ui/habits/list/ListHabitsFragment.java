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
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.mobeta.android.dslv.DragSortController;
import com.mobeta.android.dslv.DragSortListView;

import org.isoron.uhabits.utils.Preferences;
import org.isoron.uhabits.R;
import org.isoron.uhabits.commands.Command;
import org.isoron.uhabits.commands.ToggleRepetitionCommand;
import org.isoron.uhabits.models.Habit;
import org.isoron.uhabits.ui.BaseActivity;
import org.isoron.uhabits.ui.HintManager;
import org.isoron.uhabits.ui.habits.edit.EditHabitDialogFragment;
import org.isoron.uhabits.utils.DateUtils;
import org.isoron.uhabits.utils.InterfaceUtils;
import org.isoron.uhabits.utils.InterfaceUtils.OnSavedListener;
import org.isoron.uhabits.utils.ReminderUtils;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class ListHabitsFragment extends Fragment
        implements OnSavedListener, OnItemClickListener, OnLongClickListener,
        OnClickListener, ListHabitsLoader.Listener, AdapterView.OnItemLongClickListener,
        HabitSelectionCallback.Listener, ListHabitsController.Screen
{
    long lastLongClick = 0;
    private boolean showArchived;

    private ActionMode actionMode;
    private ListHabitsAdapter adapter;
    public ListHabitsLoader loader;
    private HintManager hintManager;
    private ListHabitsHelper helper;
    private List<Integer> selectedPositions;
    private OnHabitClickListener habitClickListener;
    private BaseActivity activity;

    private DragSortListView listView;
    private LinearLayout llButtonsHeader;
    public ProgressBar progressBar;
    private View llEmpty;

    private ListHabitsController controller;
    private Preferences prefs;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.list_habits_fragment, container, false);

        View llHint = view.findViewById(R.id.llHint);
        llButtonsHeader = (LinearLayout) view.findViewById(R.id.llButtonsHeader);
        llEmpty = view.findViewById(R.id.llEmpty);
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);

        selectedPositions = new LinkedList<>();
        loader = new ListHabitsLoader();
        helper = new ListHabitsHelper(activity, loader);

        hintManager = new HintManager(activity, llHint);

        loader.setListener(this);
        loader.setCheckmarkCount(helper.getButtonCount());

        llHint.setOnClickListener(this);

        TextView tvStarEmpty = (TextView) view.findViewById(R.id.tvStarEmpty);
        tvStarEmpty.setTypeface(InterfaceUtils.getFontAwesome(activity));

        createListView(view);

        if(savedInstanceState != null)
        {
            EditHabitDialogFragment frag = (EditHabitDialogFragment) getFragmentManager()
                    .findFragmentByTag("editHabit");
            if(frag != null) frag.setOnSavedListener(this);
        }

        loader.updateAllHabits(true);

        controller = new ListHabitsController();
        controller.setScreen(this);
        prefs = Preferences.getInstance();

        setHasOptionsMenu(true);
        return view;
    }

    private void createListView(View view)
    {
        listView = (DragSortListView) view.findViewById(R.id.listView);
        adapter = new ListHabitsAdapter(getActivity(), loader);
        adapter.setSelectedPositions(selectedPositions);
        adapter.setOnCheckmarkClickListener(this);
        adapter.setOnCheckmarkLongClickListener(this);

        DragSortListView.DragListener dragListener = new HabitsDragListener();
        DragSortController dragSortController = new HabitsDragSortController();

        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);
        listView.setOnItemLongClickListener(this);
        listView.setDropListener(new HabitsDropListener());
        listView.setDragListener(dragListener);
        listView.setFloatViewManager(dragSortController);
        listView.setDragEnabled(true);
        listView.setLongClickable(true);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        this.activity = (BaseActivity) activity;
        habitClickListener = (OnHabitClickListener) activity;
    }

    @Override
    public void onResume()
    {
        super.onResume();
        Long timestamp = loader.getLastLoadTimestamp();

        if (timestamp != null && timestamp != DateUtils.getStartOfToday())
            loader.updateAllHabits(true);

        helper.updateEmptyMessage(llEmpty);
        helper.updateHeader(llButtonsHeader);
        hintManager.showHintIfAppropriate();
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onLoadFinished()
    {
        adapter.notifyDataSetChanged();
        helper.updateEmptyMessage(llEmpty);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.list_habits_options, menu);
        MenuItem showArchivedItem = menu.findItem(R.id.action_show_archived);
        showArchivedItem.setChecked(showArchived);
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
        showArchived = !showArchived;
        loader.setIncludeArchived(showArchived);
        loader.updateAllHabits(true);
        activity.invalidateOptionsMenu();
    }

    private void showCreateHabitScreen()
    {
        EditHabitDialogFragment frag = EditHabitDialogFragment.createHabitFragment();
        frag.setOnSavedListener(this);
        frag.show(getFragmentManager(), "editHabit");
    }

    @Override
    public void onItemClick(AdapterView parent, View view, int position, long id)
    {
        if (new Date().getTime() - lastLongClick < 1000) return;

        if(actionMode == null)
        {
            Habit habit = loader.habitsList.get(position);
            habitClickListener.onHabitClicked(habit);
        }
        else
        {
            toggleItemSelected(position);
            adapter.notifyDataSetChanged();
        }
    }

    private void toggleItemSelected(int position)
    {
        int k = selectedPositions.indexOf(position);
        if(k < 0) selectedPositions.add(position);
        else selectedPositions.remove(k);

        if(selectedPositions.isEmpty()) actionMode.finish();
        else actionMode.invalidate();
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
    {
        selectHabit(position);
        return true;
    }

    private void selectHabit(int position)
    {
        if(!selectedPositions.contains(position)) selectedPositions.add(position);
        adapter.notifyDataSetChanged();
        if(actionMode == null) startSupportActionMode();
        actionMode.invalidate();
    }

    private void startSupportActionMode()
    {
        HabitSelectionCallback callback = new HabitSelectionCallback(activity, loader);
        callback.setSelectedPositions(selectedPositions);
        callback.setOnSavedListener(this);
        callback.setListener(this);
        actionMode = activity.startSupportActionMode(callback);
    }

    @Override
    public void onSaved(Command command, Object savedObject)
    {
        Habit h = (Habit) savedObject;

        if (h == null) activity.executeCommand(command, null);
        else activity.executeCommand(command, h.getId());
        adapter.notifyDataSetChanged();

        ReminderUtils.createReminderAlarms(activity);

        if(actionMode != null) actionMode.finish();
    }

    @Override
    public boolean onLongClick(View v)
    {
        lastLongClick = new Date().getTime();

        switch (v.getId())
        {
            case R.id.tvCheck:
                onCheckmarkLongClick(v);
                return true;
        }

        return false;
    }

    private void onCheckmarkLongClick(View v)
    {
        if (prefs.isShortToggleEnabled()) return;
        toggleCheckmark(v);
    }

    private void toggleCheckmark(View v)
    {
        Long id = helper.getHabitIdFromCheckmarkView(v);
        Habit habit = loader.habits.get(id);
        if(habit == null) return;

        float x = v.getX() + v.getWidth() / 2.0f + ((View) v.getParent()).getX();
        float y = v.getY() + v.getHeight() / 2.0f + ((View) v.getParent()).getY();
        helper.triggerRipple((View) v.getParent().getParent(), x, y);

        listView.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
        helper.toggleCheckmarkView(v, habit);

        long timestamp = helper.getTimestampFromCheckmarkView(v);
        executeCommand(new ToggleRepetitionCommand(habit, timestamp), habit.getId());
    }

    private void executeCommand(Command c, Long refreshKey)
    {
        activity.executeCommand(c, refreshKey);
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.tvCheck:
                if (prefs.isShortToggleEnabled()) toggleCheckmark(v);
                else activity.showMessage(R.string.long_press_to_toggle);
                break;

            case R.id.llHint:
                hintManager.dismissHint();
                break;
        }
    }

    public void onPostExecuteCommand(Long refreshKey)
    {
        if (refreshKey == null) loader.updateAllHabits(true);
        else loader.updateHabit(refreshKey);
    }

    @Override
    public void onActionModeDestroyed(ActionMode mode)
    {
        actionMode = null;
        selectedPositions.clear();
        adapter.notifyDataSetChanged();
        listView.setDragEnabled(true);
    }

    public interface OnHabitClickListener
    {
        void onHabitClicked(Habit habit);
    }

    private class HabitsDragSortController extends DragSortController
    {
        public HabitsDragSortController()
        {
            super(ListHabitsFragment.this.listView);
            setRemoveEnabled(false);
        }

        @Override
        public View onCreateFloatView(int position)
        {
            return adapter.getView(position, null, null);
        }

        @Override
        public void onDestroyFloatView(View floatView)
        {
        }
    }

    private class HabitsDropListener implements DragSortListView.DropListener
    {
        @Override
        public void drop(int from, int to)
        {
            if(from == to) return;
            if(actionMode != null) actionMode.finish();

            loader.reorder(from, to);
            adapter.notifyDataSetChanged();
            loader.updateAllHabits(false);
        }
    }

    private class HabitsDragListener implements DragSortListView.DragListener
    {
        @Override
        public void drag(int from, int to)
        {
        }

        @Override
        public void startDrag(int position)
        {
            selectHabit(position);
        }
    }
}
