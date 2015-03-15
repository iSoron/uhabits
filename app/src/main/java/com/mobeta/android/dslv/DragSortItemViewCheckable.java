package com.mobeta.android.dslv;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Checkable;
import android.util.Log;

/**
 * Lightweight ViewGroup that wraps list items obtained from user's
 * ListAdapter. ItemView expects a single child that has a definite
 * height (i.e. the child's layout height is not MATCH_PARENT).
 * The width of
 * ItemView will always match the width of its child (that is,
 * the width MeasureSpec given to ItemView is passed directly
 * to the child, and the ItemView measured width is set to the
 * child's measured width). The height of ItemView can be anything;
 * the 
 * 
 *
 * The purpose of this class is to optimize slide
 * shuffle animations.
 */
public class DragSortItemViewCheckable extends DragSortItemView implements Checkable {

    public DragSortItemViewCheckable(Context context) {
        super(context);
    }

    @Override
    public boolean isChecked() {
        View child = getChildAt(0);
        if (child instanceof Checkable)
            return ((Checkable) child).isChecked();
        else
            return false;
    }

    @Override
    public void setChecked(boolean checked) {
        View child = getChildAt(0);
        if (child instanceof Checkable)
            ((Checkable) child).setChecked(checked);
    }

    @Override
    public void toggle() {
        View child = getChildAt(0);
        if (child instanceof Checkable)
            ((Checkable) child).toggle();
    }
}
