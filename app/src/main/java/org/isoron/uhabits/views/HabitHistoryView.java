package org.isoron.uhabits.views;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.isoron.helpers.ColorHelper;
import org.isoron.helpers.DateHelper;
import org.isoron.uhabits.R;
import org.isoron.uhabits.models.Habit;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.support.v4.view.MotionEventCompat;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class HabitHistoryView extends View
{

	private Habit habit;
	private int reps[];

	private Context context;
	private Paint pSquareBg, pSquareFg, pTextHeader;

    private int squareSize, squareSpacing;
	private int nColumns, offsetWeeks;

	private int colorPrimary, colorPrimaryBrighter, grey;

	public HabitHistoryView(Context context, Habit habit, int squareSize)
	{
		super(context);
		this.habit = habit;
		this.context = context;
		this.squareSize = squareSize;

		Typeface fontawesome = Typeface.createFromAsset(context.getAssets(),
				"fontawesome-webfont.ttf");

		colorPrimary = habit.color;
		colorPrimaryBrighter = ColorHelper.mixColors(colorPrimary, Color.WHITE, 0.5f);
		grey = Color.rgb(230, 230, 230);
		squareSpacing = 2;

		pTextHeader = new Paint();
		pTextHeader.setColor(Color.LTGRAY);
		pTextHeader.setTextAlign(Align.LEFT);
		pTextHeader.setTextSize(squareSize * 0.5f);
		pTextHeader.setAntiAlias(true);

		pSquareBg = new Paint();
		pSquareBg.setColor(habit.color);

		pSquareFg = new Paint();
		pSquareFg.setColor(Color.WHITE);
		pSquareFg.setAntiAlias(true);
//		pSquareFg.setTypeface(fontawesome);
		pSquareFg.setTextSize(squareSize * 0.5f);
		pSquareFg.setTextAlign(Align.CENTER);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		setMeasuredDimension(getMeasuredWidth(), 8 * squareSize);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh)
	{
        nColumns = (w / squareSize) - 1;
		fetchReps();
	}

	private void fetchReps()
	{
		Calendar currentDate = new GregorianCalendar();
		currentDate.add(Calendar.DAY_OF_YEAR, -offsetWeeks * 7);
		int dayOfWeek = currentDate.get(Calendar.DAY_OF_WEEK) % 7;

		long dateTo = DateHelper.getStartOfToday();
		for (int i = 0; i < 7 - dayOfWeek; i++)
			dateTo += DateHelper.millisecondsInOneDay;

		for (int i = 0; i < offsetWeeks * 7; i++)
			dateTo -= DateHelper.millisecondsInOneDay;

		long dateFrom = dateTo;
		for (int i = 0; i < nColumns * 7; i++)
			dateFrom -= DateHelper.millisecondsInOneDay;

		reps = habit.getReps(dateFrom, dateTo);
	}

	@Override
	protected void onDraw(Canvas canvas)
	{
		super.onDraw(canvas);

		Rect square = new Rect(0, 0, squareSize - squareSpacing, squareSize - squareSpacing);

		Calendar currentDate = new GregorianCalendar();
		currentDate.add(Calendar.DAY_OF_YEAR, -(offsetWeeks-1) * 7);

		int nDays = nColumns * 7;
		int todayWeekday = new GregorianCalendar().get(Calendar.DAY_OF_WEEK) % 7;

		currentDate.add(Calendar.DAY_OF_YEAR, -nDays);

		SimpleDateFormat dfMonth = new SimpleDateFormat("MMM");
		SimpleDateFormat dfYear = new SimpleDateFormat("yyyy");

		String previousMonth = "";
		String previousYear = "";

		int colors[] = { grey, colorPrimaryBrighter, colorPrimary };
		String markers[] = { context.getString(R.string.fa_times),
				context.getString(R.string.fa_check), context.getString(R.string.fa_check) };

		float squareTextOffset = pSquareFg.getFontSpacing() * 0.4f;
		float headerTextOffset = pTextHeader.getFontSpacing() * 0.3f;
		boolean justPrintedYear = false;

		int k = nDays;
		for (int i = 0; i < nColumns; i++)
		{
			String month = dfMonth.format(currentDate.getTime());
			String year = dfYear.format(currentDate.getTime());

			if(!month.equals(previousMonth))
			{
				int offset = 0;
				if(justPrintedYear)
					offset += squareSize;
				
				canvas.drawText(month, square.left + offset, square.bottom - headerTextOffset,
						pTextHeader);
				previousMonth = month;
				justPrintedYear = false;
			}
			else if(!year.equals(previousYear))
			{
				canvas.drawText(year, square.left, square.bottom - headerTextOffset, pTextHeader);
				previousYear = year;
				justPrintedYear = true;
			}
			else
			{
				justPrintedYear = false;
			}


			square.offset(0, squareSize);

			for (int j = 0; j < 7; j++)
			{
				if(!(i == nColumns - 1 && offsetWeeks == 0 && j > todayWeekday))
				{
					pSquareBg.setColor(colors[reps[k]]);
					canvas.drawRect(square, pSquareBg);
					//					canvas.drawText(markers[reps[k]], square.centerX(), square.centerY()
					//							+ squareTextOffset, pSquareFg);
					canvas.drawText(Integer.toString(currentDate.get(Calendar.DAY_OF_MONTH)),
							square.centerX(), square.centerY() + squareTextOffset, pSquareFg);
				}

				currentDate.add(Calendar.DAY_OF_MONTH, 1);
				square.offset(0, squareSize);
				k--;
			}

			square.offset(squareSize, -8 * squareSize);
		}

		String wdays[] = { "Sat", "Sun", "Mon", "Tue", "Wed", "Thu", "Fri" };

		for (int i = 0; i < 7; i++)
		{
			square.offset(0, squareSize);
			canvas.drawText(wdays[i], square.left + headerTextOffset, square.bottom
					- headerTextOffset, pTextHeader);
		}
	}

	private Float prevX, prevY;

	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		int action = event.getAction();

		int pointerIndex = MotionEventCompat.getActionIndex(event);
		final float x = MotionEventCompat.getX(event, pointerIndex);
		final float y = MotionEventCompat.getY(event, pointerIndex);

		if(action == MotionEvent.ACTION_DOWN)
		{
			prevX = x;
			prevY = y;
		}

		if(action == MotionEvent.ACTION_MOVE)
		{
			float dx = x - prevX;
			float dy = y - prevY;

			int newOffsetWeeks = offsetWeeks + (int) (dx / squareSize);
			newOffsetWeeks = Math.max(0, newOffsetWeeks);

			if(newOffsetWeeks != offsetWeeks)
			{
				prevX = x;
				prevY = y;
				offsetWeeks = newOffsetWeeks;

				fetchReps();
				invalidate();
			}

		}
		return true;
	}

}
