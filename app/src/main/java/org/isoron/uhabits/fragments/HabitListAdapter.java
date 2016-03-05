package org.isoron.uhabits.fragments;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.isoron.helpers.DateHelper;
import org.isoron.uhabits.R;
import org.isoron.uhabits.helpers.ListHabitsHelper;
import org.isoron.uhabits.loaders.HabitListLoader;
import org.isoron.uhabits.models.Habit;

import java.util.List;

class HabitListAdapter extends BaseAdapter
{
    private final int buttonCount;
    private final int tvNameWidth;
    private LayoutInflater inflater;
    private Typeface fontawesome;
    private HabitListLoader loader;
    private ListHabitsHelper helper;
    private List selectedPositions;
    private View.OnLongClickListener onCheckmarkLongClickListener;
    private View.OnClickListener onCheckmarkClickListener;

    public HabitListAdapter(Context context, HabitListLoader loader)
    {
        this.loader = loader;

        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        fontawesome = Typeface.createFromAsset(context.getAssets(), "fontawesome-webfont.ttf");
        helper = new ListHabitsHelper(context, loader);

        buttonCount = helper.getButtonCount();
        tvNameWidth = helper.getHabitNameWidth();
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

        if (view == null || (Long) view.getTag(R.id.timestamp_key) != DateHelper.getStartOfToday())
        {
            view = inflater.inflate(R.layout.list_habits_item, null);
            ((TextView) view.findViewById(R.id.tvStar)).setTypeface(fontawesome);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(tvNameWidth,
                    LinearLayout.LayoutParams.WRAP_CONTENT, 1);
            view.findViewById(R.id.label).setLayoutParams(params);

            inflateCheckmarkButtons(view);

            view.setTag(R.id.timestamp_key, DateHelper.getStartOfToday());
        }

        TextView tvStar = ((TextView) view.findViewById(R.id.tvStar));
        TextView tvName = (TextView) view.findViewById(R.id.label);
        LinearLayout llInner = (LinearLayout) view.findViewById(R.id.llInner);
        LinearLayout llButtons = (LinearLayout) view.findViewById(R.id.llButtons);

        llInner.setTag(R.string.habit_key, habit.getId());

        helper.updateNameAndIcon(habit, tvStar, tvName);
        helper.updateCheckmarkButtons(habit, llButtons);

        boolean selected = selectedPositions.contains(position);
        if (selected) llInner.setBackgroundResource(R.drawable.selected_box);
        else
        {
            if (android.os.Build.VERSION.SDK_INT >= 21)
                llInner.setBackgroundResource(R.drawable.ripple_white);
            else llInner.setBackgroundResource(R.drawable.card_background);
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
            btCheck.setOnLongClickListener(onCheckmarkLongClickListener);
            btCheck.setOnClickListener(onCheckmarkClickListener);
            ((LinearLayout) view.findViewById(R.id.llButtons)).addView(check);
        }
    }

    public void setSelectedPositions(List selectedPositions)
    {
        this.selectedPositions = selectedPositions;
    }

    public void setOnCheckmarkLongClickListener(View.OnLongClickListener listener)
    {
        this.onCheckmarkLongClickListener = listener;
    }

    public void setOnCheckmarkClickListener(View.OnClickListener listener)
    {
        this.onCheckmarkClickListener = listener;
    }
}
