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

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.view.ActionMode;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
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
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.mobeta.android.dslv.DragSortController;
import com.mobeta.android.dslv.DragSortListView;
import com.mobeta.android.dslv.DragSortListView.DropListener;

import org.isoron.uhabits.BaseActivity;
import org.isoron.uhabits.R;
import org.isoron.uhabits.commands.Command;
import org.isoron.uhabits.commands.ToggleRepetitionCommand;
import org.isoron.uhabits.dialogs.EditHabitDialogFragment;
import org.isoron.uhabits.dialogs.FilePickerDialog;
import org.isoron.uhabits.helpers.DatabaseHelper;
import org.isoron.uhabits.helpers.DateHelper;
import org.isoron.uhabits.helpers.HintManager;
import org.isoron.uhabits.helpers.ListHabitsHelper;
import org.isoron.uhabits.helpers.ReminderHelper;
import org.isoron.uhabits.helpers.UIHelper.OnSavedListener;
import org.isoron.uhabits.loaders.HabitListLoader;
import org.isoron.uhabits.models.Habit;
import org.isoron.uhabits.tasks.ExportCSVTask;
import org.isoron.uhabits.tasks.ExportDBTask;
import org.isoron.uhabits.tasks.ImportDataTask;

import java.io.File;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class ListHabitsFragment extends Fragment
        implements OnSavedListener, OnItemClickListener, OnLongClickListener, DropListener,
        OnClickListener, HabitListLoader.Listener, AdapterView.OnItemLongClickListener,
        HabitSelectionCallback.Listener, ImportDataTask.Listener, ExportCSVTask.Listener,
        ExportDBTask.Listener
{
    long lastLongClick = 0;
    private boolean isShortToggleEnabled;
    private boolean showArchived;

    private ActionMode actionMode;
    private HabitListAdapter adapter;
    private HabitListLoader loader;
    private HintManager hintManager;
    private ListHabitsHelper helper;
    private List<Integer> selectedPositions;
    private OnHabitClickListener habitClickListener;
    private BaseActivity activity;
    private SharedPreferences prefs;

    private DragSortListView listView;
    private LinearLayout llButtonsHeader;
    private ProgressBar progressBar;
    private View llEmpty;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.list_habits_fragment, container, false);
        View llHint = view.findViewById(R.id.llHint);
        TextView tvStarEmpty = (TextView) view.findViewById(R.id.tvStarEmpty);
        listView = (DragSortListView) view.findViewById(R.id.listView);
        llButtonsHeader = (LinearLayout) view.findViewById(R.id.llButtonsHeader);
        llEmpty = view.findViewById(R.id.llEmpty);

        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);

        selectedPositions = new LinkedList<>();
        loader = new HabitListLoader();
        helper = new ListHabitsHelper(activity, loader);
        hintManager = new HintManager(activity, llHint);

        loader.setListener(this);
        loader.setCheckmarkCount(helper.getButtonCount());

        llHint.setOnClickListener(this);
        tvStarEmpty.setTypeface(helper.getFontawesome());

        adapter = new HabitListAdapter(getActivity(), loader);
        adapter.setSelectedPositions(selectedPositions);
        adapter.setOnCheckmarkClickListener(this);
        adapter.setOnCheckmarkLongClickListener(this);

        DragSortListView.DragListener dragListener = new HabitsDragListener();
        DragSortController dragSortController = new HabitsDragSortController();

        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);
        listView.setOnItemLongClickListener(this);
        listView.setDropListener(this);
        listView.setDragListener(dragListener);
        listView.setFloatViewManager(dragSortController);
        listView.setDragEnabled(true);
        listView.setLongClickable(true);

        if(savedInstanceState != null)
        {
            EditHabitDialogFragment frag = (EditHabitDialogFragment) getFragmentManager()
                    .findFragmentByTag("editHabit");
            if(frag != null) frag.setOnSavedListener(this);
        }

        loader.updateAllHabits(true);

        setHasOptionsMenu(true);
        return view;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        this.activity = (BaseActivity) activity;

        habitClickListener = (OnHabitClickListener) activity;
        prefs = PreferenceManager.getDefaultSharedPreferences(activity);
    }

    @Override
    public void onResume()
    {
        super.onResume();
        Long timestamp = loader.getLastLoadTimestamp();

        if (timestamp != null && timestamp != DateHelper.getStartOfToday())
            loader.updateAllHabits(true);

        helper.updateEmptyMessage(llEmpty);
        helper.updateHeader(llButtonsHeader);
        hintManager.showHintIfAppropriate();

        adapter.notifyDataSetChanged();
        isShortToggleEnabled = prefs.getBoolean("pref_short_toggle", false);
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
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo)
    {
        super.onCreateContextMenu(menu, view, menuInfo);
        getActivity().getMenuInflater().inflate(R.menu.list_habits_context, menu);

        AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
        final Habit habit = loader.habits.get(info.id);

        if (habit.isArchived()) menu.findItem(R.id.action_archive_habit).setVisible(false);
        else menu.findItem(R.id.action_unarchive_habit).setVisible(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.action_add:
            {
                EditHabitDialogFragment frag = EditHabitDialogFragment.createHabitFragment();
                frag.setOnSavedListener(this);
                frag.show(getFragmentManager(), "editHabit");
                return true;
            }

            case R.id.action_show_archived:
            {
                showArchived = !showArchived;
                loader.setIncludeArchived(showArchived);
                loader.updateAllHabits(true);
                activity.invalidateOptionsMenu();
                return true;
            }

            default:
                return super.onOptionsItemSelected(item);
        }
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
            int k = selectedPositions.indexOf(position);
            if(k < 0)
                selectedPositions.add(position);
            else
                selectedPositions.remove(k);

            if(selectedPositions.isEmpty()) actionMode.finish();
            else actionMode.invalidate();

            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
    {
        selectItem(position);
        return true;
    }

    private void selectItem(int position)
    {
        if(!selectedPositions.contains(position))
            selectedPositions.add(position);

        adapter.notifyDataSetChanged();

        if(actionMode == null)
        {
            HabitSelectionCallback callback = new HabitSelectionCallback(activity, loader);
            callback.setSelectedPositions(selectedPositions);
            callback.setProgressBar(progressBar);
            callback.setOnSavedListener(this);
            callback.setListener(this);

            actionMode = activity.startSupportActionMode(callback);
        }

        if(actionMode != null) actionMode.invalidate();
    }

    @Override
    public void onSaved(Command command, Object savedObject)
    {
        Habit h = (Habit) savedObject;

        if (h == null) activity.executeCommand(command, null);
        else activity.executeCommand(command, h.getId());
        adapter.notifyDataSetChanged();

        ReminderHelper.createReminderAlarms(activity);

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
        if (isShortToggleEnabled) return;

        toggleCheck(v);
    }

    private void toggleCheck(View v)
    {
        Long tag = (Long) v.getTag(R.string.habit_key);
        Integer offset = (Integer) v.getTag(R.string.offset_key);
        long timestamp = DateHelper.getStartOfDay(
                DateHelper.getLocalTime() - offset * DateHelper.millisecondsInOneDay);

        Habit habit = loader.habits.get(tag);
        if(habit == null) return;

        listView.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);

        helper.toggleCheckmarkView(v, habit);
        executeCommand(new ToggleRepetitionCommand(habit, timestamp), habit.getId());
    }

    private void executeCommand(Command c, Long refreshKey)
    {
        activity.executeCommand(c, refreshKey);
    }

    @Override
    public void drop(int from, int to)
    {
        if(from == to) return;
        if(actionMode != null) actionMode.finish();

        loader.reorder(from, to);
        adapter.notifyDataSetChanged();
        loader.updateAllHabits(false);
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.tvCheck:
                if (isShortToggleEnabled) toggleCheck(v);
                else activity.showToast(R.string.long_press_to_toggle);
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

    private class HabitsDragListener implements DragSortListView.DragListener
    {
        @Override
        public void drag(int from, int to)
        {
        }

        @Override
        public void startDrag(int position)
        {
            selectItem(position);
        }
    }

    public void showImportDialog()
    {
        File dir = DatabaseHelper.getFilesDir(null);
        if(dir == null)
        {
            activity.showToast(R.string.could_not_import);
            return;
        }

        FilePickerDialog picker = new FilePickerDialog(activity, dir);
        picker.setListener(new FilePickerDialog.OnFileSelectedListener()
        {
            @Override
            public void onFileSelected(File file)
            {
                ImportDataTask task = new ImportDataTask(file, progressBar);
                task.setListener(ListHabitsFragment.this);
                task.execute();
            }
        });

        picker.show();
    }

    @Override
    public void onImportFinished(int result)
    {
        switch (result)
        {
            case ImportDataTask.SUCCESS:
                loader.updateAllHabits(true);
                activity.showToast(R.string.habits_imported);
                break;

            case ImportDataTask.NOT_RECOGNIZED:
                activity.showToast(R.string.file_not_recognized);
                break;

            default:
                activity.showToast(R.string.could_not_import);
                break;
        }
    }

    public void exportAllHabits()
    {
        ExportCSVTask task = new ExportCSVTask(Habit.getAll(true), progressBar);
        task.setListener(this);
        task.execute();
    }

    @Override
    public void onExportCSVFinished(@Nullable String archiveFilename)
    {
        if(archiveFilename != null)
        {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_SEND);
            intent.setType("application/zip");
            intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(archiveFilename)));
            activity.startActivity(intent);
        }
        else
        {
            activity.showToast(R.string.could_not_export);
        }
    }

    public void exportDB()
    {
        ExportDBTask task = new ExportDBTask(progressBar);
        task.setListener(this);
        task.execute();
    }

    @Override
    public void onExportDBFinished(@Nullable String filename)
    {
        if(filename != null)
        {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_SEND);
            intent.setType("application/octet-stream");
            intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(filename)));
            activity.startActivity(intent);
        }
        else
        {
            activity.showToast(R.string.could_not_export);
        }
    }
}
