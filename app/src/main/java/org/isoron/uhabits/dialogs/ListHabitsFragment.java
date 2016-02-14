package org.isoron.uhabits.dialogs;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Vibrator;
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
import android.widget.TextView;

import com.mobeta.android.dslv.DragSortController;
import com.mobeta.android.dslv.DragSortListView;
import com.mobeta.android.dslv.DragSortListView.DropListener;

import org.isoron.helpers.Command;
import org.isoron.helpers.DateHelper;
import org.isoron.helpers.DialogHelper.OnSavedListener;
import org.isoron.helpers.ReplayableActivity;
import org.isoron.uhabits.R;
import org.isoron.uhabits.ReminderHelper;
import org.isoron.uhabits.models.Habit;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

public class ListHabitsFragment extends Fragment
        implements OnSavedListener, OnItemClickListener, OnLongClickListener, DropListener,
        OnClickListener
{
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

    private OnHabitClickListener habitClickListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.list_habits_fragment, container, false);

        DisplayMetrics dm = getResources().getDisplayMetrics();
        int width = (int) (dm.widthPixels / dm.density);
        button_count = (int) ((width - 160) / 42);
        tvNameWidth = (int) ((width - 30 - button_count * 42) * dm.density);

        tvNameHeader = (TextView) view.findViewById(R.id.tvNameHeader);
