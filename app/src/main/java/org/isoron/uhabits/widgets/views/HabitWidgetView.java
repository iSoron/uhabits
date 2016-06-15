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

package org.isoron.uhabits.widgets.views;

import android.content.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.graphics.drawable.shapes.*;
import android.support.annotation.*;
import android.util.*;
import android.view.*;
import android.widget.*;

import org.isoron.uhabits.*;
import org.isoron.uhabits.models.*;
import org.isoron.uhabits.ui.habits.show.views.*;
import org.isoron.uhabits.utils.*;

import java.util.*;

public abstract class HabitWidgetView extends FrameLayout
    implements HabitDataView
{
    @Nullable
    protected InsetDrawable background;

    @Nullable
    protected Paint backgroundPaint;

    @Nullable
    protected Habit habit;

    protected ViewGroup frame;

    private int shadowAlpha;

    public HabitWidgetView(Context context)
    {
        super(context);
        init();
    }

    public HabitWidgetView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init();
    }

    @Override
    public void setHabit(@NonNull Habit habit)
    {
        this.habit = habit;
    }

    public void setShadowAlpha(int shadowAlpha)
    {
        this.shadowAlpha = shadowAlpha;
    }

    protected abstract
    @NonNull
    Integer getInnerLayoutId();

    protected void rebuildBackground()
    {
        Context context = getContext();

        int backgroundAlpha = (int) (255 *
                                     InterfaceUtils.getStyledFloat(context,
                                         R.attr.widgetBackgroundAlpha));

        int shadowRadius = (int) InterfaceUtils.dpToPixels(context, 2);
        int shadowOffset = (int) InterfaceUtils.dpToPixels(context, 1);
        int shadowColor = Color.argb(shadowAlpha, 0, 0, 0);

        float cornerRadius = InterfaceUtils.dpToPixels(context, 5);
        float[] radii = new float[8];
        Arrays.fill(radii, cornerRadius);

        RoundRectShape shape = new RoundRectShape(radii, null, null);
        ShapeDrawable innerDrawable = new ShapeDrawable(shape);

        int insetLeftTop = Math.max(shadowRadius - shadowOffset, 0);
        int insetRightBottom = shadowRadius + shadowOffset;

        background =
            new InsetDrawable(innerDrawable, insetLeftTop, insetLeftTop,
                insetRightBottom, insetRightBottom);
        backgroundPaint = innerDrawable.getPaint();
        backgroundPaint.setShadowLayer(shadowRadius, shadowOffset, shadowOffset,
            shadowColor);
        backgroundPaint.setColor(
            InterfaceUtils.getStyledColor(context, R.attr.cardBackgroundColor));
        backgroundPaint.setAlpha(backgroundAlpha);

        frame = (ViewGroup) findViewById(R.id.frame);
        if (frame != null) frame.setBackgroundDrawable(background);
    }

    private void init()
    {
        inflate(getContext(), getInnerLayoutId(), this);
        shadowAlpha = (int) (255 * InterfaceUtils.getStyledFloat(getContext(),
            R.attr.widgetShadowAlpha));
        rebuildBackground();
    }
}
