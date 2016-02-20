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

package org.isoron.uhabits.dialogs;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
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

import org.isoron.helpers.ColorHelper;
import org.isoron.helpers.Command;
import org.isoron.helpers.DateHelper;
import org.isoron.helpers.DialogHelper.OnSavedListener;
import org.isoron.helpers.ReplayableActivity;
import org.isoron.uhabits.R;
import org.isoron.uhabits.ReminderHelper;
import org.isoron.uhabits.models.Habit;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;

public class ListHabitsFragment extends Fragment
        implements OnSavedListener, OnItemClickListener, OnLongClickListener, DropListener,
        OnClickListener
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
    private int button_count;
    private View llEmpty;
    private ProgressBar progressBar;

    private OnHabitClickListener habitClickListener;
    private boolean short_toggle_enabled;

    private HashMap<Long, Habit> habits;
    private HashMap<Integer, Habit> positionToHabit;
    private HashMap<Long, int[]> checkmarks;
    private HashMap<Long, Integer> scores;

    private Long lastLoadedTimestamp = null;
    private AsyncTask<Void, Integer, Void> currentFetchTask = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        DisplayMetrics dm = getResources().getDisplayMetrics();
        int width = (int) (dm.widthPixels / dm.density);
        button_count = (int) ((width - 160) / 42);
        tvNameWidth = (int) ((width - 30 - button_count * 42) * dm.density);

        habits = new HashMap<>();
        positionToHabit = new HashMap<>();
        checkmarks = new HashMap<>();
        scores = new HashMap<>();

        View view = inflater.inflate(R.layout.list_habits_fragment, container, false);
        tvNameHeader = (TextView) view.findViewById(R.id.tvNameHeader);

        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);

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

        Typeface fontawesome =
                Typeface.createFromAsset(getActivity().getAssets(), "fontawesome-webfont.ttf");
        ((TextView) view.findViewById(R.id.tvStarEmpty)).setTypeface(fontawesome);
        llEmpty = view.findViewById(R.id.llEmpty);

        updateEmptyMessage();
        setHasOptionsMenu(true);
        return view;
    }

    @Override
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
        if (lastLoadedTimestamp == null || lastLoadedTimestamp != DateHelper.getStartOfToday())
        {
            updateHeader();
            fetchAllHabits();
            updateEmptyMessage();
        }

        adapter.notifyDataSetChanged();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        short_toggle_enabled = prefs.getBoolean("pref_short_toggle", false);
    }

    private void updateHeader()
    {
        LayoutInflater inflater = activity.getLayoutInflater();
        View view = getView();

        if (view == null) return;

        GregorianCalendar day = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
        day.setTimeInMillis(DateHelper.getStartOfDay(DateHelper.getLocalTime()));

        LinearLayout llButtonsHeader = (LinearLayout) view.findViewById(R.id.llButtonsHeader);
        llButtonsHeader.removeAllViews();

        for (int i = 0; i < button_count; i++)
        {
            View check = inflater.inflate(R.layout.list_habits_header_check, null);
            Button btCheck = (Button) check.findViewById(R.id.tvCheck);
            btCheck.setText(
                    day.getDisplayName(GregorianCalendar.DAY_OF_WEEK, GregorianCalendar.SHORT,
                            Locale.US) + "\n" +
                            Integer.toString(day.get(GregorianCalendar.DAY_OF_MONTH)));
            llButtonsHeader.addView(check);

            day.add(GregorianCalendar.DAY_OF_MONTH, -1);
        }
    }

    private void fetchAllHabits()
    {
        if (currentFetchTask != null) currentFetchTask.cancel(true);

        currentFetchTask = new AsyncTask<Void, Integer, Void>()
        {
            HashMap<Long, Habit> newHabits = Habit.getAll();
            HashMap<Integer, Habit> newPositionToHabit = new HashMap<>();
            HashMap<Long, int[]> newCheckmarks = new HashMap<>();
            HashMap<Long, Integer> newScores = new HashMap<>();

            @Override
            protected Void doInBackground(Void... params)
            {
                long dateTo = DateHelper.getStartOfDay(DateHelper.getLocalTime());
                long dateFrom = dateTo - (button_count - 1) * DateHelper.millisecondsInOneDay;
                int[] empty = new int[button_count];

                for (Habit h : newHabits.values())
                {
                    newScores.put(h.getId(), 0);
                    newPositionToHabit.put(h.position, h);
                    newCheckmarks.put(h.getId(), empty);
                }

                int current = 0;
                for (int i = 0; i < newHabits.size(); i++)
                {
                    if (isCancelled()) return null;

                    Habit h = newPositionToHabit.get(i);
                    newScores.put(h.getId(), h.getScore());
                    newCheckmarks.put(h.getId(), h.getCheckmarks(dateFrom, dateTo));

                    publishProgress(current++, newHabits.size());
                }

                commit();

                return null;
            }

            private void commit()
            {
                habits = newHabits;
                positionToHabit = newPositionToHabit;
                checkmarks = newCheckmarks;
                scores = newScores;
            }

            @Override
            protected void onPreExecute()
            {
                progressBar.setIndeterminate(false);
                progressBar.setProgress(0);
                progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            protected void onProgressUpdate(Integer... values)
            {
                progressBar.setMax(values[1]);
                progressBar.setProgress(values[0]);

                if (lastLoadedTimestamp == null)
                {
                    commit();
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            protected void onPostExecute(Void aVoid)
            {
                if (isCancelled()) return;

                adapter.notifyDataSetChanged();
                updateEmptyMessage();

                progressBar.setVisibility(View.INVISIBLE);
                currentFetchTask = null;
                lastLoadedTimestamp = DateHelper.getStartOfToday();
            }

        };

        currentFetchTask.execute();
    }

    private void fetchHabit(final Long id)
    {
        new AsyncTask<Void, Void, Void>()
        {
            @Override
            protected Void doInBackground(Void... params)
            {
                long dateTo = DateHelper.getStartOfDay(DateHelper.getLocalTime());
                long dateFrom = dateTo - (button_count - 1) * DateHelper.millisecondsInOneDay;

                Habit h = Habit.get(id);
                habits.put(id, h);
                scores.put(id, h.getScore());
                checkmarks.put(id, h.getCheckmarks(dateFrom, dateTo));

                return null;
            }

            @Override
            protected void onPreExecute()
            {
                new Handler().postDelayed(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        if (getStatus() == Status.RUNNING)
                        {
                            progressBar.setIndeterminate(true);
                            progressBar.setVisibility(View.VISIBLE);
                        }
                    }
                }, 500);
            }

            @Override
            protected void onPostExecute(Void aVoid)
            {
                progressBar.setVisibility(View.GONE);
                adapter.notifyDataSetChanged();
            }
        }.execute();
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
        final Habit habit = habits.get(info.id);

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
                fetchAllHabits();
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
        final int id = menuItem.getItemId();
        final Habit habit = habits.get(info.id);

        if (id == R.id.action_edit_habit)
        {
            EditHabitFragment frag = EditHabitFragment.editSingleHabitFragment(habit.getId());
            frag.setOnSavedListener(this);
            frag.show(getFragmentManager(), "dialog");
            return true;
        }
        else if (id == R.id.action_archive_habit)
        {
            Command c = habit.new ArchiveCommand();
            executeCommand(c, null);
        }
        else if (id == R.id.action_unarchive_habit)
        {
            Command c = habit.new UnarchiveCommand();
            executeCommand(c, null);
        }

        return super.onContextItemSelected(menuItem);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
        if (new Date().getTime() - lastLongClick < 1000) return;

        Habit habit = positionToHabit.get(position);
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
        if (lastLoadedTimestamp == null) llEmpty.setVisibility(View.GONE);
        else llEmpty.setVisibility(habits.size() > 0 ? View.GONE : View.VISIBLE);
    }

    @Override
    public boolean onLongClick(View v)
    {
        switch (v.getId())
        {
            case R.id.tvCheck:
            {
                lastLongClick = new Date().getTime();
                if (!short_toggle_enabled)
                {
                    toggleCheck(v);
                    Vibrator vb =
                            (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
                    vb.vibrate(100);
                }

                return true;
            }
        }

        return false;
    }

    private void toggleCheck(View v)
    {
        Habit habit = habits.get((Long) v.getTag(R.string.habit_key));

        int offset = (Integer) v.getTag(R.string.offset_key);
        long timestamp = DateHelper.getStartOfDay(
                DateHelper.getLocalTime() - offset * DateHelper.millisecondsInOneDay);

        if (v.getTag(R.string.toggle_key).equals(2)) updateCheck(habit.color, (TextView) v, 0);
        else updateCheck(habit.color, (TextView) v, 2);

        executeCommand(habit.new ToggleRepetitionCommand(timestamp), habit.getId());
    }

    private void executeCommand(Command c, Long refreshKey)
    {
        activity.executeCommand(c, refreshKey);
    }

    @Override
    public void drop(int from, int to)
    {
        Habit fromHabit = positionToHabit.get(from);
        Habit toHabit = positionToHabit.get(to);
        positionToHabit.put(to, fromHabit);
        positionToHabit.put(from, toHabit);
        adapter.notifyDataSetChanged();

        Habit.reorder(from, to);
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.tvCheck:
                if (short_toggle_enabled) toggleCheck(v);
                else activity.showToast(R.string.long_press_to_toggle);
                return;
        }
    }

    class ListHabitsAdapter extends BaseAdapter
    {
        private Context context;
        private LayoutInflater inflater;
        private Typeface fontawesome;

        public ListHabitsAdapter(Context context)
        {
            this.context = context;

            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            fontawesome = Typeface.createFromAsset(context.getAssets(), "fontawesome-webfont.ttf");
        }

        @Override
        public int getCount()
        {
            return habits.size();
        }

        @Override
        public Object getItem(int position)
        {
            return positionToHabit.get(position);
        }

        @Override
        public long getItemId(int position)
        {
            return ((Habit) getItem(position)).getId();
        }

        @Override
        public View getView(int position, View view, ViewGroup parent)
        {
            final Habit habit = positionToHabit.get(position);

            if (view == null ||
                    (Long) view.getTag(R.id.KEY_TIMESTAMP) != DateHelper.getStartOfToday())
            {
                view = inflater.inflate(R.layout.list_habits_item, null);
                ((TextView) view.findViewById(R.id.tvStar)).setTypeface(fontawesome);

                LinearLayout.LayoutParams params =
                        new LinearLayout.LayoutParams(tvNameWidth, LayoutParams.WRAP_CONTENT, 1);
                view.findViewById(R.id.tvName).setLayoutParams(params);

                Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE))
                        .getDefaultDisplay();
                Point size = new Point();
                display.getSize(size);

                for (int i = 0; i < button_count; i++)
                {
                    View check = inflater.inflate(R.layout.list_habits_item_check, null);
                    TextView btCheck = (TextView) check.findViewById(R.id.tvCheck);
                    btCheck.setTypeface(fontawesome);
                    btCheck.setOnLongClickListener(ListHabitsFragment.this);
                    btCheck.setOnClickListener(ListHabitsFragment.this);
                    ((LinearLayout) view.findViewById(R.id.llButtons)).addView(check);
                }

                view.setTag(R.id.KEY_TIMESTAMP, DateHelper.getStartOfToday());
            }

            TextView tvStar = (TextView) view.findViewById(R.id.tvStar);
            TextView tvName = (TextView) view.findViewById(R.id.tvName);

            if (habit == null)
            {
                tvName.setText(null);
                return view;
            }

            LinearLayout llInner = (LinearLayout) view.findViewById(R.id.llInner);
            llInner.setTag(R.string.habit_key, habit.getId());

            int activeColor = habit.color;

            tvName.setText(habit.name);
            tvName.setTextColor(activeColor);

            if (habit.isArchived())
            {
                activeColor = ColorHelper.palette[12];
                tvName.setTextColor(activeColor);

                tvStar.setText(context.getString(R.string.fa_archive));
                tvStar.setTextColor(activeColor);
            }
            else
            {
                int score = scores.get(habit.getId());

                if (score < Habit.HALF_STAR_CUTOFF)
                {
                    tvStar.setText(context.getString(R.string.fa_star_o));
                    tvStar.setTextColor(INACTIVE_COLOR);
                }
                else if (score < Habit.FULL_STAR_CUTOFF)
                {
                    tvStar.setText(context.getString(R.string.fa_star_half_o));
                    tvStar.setTextColor(INACTIVE_COLOR);
                }
                else
                {
                    tvStar.setText(context.getString(R.string.fa_star));
                    tvStar.setTextColor(activeColor);
                }
            }

            LinearLayout llButtons = (LinearLayout) view.findViewById(R.id.llButtons);
            int m = llButtons.getChildCount();

            int isChecked[] = checkmarks.get(habit.getId());

            for (int i = 0; i < m; i++)
            {

                TextView tvCheck = (TextView) llButtons.getChildAt(i);
                tvCheck.setTag(R.string.habit_key, habit.getId());
                tvCheck.setTag(R.string.offset_key, i);
                updateCheck(activeColor, tvCheck, isChecked[i]);
            }

            return view;
        }
    }

    private void updateCheck(int activeColor, TextView tvCheck, int check)
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
        if (refreshKey == null) fetchAllHabits();
        else fetchHabit(refreshKey);
    }
}