//		updateStarCount();

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
        updateHeader();
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
            btCheck.setText(day.getDisplayName(GregorianCalendar.DAY_OF_WEEK,
                    GregorianCalendar.SHORT, Locale.US) + "\n" +
                    Integer.toString(day.get(GregorianCalendar.DAY_OF_MONTH)));
            llButtonsHeader.addView(check);

            day.add(GregorianCalendar.DAY_OF_MONTH, -1);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        inflater.inflate(R.menu.show_habits_options, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo)
    {
        super.onCreateContextMenu(menu, view, menuInfo);
        getActivity().getMenuInflater().inflate(R.menu.show_habits_context, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();

        if (id == R.id.action_add)
        {
            EditHabitFragment frag = EditHabitFragment.createHabitFragment();
            frag.setOnSavedListener(this);
            frag.show(getFragmentManager(), "dialog");
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onContextItemSelected(MenuItem menuItem)
    {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuItem.getMenuInfo();
        final int id = menuItem.getItemId();
        final Habit habit = Habit.get(info.id);

        if (id == R.id.action_edit_habit)
        {
            EditHabitFragment frag = EditHabitFragment.editSingleHabitFragment(habit.getId());
            frag.setOnSavedListener(this);
            frag.show(getFragmentManager(), "dialog");
            return true;
        }

        if (id == R.id.action_archive_habit)
        {
            Command c = habit.new ArchiveCommand();
            executeCommand(c);
        }

        return super.onContextItemSelected(menuItem);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
        if (new Date().getTime() - lastLongClick < 1000) return;

        Habit habit = Habit.getByPosition(position);
        habitClickListener.onHabitClicked(habit);
    }

    @Override
    public void onSaved(Command command)
    {
        executeCommand(command);
        ReminderHelper.createReminderAlarms(activity);
    }

    public void notifyDataSetChanged()
    {
        updateEmptyMessage();
        adapter.notifyDataSetChanged();
    }

    private void updateEmptyMessage()
    {
        llEmpty.setVisibility(Habit.getCount() > 0 ? View.GONE : View.VISIBLE);
    }

    @Override
    public boolean onLongClick(View v)
    {
        int id = v.getId();

        if (id == R.id.tvCheck)
        {
            lastLongClick = new Date().getTime();
            Habit habit = Habit.get((Long) v.getTag(R.string.habit_key));
            int offset = (Integer) v.getTag(R.string.offset_key);
            long timestamp = DateHelper.getStartOfDay(
                    DateHelper.getLocalTime() - offset * DateHelper.millisecondsInOneDay);

            executeCommand(habit.new ToggleRepetitionCommand(timestamp));

            Vibrator vb = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
            vb.vibrate(100);

            return true;
        }

        return false;
    }

    private void executeCommand(Command c)
    {
        activity.executeCommand(c);
    }

    @Override
    public void drop(int from, int to)
    {
        Habit.reorder(from, to);
        notifyDataSetChanged();
    }

    @Override
    public void onClick(View v)
    {
    }

    void updateStarCount()
    {
        String msg = "";
        int starCount = Habit.getStarCount();

        if (starCount == 1) msg = String.format("%d star", starCount);
        else if (starCount > 1) msg = String.format("%d stars", starCount);

        tvNameHeader.setText(msg);
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
            return Habit.getCount();
        }

        @Override
        public Object getItem(int position)
        {
            return Habit.getByPosition(position);
        }

        @Override
        public long getItemId(int position)
        {
            return ((Habit) getItem(position)).getId();
        }

        @Override
        public View getView(int position, View view, ViewGroup parent)
        {
            final Habit habit = (Habit) getItem(position);

            if (view == null || (Long) view.getTag(R.id.KEY_TIMESTAMP) !=
                    DateHelper.getStartOfToday())
            {
                view = inflater.inflate(R.layout.list_habits_item, null);
                ((TextView) view.findViewById(R.id.tvStar)).setTypeface(fontawesome);

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(tvNameWidth,
                        LayoutParams.WRAP_CONTENT, 1);
                ((TextView) view.findViewById(R.id.tvName)).setLayoutParams(params);

                Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE))
                        .getDefaultDisplay();
                Point size = new Point();
                display.getSize(size);

                LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(
                        LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                llp.setMargins(2, 0, 2, 0);

                for (int i = 0; i < button_count; i++)
                {
                    View check = inflater.inflate(R.layout.list_habits_item_check, null);
                    TextView btCheck = (TextView) check.findViewById(R.id.tvCheck);
                    btCheck.setTypeface(fontawesome);
                    btCheck.setOnLongClickListener(ListHabitsFragment.this);
//					btCheck.setLayoutParams(llp);
                    ((LinearLayout) view.findViewById(R.id.llButtons)).addView(check);
                }

//				LinearLayout llInner = (LinearLayout) view.findViewById(R.id.llInner);
//				llInner.setOnClickListener(ListHabitsFragment.this);

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

            int inactiveColor = Color.rgb(230, 230, 230);
            int activeColor = habit.color;

            tvName.setText(habit.name);
            tvName.setTextColor(activeColor);

            int score = habit.getScore();

            if (score < Habit.HALF_STAR_CUTOFF)
            {
                tvStar.setText(context.getString(R.string.fa_star_o));
                tvStar.setTextColor(inactiveColor);
            }
            else if (score < Habit.FULL_STAR_CUTOFF)
            {
                tvStar.setText(context.getString(R.string.fa_star_half_o));
                tvStar.setTextColor(inactiveColor);
            }
            else
            {
                tvStar.setText(context.getString(R.string.fa_star));
                tvStar.setTextColor(activeColor);
            }

            LinearLayout llButtons = (LinearLayout) view.findViewById(R.id.llButtons);
            int m = llButtons.getChildCount();

            long dateTo = DateHelper.getStartOfDay(DateHelper.getLocalTime());
            long dateFrom = dateTo - m * DateHelper.millisecondsInOneDay;

            int isChecked[] = habit.getReps(dateFrom, dateTo);

            for (int i = 0; i < m; i++)
            {

                TextView tvCheck = (TextView) llButtons.getChildAt(i);
                tvCheck.setTag(R.string.habit_key, habit.getId());
                tvCheck.setTag(R.string.offset_key, i);

                switch (isChecked[i])
                {
                    case 2:
                        tvCheck.setText(R.string.fa_check);
                        tvCheck.setTextColor(activeColor);
                        break;

                    case 1:
                        tvCheck.setText(R.string.fa_check);
                        tvCheck.setTextColor(inactiveColor);
                        break;

                    case 0:
                        tvCheck.setText(R.string.fa_times);
                        tvCheck.setTextColor(inactiveColor);
                        break;
                }
            }

            return view;
        }

    }
}
