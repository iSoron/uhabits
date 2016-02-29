/* Copyright (C) 2016 Alinson Santos Xavier
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied  warranty of MERCHANTABILITY or
 * FITNESS  FOR  A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You  should  have  received  a  copy  of the GNU General Public License
 * along  with  this  program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.isoron.uhabits.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
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
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.colorpicker.ColorPickerDialog;
import com.android.colorpicker.ColorPickerSwatch;
import com.mobeta.android.dslv.DragSortController;
import com.mobeta.android.dslv.DragSortListView;
import com.mobeta.android.dslv.DragSortListView.DropListener;

import org.isoron.helpers.ColorHelper;
import org.isoron.helpers.Command;
import org.isoron.helpers.DateHelper;
import org.isoron.helpers.DialogHelper;
import org.isoron.helpers.DialogHelper.OnSavedListener;
import org.isoron.helpers.ReplayableActivity;
import org.isoron.uhabits.R;
import org.isoron.uhabits.commands.ArchiveHabitsCommand;
import org.isoron.uhabits.commands.ChangeHabitColorCommand;
import org.isoron.uhabits.commands.DeleteHabitsCommand;
import org.isoron.uhabits.commands.UnarchiveHabitsCommand;
import org.isoron.uhabits.helpers.ReminderHelper;
import org.isoron.uhabits.io.CSVExporter;
import org.isoron.uhabits.loaders.HabitListLoader;
import org.isoron.uhabits.models.Habit;

import java.io.File;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;

public class ListHabitsFragment extends Fragment
        implements OnSavedListener, OnItemClickListener, OnLongClickListener, DropListener,
        OnClickListener, HabitListLoader.Listener, AdapterView.OnItemLongClickListener
{
    private class ListHabitsActionBarCallback implements ActionMode.Callback
    {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu)
        {
            getActivity().getMenuInflater().inflate(R.menu.list_habits_context, menu);
            updateTitle(mode);
            updateActions(menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu)
        {
            updateTitle(mode);
            updateActions(menu);
            return true;
        }

        private void updateActions(Menu menu)
        {
            boolean showEdit = (selectedPositions.size() == 1);
            boolean showColor = true;
            boolean showArchive = true;
            boolean showUnarchive = true;

            if(showEdit) showColor = false;
            for(int i : selectedPositions)
            {
                Habit h = loader.habitsList.get(i);
                if(h.isArchived())
                {
                    showColor = false;
                    showArchive = false;
                }
                else showUnarchive = false;
            }

            MenuItem itemEdit = menu.findItem(R.id.action_edit_habit);
            MenuItem itemColor = menu.findItem(R.id.action_color);
            MenuItem itemArchive = menu.findItem(R.id.action_archive_habit);
            MenuItem itemUnarchive = menu.findItem(R.id.action_unarchive_habit);

            itemEdit.setVisible(showEdit);
            itemColor.setVisible(showColor);
            itemArchive.setVisible(showArchive);
            itemUnarchive.setVisible(showUnarchive);
        }

        private void updateTitle(ActionMode mode)
        {
            mode.setTitle("" + selectedPositions.size());
        }

        @Override
        public boolean onActionItemClicked(final ActionMode mode, MenuItem item)
        {
            final LinkedList<Habit> selectedHabits = new LinkedList<>();
            for(int i : selectedPositions)
                selectedHabits.add(loader.habitsList.get(i));

            Habit firstHabit = selectedHabits.getFirst();

            switch(item.getItemId())
            {
                case R.id.action_archive_habit:
                    executeCommand(new ArchiveHabitsCommand(selectedHabits), null);
                    mode.finish();
                    return true;

                case R.id.action_unarchive_habit:
                    executeCommand(new UnarchiveHabitsCommand(selectedHabits), null);
                    mode.finish();
                    return true;

                case R.id.action_edit_habit:
                {
                    EditHabitFragment frag = EditHabitFragment.editSingleHabitFragment(firstHabit.getId());
                    frag.setOnSavedListener(ListHabitsFragment.this);
                    frag.show(getFragmentManager(), "dialog");
                    return true;
                }

                case R.id.action_color:
                {
                    ColorPickerDialog picker = ColorPickerDialog.newInstance(
                            R.string.color_picker_default_title, ColorHelper.palette,
                            firstHabit.color, 4, ColorPickerDialog.SIZE_SMALL);

                    picker.setOnColorSelectedListener(new ColorPickerSwatch.OnColorSelectedListener()
                    {
                        public void onColorSelected(int color)
                        {
                            executeCommand(new ChangeHabitColorCommand(selectedHabits, color), null);
                            mode.finish();
                        }
                    });
                    picker.show(getFragmentManager(), "picker");
                    return true;
                }

                case R.id.action_delete:
                {
                    new AlertDialog.Builder(activity)
                            .setTitle(R.string.delete_habits)
                            .setMessage(R.string.delete_habits_message)
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener()
                            {
                                @Override
                                public void onClick(DialogInterface dialog, int which)
                                {
                                    executeCommand(new DeleteHabitsCommand(selectedHabits), null);
                                    mode.finish();
                                }
                            }).setNegativeButton(android.R.string.no, null)
                            .show();

                    return true;
                }

                case R.id.action_export_csv:
                {
                    onExportHabitsClick(selectedHabits);
                    return true;
                }
            }

            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode)
        {
            actionMode = null;

            selectedPositions.clear();
            adapter.notifyDataSetChanged();

            listView.setDragEnabled(true);
        }
    }

    public static final int INACTIVE_COLOR = Color.rgb(200, 200, 200);
    public static final int INACTIVE_CHECKMARK_COLOR = Color.rgb(230, 230, 230);

    public static final int HINT_INTERVAL = 5;
    public static final int HINT_INTERVAL_OFFSET = 2;

    public interface OnHabitClickListener
    {
        void onHabitClicked(Habit habit);
    }

    ListHabitsAdapter adapter;
    DragSortListView listView;
    ReplayableActivity activity;
    TextView tvNameHeader;
    long lastLongClick = 0;

    private int tvNameWidth;
    private int buttonCount;
    private View llEmpty;
    private View llHint;

    private OnHabitClickListener habitClickListener;
    private boolean isShortToggleEnabled;

    private HabitListLoader loader;
    private boolean showArchived;
    private SharedPreferences prefs;

    private ActionMode actionMode;
    private List<Integer> selectedPositions;
    private DragSortController dragSortController;
    private ProgressBar progressBar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        DisplayMetrics dm = getResources().getDisplayMetrics();
        int width = (int) (dm.widthPixels / dm.density);
        buttonCount = Math.max(0, (int) ((width - 160) / 42.0));
        tvNameWidth = (int) ((width - 30 - buttonCount * 42) * dm.density);

        loader = new HabitListLoader();
        loader.setListener(this);
        loader.setCheckmarkCount(buttonCount);

        View view = inflater.inflate(R.layout.list_habits_fragment, container, false);
        tvNameHeader = (TextView) view.findViewById(R.id.tvNameHeader);

        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        loader.setProgressBar(progressBar);

        adapter = new ListHabitsAdapter(getActivity());
        listView = (DragSortListView) view.findViewById(R.id.listView);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);
        listView.setOnItemLongClickListener(this);
        listView.setDropListener(this);
        listView.setDragListener(new DragSortListView.DragListener()
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
        });

        dragSortController = new DragSortController(listView) {
                @Override
                public View onCreateFloatView(int position)
                {
                     return adapter.getView(position, null, null);
                }

            @Override
            public void onDestroyFloatView(View floatView)
            {
            }
        };
        dragSortController.setRemoveEnabled(false);

        listView.setFloatViewManager(dragSortController);
        listView.setDragEnabled(true);
        listView.setLongClickable(true);

        llHint = view.findViewById(R.id.llHint);
        llHint.setOnClickListener(this);

        Typeface fontawesome = Typeface.createFromAsset(getActivity().getAssets(),
                "fontawesome-webfont.ttf");
        ((TextView) view.findViewById(R.id.tvStarEmpty)).setTypeface(fontawesome);
        llEmpty = view.findViewById(R.id.llEmpty);

        loader.updateAllHabits(true);
        setHasOptionsMenu(true);

        selectedPositions = new LinkedList<>();

        return view;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        this.activity = (ReplayableActivity) activity;

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

        updateEmptyMessage();
        updateHeader();
        showNextHint();

        adapter.notifyDataSetChanged();
        isShortToggleEnabled = prefs.getBoolean("pref_short_toggle", false);
    }

    private void updateHeader()
    {
        LayoutInflater inflater = activity.getLayoutInflater();
        View view = getView();

        if (view == null) return;

        GregorianCalendar day = DateHelper.getStartOfTodayCalendar();

        LinearLayout llButtonsHeader = (LinearLayout) view.findViewById(R.id.llButtonsHeader);
        llButtonsHeader.removeAllViews();

        for (int i = 0; i < buttonCount; i++)
        {
            View tvDay = inflater.inflate(R.layout.list_habits_header_check, null);
            Button btCheck = (Button) tvDay.findViewById(R.id.tvCheck);
            btCheck.setText(DateHelper.formatHeaderDate(day));
            llButtonsHeader.addView(tvDay);

            day.add(GregorianCalendar.DAY_OF_MONTH, -1);
        }
    }

    @Override
    public void onLoadFinished()
    {
        adapter.notifyDataSetChanged();
        updateEmptyMessage();
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
                EditHabitFragment frag = EditHabitFragment.createHabitFragment();
                frag.setOnSavedListener(this);
                frag.show(getFragmentManager(), "dialog");
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
            actionMode = getActivity().startActionMode(new ListHabitsActionBarCallback());
//            listView.setDragEnabled(false);
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

    private void updateEmptyMessage()
    {
        if (loader.getLastLoadTimestamp() == null) llEmpty.setVisibility(View.GONE);
        else llEmpty.setVisibility(loader.habits.size() > 0 ? View.GONE : View.VISIBLE);
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
        DialogHelper.vibrate(activity, 100);
    }

    private void toggleCheck(View v)
    {
        Long tag = (Long) v.getTag(R.string.habit_key);
        Habit habit = loader.habits.get(tag);

        int offset = (Integer) v.getTag(R.string.offset_key);
        long timestamp = DateHelper.getStartOfDay(
                DateHelper.getLocalTime() - offset * DateHelper.millisecondsInOneDay);

        if (v.getTag(R.string.toggle_key).equals(2)) updateCheckmark(habit.color, (TextView) v, 0);
        else updateCheckmark(habit.color, (TextView) v, 2);

        executeCommand(habit.new ToggleRepetitionCommand(timestamp), habit.getId());
    }

    private void executeCommand(Command c, Long refreshKey)
    {
        activity.executeCommand(c, refreshKey);
    }

    private void hideHint()
    {
        llHint.animate().alpha(0f).setDuration(500).setListener(new AnimatorListenerAdapter()
        {
            @Override
            public void onAnimationEnd(Animator animation)
            {
                llHint.setVisibility(View.GONE);
            }
        });
    }

    private void showNextHint()
    {
        Integer lastHintNumber = prefs.getInt("last_hint_number", -1);
        Long lastHintTimestamp = prefs.getLong("last_hint_timestamp", -1);

        if(DateHelper.getStartOfToday() > lastHintTimestamp)
            showHint(lastHintNumber + 1);
    }

    private void showHint(int hintNumber)
    {
        String[] hints = activity.getResources().getStringArray(R.array.hints);
        if(hintNumber >= hints.length) return;

        prefs.edit().putInt("last_hint_number", hintNumber).apply();
        prefs.edit().putLong("last_hint_timestamp", DateHelper.getStartOfToday()).apply();

        TextView tvContent = (TextView) llHint.findViewById(R.id.hintContent);
        tvContent.setText(hints[hintNumber]);

        llHint.setAlpha(0.0f);
        llHint.setVisibility(View.VISIBLE);
        llHint.animate().alpha(1f).setDuration(500);
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
                hideHint();
                break;
        }
    }

    class ListHabitsAdapter extends BaseAdapter
    {
        private LayoutInflater inflater;
        private Typeface fontawesome;

        public ListHabitsAdapter(Context context)
        {

            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            fontawesome = Typeface.createFromAsset(context.getAssets(), "fontawesome-webfont.ttf");
        }

        @Override
        public int getCount()
        {
            return loader.habits.size();
        }

        @Override
        public Object getItem(int position)
        {
            return loader.habitsList.get(position);
        }

        @Override
        public long getItemId(int position)
        {
            return ((Habit) getItem(position)).getId();
        }

        @Override
        public View getView(int position, View view, ViewGroup parent)
        {
            final Habit habit = loader.habitsList.get(position);

            if (view == null ||
                    (Long) view.getTag(R.id.timestamp_key) != DateHelper.getStartOfToday())
            {
                view = inflater.inflate(R.layout.list_habits_item, null);
                ((TextView) view.findViewById(R.id.tvStar)).setTypeface(fontawesome);

                LinearLayout.LayoutParams params =
                        new LinearLayout.LayoutParams(tvNameWidth, LayoutParams.WRAP_CONTENT, 1);
                view.findViewById(R.id.label).setLayoutParams(params);

                inflateCheckmarkButtons(view);

                view.setTag(R.id.timestamp_key, DateHelper.getStartOfToday());
            }

            TextView tvStar = ((TextView) view.findViewById(R.id.tvStar));
            TextView tvName = (TextView) view.findViewById(R.id.label);
            LinearLayout llInner = (LinearLayout) view.findViewById(R.id.llInner);
            LinearLayout llButtons = (LinearLayout) view.findViewById(R.id.llButtons);

            llInner.setTag(R.string.habit_key, habit.getId());

            updateNameAndIcon(habit, tvStar, tvName);
            updateCheckmarkButtons(habit, llButtons);

            boolean selected = selectedPositions.contains(position);
            if(selected)
                llInner.setBackgroundResource(R.drawable.selected_box);
            else
            {
                if (android.os.Build.VERSION.SDK_INT >= 21)
                    llInner.setBackgroundResource(R.drawable.ripple_white);
                else
                    llInner.setBackgroundColor(Color.WHITE);
            }

            return view;
        }

        private void inflateCheckmarkButtons(View view)
        {
            for (int i = 0; i < buttonCount; i++)
            {
                View check = inflater.inflate(R.layout.list_habits_item_check, null);
                TextView btCheck = (TextView) check.findViewById(R.id.tvCheck);
                btCheck.setTypeface(fontawesome);
                btCheck.setOnLongClickListener(ListHabitsFragment.this);
                btCheck.setOnClickListener(ListHabitsFragment.this);
                ((LinearLayout) view.findViewById(R.id.llButtons)).addView(check);
            }
        }
    }

    private void updateCheckmarkButtons(Habit habit, LinearLayout llButtons)
    {
        int activeColor = getActiveColor(habit);
        int m = llButtons.getChildCount();
        Long habitId = habit.getId();

        int isChecked[] = loader.checkmarks.get(habitId);

        for (int i = 0; i < m; i++)
        {

            TextView tvCheck = (TextView) llButtons.getChildAt(i);
            tvCheck.setTag(R.string.habit_key, habitId);
            tvCheck.setTag(R.string.offset_key, i);
            if(isChecked.length > i)
                updateCheckmark(activeColor, tvCheck, isChecked[i]);
        }
    }

    private void  updateNameAndIcon(Habit habit, TextView tvStar, TextView tvName)
    {
        int activeColor = getActiveColor(habit);

        tvName.setText(habit.name);
        tvName.setTextColor(activeColor);

        if (habit.isArchived())
        {
            tvStar.setText(getString(R.string.fa_archive));
            tvStar.setTextColor(activeColor);
        }
        else
        {
            int score = loader.scores.get(habit.getId());

            if (score < Habit.HALF_STAR_CUTOFF)
            {
                tvStar.setText(getString(R.string.fa_star_o));
                tvStar.setTextColor(INACTIVE_COLOR);
            }
            else if (score < Habit.FULL_STAR_CUTOFF)
            {
                tvStar.setText(getString(R.string.fa_star_half_o));
                tvStar.setTextColor(INACTIVE_COLOR);
            }
            else
            {
                tvStar.setText(getString(R.string.fa_star));
                tvStar.setTextColor(activeColor);
            }
        }
    }

    private int getActiveColor(Habit habit)
    {
        int activeColor = habit.color;
        if(habit.isArchived()) activeColor = INACTIVE_COLOR;

        return activeColor;
    }

    private void updateCheckmark(int activeColor, TextView tvCheck, int check)
    {
        switch (check)
        {
            case 2:
                tvCheck.setText(R.string.fa_check);
                tvCheck.setTextColor(activeColor);
                tvCheck.setTag(R.string.toggle_key, 2);
                break;

            case 1:
                tvCheck.setText(R.string.fa_check);
                tvCheck.setTextColor(INACTIVE_CHECKMARK_COLOR);
                tvCheck.setTag(R.string.toggle_key, 1);
                break;

            case 0:
                tvCheck.setText(R.string.fa_times);
                tvCheck.setTextColor(INACTIVE_CHECKMARK_COLOR);
                tvCheck.setTag(R.string.toggle_key, 0);
                break;
        }
    }

    public void onPostExecuteCommand(Long refreshKey)
    {
        if (refreshKey == null) loader.updateAllHabits(true);
        else loader.updateHabit(refreshKey);
    }

    private void onExportHabitsClick(final LinkedList<Habit> selectedHabits)
    {
        new AsyncTask<Void, Void, Void>()
        {
            String filename;

            @Override
            protected void onPreExecute()
            {
                progressBar.setIndeterminate(true);
                progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            protected void onPostExecute(Void aVoid)
            {
                if(filename != null)
                {
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_SEND);
                    intent.setType("application/zip");
                    intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(filename)));

                    startActivity(intent);
                }

                progressBar.setVisibility(View.GONE);
            }

            @Override
            protected Void doInBackground(Void... params)
            {
                CSVExporter exporter = new CSVExporter(activity, selectedHabits);
                filename = exporter.writeArchive();
                return null;
            }
        }.execute();
    }
}
