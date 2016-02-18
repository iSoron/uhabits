package org.isoron.uhabits.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v4.view.MotionEventCompat;
import android.view.MotionEvent;
import android.view.View;

import org.isoron.helpers.ColorHelper;
import org.isoron.helpers.DateHelper;
import org.isoron.uhabits.models.Habit;
import org.isoron.uhabits.models.Streak;

import java.text.SimpleDateFormat;
import java.util.List;

public class HabitStreakView extends View
{
    private Habit habit;
    private int columnWidth, columnHeight, nColumns;

    private Paint pText, pBar;
    private List<Streak> streaks;
    private int dataOffset;

    private long maxStreakLength;

    private int barHeaderHeight;

    private int[] colors;
    private float prevX;
    private float prevY;

    public HabitStreakView(Context context, Habit habit, int columnWidth)
    {
        super(context);
        this.habit = habit;
        this.columnWidth = columnWidth;

        pText = new Paint();
        pText.setColor(Color.LTGRAY);
        pText.setTextAlign(Paint.Align.CENTER);
        pText.setTextSize(columnWidth * 0.5f);
        pText.setAntiAlias(true);

        pBar = new Paint();
        pBar.setTextAlign(Paint.Align.CENTER);
        pBar.setTextSize(columnWidth * 0.5f);
        pBar.setAntiAlias(true);

        columnHeight = 8 * columnWidth;
        barHeaderHeight = columnWidth;

        colors = new int[4];

        colors[0] = Color.rgb(230, 230, 230);
        colors[3] = habit.color;
        colors[1] = ColorHelper.mixColors(colors[0], colors[3], 0.66f);
        colors[2] = ColorHelper.mixColors(colors[0], colors[3], 0.33f);

        fetchStreaks();
    }

    private void fetchStreaks()
    {
        streaks = habit.getStreaks();

        for(Streak s : streaks)
            maxStreakLength = Math.max(maxStreakLength, s.length);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(getMeasuredWidth(), columnHeight + 2*barHeaderHeight);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh)
    {
        super.onSizeChanged(w, h, oldw, oldh);
        nColumns = w / columnWidth;
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);

        float lineHeight = pText.getFontSpacing();
        float barHeaderOffset = lineHeight * 0.4f;

        int nStreaks = streaks.size();
        int start = Math.max(0, nStreaks - nColumns - dataOffset);
        SimpleDateFormat dfMonth = new SimpleDateFormat("MMM");

        String previousMonth = "";

        for (int offset = 0; offset < nColumns && start+offset < nStreaks; offset++)
        {
            String month = dfMonth.format(streaks.get(start+offset).start);

            long l = streaks.get(offset+start).length;
            double lRelative = ((double) l) / maxStreakLength;

            pBar.setColor(colors[(int) Math.floor(lRelative*3)]);

            int height = (int) (columnHeight * lRelative);
            Rect r = new Rect(0,0,columnWidth-2, height);
            r.offset(offset * columnWidth, barHeaderHeight + columnHeight - height);

            canvas.drawRect(r, pBar);
            canvas.drawText(Long.toString(l), r.centerX(), r.top - barHeaderOffset, pBar);

            if(!month.equals(previousMonth))
                canvas.drawText(month, r.centerX(), r.bottom + lineHeight * 1.2f, pText);

            previousMonth = month;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        int action = event.getAction();

        int pointerIndex = MotionEventCompat.getActionIndex(event);
        final float x = MotionEventCompat.getX(event, pointerIndex);
        final float y = MotionEventCompat.getY(event, pointerIndex);

        if (action == MotionEvent.ACTION_DOWN)
        {
            prevX = x;
            prevY = y;
        }

        if (action == MotionEvent.ACTION_MOVE)
        {
            float dx = x - prevX;
            float dy = y - prevY;

            if (Math.abs(dy) > Math.abs(dx)) return false;
            getParent().requestDisallowInterceptTouchEvent(true);
            if(move(dx))
            {
                prevX = x;
                prevY = y;
            }
        }

        return true;
    }

    private boolean move(float dx)
    {
        int newDataOffset = dataOffset + (int) (dx / columnWidth);
        newDataOffset = Math.max(0, Math.min(streaks.size() - nColumns, newDataOffset));

        if (newDataOffset != dataOffset)
        {
            dataOffset = newDataOffset;
            invalidate();
            return true;
        }
        else
            return false;
    }
}
