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

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
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

import com.mobeta.android.dslv.DragSortController;
import com.mobeta.android.dslv.DragSortListView;
import com.mobeta.android.dslv.DragSortListView.DropListener;

import org.isoron.helpers.Command;
import org.isoron.helpers.DateHelper;
import org.isoron.helpers.DialogHelper;
import org.isoron.helpers.DialogHelper.OnSavedListener;
import org.isoron.helpers.ReplayableActivity;
import org.isoron.uhabits.R;
import org.isoron.uhabits.helpers.ReminderHelper;
import org.isoron.uhabits.loaders.HabitListLoader;
import org.isoron.uhabits.models.Habit;

import java.util.Date;
import java.util.GregorianCalendar;

public class ListHabitsFragment extends Fragment
        implements OnSavedListener, OnItemClickListener, OnLongClickListener, DropListener,
        OnClickListener, HabitListLoader.Listener
{
    public static final int INACTIVE_COLOR = Color.rgb(230, 230, 230);

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

    private OnHabitClickListener habitClickListener;
    private boolean isShortToggleEnabled;

    private HabitListLoader loader;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        DisplayMetrics dm = getResources().getDisplayMetrics();
        int width = (int) (dm.widthPixels / dm.density);
        buttonCount = (int) ((width - 160) / 42.0);
        tvNameWidth = (int) ((width - 30 - buttonCount * 42) * dm.density);

        loader = new HabitListLoader();
        loader.setListener(this);
        loader.setCheckmarkCount(buttonCount);

        View view = inflater.inflate(R.layout.list_habits_fragment, container, false);
        tvNameHeader = (TextView) view.findViewById(R.id.tvNameHeader);

        ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);
        loader.setProgressBar(progressBar);

        adapter = new ListHabitsAdapter(getActivity());
        listView = (DragSortListView) view.findViewById(R.id.listView);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);
        registerForContextMenu(listView);
        listView.setDropListener(this);

        DragSortController controller = new DragSortController(listView);
        controller.setDragHandleId(R.id.tvStar);
        controller.setRemoveEnabled(false);
        controller.setSortEnabled(true);
        controller.setDragInitMode(1);

        listView.setFloatViewManager(controller);
        listView.setOnTouchListener(controller);
        listView.setDragEnabled(true);

        Typeface fontawesome = Typeface.createFromAsset(getActivity().getAssets(),
                "fontawesome-webfont.ttf");
        ((TextView) view.findViewById(R.id.tvStarEmpty)).setTypeface(fontawesome);
        llEmpty = view.findViewById(R.id.llEmpty);

        loader.updateAllHabits();
        setHasOptionsMenu(true);

        return view;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        habitClickListener = (OnHabitClickListener) activity;
        this.activity = (ReplayableActivity) activity;
    }

    @Override
    public void onResume()
    {
        super.onResume();
        Long timestamp = loader.getLastLoadTimestamp();

        if (timestamp != null && timestamp != DateHelper.getStartOfToday())
            loader.updateAllHabits();

        updateEmptyMessage();
        updateHeader();
        adapter.notifyDataSetChanged();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
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
        showArchivedItem.setChecked(Habit.isIncludeArchived());
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
                Habit.setIncludeArchived(!Habit.isIncludeArchived());
                loader.updateAllHabits();
                activity.invalidateOptionsMenu();
                return true;
            }

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem menuItem)
    {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuItem.getMenuInfo();
        final Habit habit = loader.habits.get(info.id);

        switch(menuItem.getItemId())
        {
            case R.id.action_edit_habit:
            {
                EditHabitFragment frag = EditHabitFragment.editSingleHabitFragment(habit.getId());
                frag.setOnSavedListener(this);
                frag.show(getFragmentManager(), "dialog");
                return true;
            }

            case R.id.action_archive_habit:
            {
                Command c = habit.new ArchiveCommand();
                executeCommand(c, null);
                return true;
            }

            case R.id.action_unarchive_habit:
            {
                Command c = habit.new UnarchiveCommand();
                executeCommand(c, null);
            }
        }

        return super.onContextItemSelected(menuItem);
    }

    @Override
    public void onItemClick(AdapterView parent, View view, int position, long id)
    {
        if (new Date().getTime() - lastLongClick < 1000) return;

        Habit habit = loader.positionToHabit.get(position);
        habitClickListener.onHabitClicked(habit);
    }

    @Override
    public void onSaved(Command command, Object savedObject)
    {
        Habit h = (Habit) savedObject;

        if (h == null) activity.executeCommand(command, null);
        else activity.executeCommand(command, h.getId());
        adapter.notifyDataSetChanged();

        ReminderHelper.createReminderAlarms(activity);
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

    @Override
    public void drop(int from, int to)
    {
        Habit fromHabit = loader.positionToHabit.get(from);
        Habit toHabit = loader.positionToHabit.get(to);
        loader.positionToHabit.put(to, fromHabit);
        loader.positionToHabit.put(from, toHabit);
        adapter.notifyDataSetChanged();

        Habit.reorder(from, to);
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
            return loader.positionToHabit.get(position);
        }

        @Override
        public long getItemId(int position)
        {
            return ((Habit) getItem(position)).getId();
        }

        @Override
        public View getView(int position, View view, ViewGroup parent)
        {
            final Habit habit = loader.positionToHabit.get(position);

            if (view == null ||
                    (Long) view.getTag(R.id.KEY_TIMESTAMP) != DateHelper.getStartOfToday())
            {
                view = inflater.inflate(R.layout.list_habits_item, null);
                ((TextView) view.findViewById(R.id.tvStar)).setTypeface(fontawesome);

                LinearLayout.LayoutParams params =
                        new LinearLayout.LayoutParams(tvNameWidth, LayoutParams.WRAP_CONTENT, 1);
                view.findViewById(R.id.tvName).setLayoutParams(params);

                inflateCheckmarkButtons(view);

                view.setTag(R.id.KEY_TIMESTAMP, DateHelper.getStartOfToday());
            }

            TextView tvStar = ((TextView) view.findViewById(R.id.tvStar));
            TextView tvName = (TextView) view.findViewById(R.id.tvName);
            LinearLayout llInner = (LinearLayout) view.findViewById(R.id.llInner);
            LinearLayout llButtons = (LinearLayout) view.findViewById(R.id.llButtons);

            llInner.setTag(R.string.habit_key, habit.getId());

            updateNameAndIcon(habit, tvStar, tvName);
            updateCheckmarkButtons(habit, llButtons);

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
        int isChecked[] = loader.checkmarks.get(habit.getId());

        for (int i = 0; i < m; i++)
        {

            TextView tvCheck = (TextView) llButtons.getChildAt(i);
            tvCheck.setTag(R.string.habit_key, habit.getId());
            tvCheck.setTag(R.string.offset_key, i);
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
                tvCheck.setTextColor(INACTIVE_COLOR);
                tvCheck.setTag(R.string.toggle_key, 1);
                break;

            case 0:
                tvCheck.setText(R.string.fa_times);
                tvCheck.setTextColor(INACTIVE_COLOR);
                tvCheck.setTag(R.string.toggle_key, 0);
                break;
        }
    }

    public void onPostExecuteCommand(Long refreshKey)
    {
        if (refreshKey == null) loader.updateAllHabits();
        else loader.updateHabit(refreshKey);
    }
}
