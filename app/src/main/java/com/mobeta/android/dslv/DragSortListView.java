/*
 * DragSortListView.
 *
 * A subclass of the Android ListView component that enables drag
 * and drop re-ordering of list items.
 *
 * Copyright 2012 Carl Bauer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mobeta.android.dslv;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import org.isoron.uhabits.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.util.SparseIntArray;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.Checkable;
import android.widget.ListAdapter;
import android.widget.ListView;

/**
 * ListView subclass that mediates drag and drop resorting of items.
 * 
 * 
 * @author heycosmo
 *
 */
public class DragSortListView extends ListView {
    
    
    /**
     * The View that floats above the ListView and represents
     * the dragged item.
     */
    private View mFloatView;

    /**
     * The float View location. First based on touch location
     * and given deltaX and deltaY. Then restricted by callback
     * to FloatViewManager.onDragFloatView(). Finally restricted
     * by bounds of DSLV.
     */
    private Point mFloatLoc = new Point();

    private Point mTouchLoc = new Point();

    /**
     * The middle (in the y-direction) of the floating View.
     */
    private int mFloatViewMid;

    /**
     * Flag to make sure float View isn't measured twice
     */
    private boolean mFloatViewOnMeasured = false;

    /**
     * Watch the Adapter for data changes. Cancel a drag if
     * coincident with a change.
     */ 
    private DataSetObserver mObserver;

    /**
     * Transparency for the floating View (XML attribute).
     */
    private float mFloatAlpha = 1.0f;
    private float mCurrFloatAlpha = 1.0f;

    /**
     * While drag-sorting, the current position of the floating
     * View. If dropped, the dragged item will land in this position.
     */
    private int mFloatPos;

    /**
     * The first expanded ListView position that helps represent
     * the drop slot tracking the floating View.
     */
    private int mFirstExpPos;

    /**
     * The second expanded ListView position that helps represent
     * the drop slot tracking the floating View. This can equal
     * mFirstExpPos if there is no slide shuffle occurring; otherwise
     * it is equal to mFirstExpPos + 1.
     */
    private int mSecondExpPos;

    /**
     * Flag set if slide shuffling is enabled.
     */
    private boolean mAnimate = false;

    /**
     * The user dragged from this position.
     */
    private int mSrcPos;

    /**
     * Offset (in x) within the dragged item at which the user
     * picked it up (or first touched down with the digitalis).
     */
    private int mDragDeltaX;

    /**
     * Offset (in y) within the dragged item at which the user
     * picked it up (or first touched down with the digitalis).
     */
    private int mDragDeltaY;


    /**
     * The difference (in x) between screen coordinates and coordinates
     * in this view.
     */
    private int mOffsetX;

    /**
     * The difference (in y) between screen coordinates and coordinates
     * in this view.
     */
    private int mOffsetY;

    /**
     * A listener that receives callbacks whenever the floating View
     * hovers over a new position.
     */
    private DragListener mDragListener;

    /**
     * A listener that receives a callback when the floating View
     * is dropped.
     */
    private DropListener mDropListener;

    /**
     * A listener that receives a callback when the floating View
     * (or more precisely the originally dragged item) is removed
     * by one of the provided gestures.
     */
    private RemoveListener mRemoveListener;

    /**
     * Enable/Disable item dragging
     * 
     * @attr name dslv:drag_enabled
     */
    private boolean mDragEnabled = true;

    /**
     * Drag state enum.
     */
    private final static int IDLE = 0;
    private final static int REMOVING = 1;
    private final static int DROPPING = 2;
    private final static int STOPPED = 3;
    private final static int DRAGGING = 4;

    private int mDragState = IDLE;

    /**
     * Height in pixels to which the originally dragged item
     * is collapsed during a drag-sort. Currently, this value
     * must be greater than zero.
     */
    private int mItemHeightCollapsed = 1;

    /**
     * Height of the floating View. Stored for the purpose of
     * providing the tracking drop slot.
     */
    private int mFloatViewHeight;

    /**
     * Convenience member. See above.
     */
    private int mFloatViewHeightHalf;

    /**
     * Save the given width spec for use in measuring children
     */
    private int mWidthMeasureSpec = 0;

    /**
     * Sample Views ultimately used for calculating the height
     * of ListView items that are off-screen.
     */
    private View[] mSampleViewTypes = new View[1];

    /**
     * Drag-scroll encapsulator!
     */
    private DragScroller mDragScroller;

    /**
     * Determines the start of the upward drag-scroll region
     * at the top of the ListView. Specified by a fraction
     * of the ListView height, thus screen resolution agnostic.
     */
    private float mDragUpScrollStartFrac = 1.0f / 3.0f;

    /**
     * Determines the start of the downward drag-scroll region
     * at the bottom of the ListView. Specified by a fraction
     * of the ListView height, thus screen resolution agnostic.
     */
    private float mDragDownScrollStartFrac = 1.0f / 3.0f;

    /**
     * The following are calculated from the above fracs.
     */
    private int mUpScrollStartY;
    private int mDownScrollStartY;
    private float mDownScrollStartYF;
    private float mUpScrollStartYF;

    /**
     * Calculated from above above and current ListView height.
     */
    private float mDragUpScrollHeight;

    /**
     * Calculated from above above and current ListView height.
     */
    private float mDragDownScrollHeight;

    /**
     * Maximum drag-scroll speed in pixels per ms. Only used with
     * default linear drag-scroll profile.
     */
    private float mMaxScrollSpeed = 0.5f;

    /**
     * Defines the scroll speed during a drag-scroll. User can
     * provide their own; this default is a simple linear profile
     * where scroll speed increases linearly as the floating View
     * nears the top/bottom of the ListView.
     */
    private DragScrollProfile mScrollProfile = new DragScrollProfile() {
        @Override
        public float getSpeed(float w, long t) {
            return mMaxScrollSpeed * w;
        }
    };

    /**
     * Current touch x.
     */
    private int mX;

    /**
     * Current touch y.
     */
    private int mY;

    /**
     * Last touch x.
     */
    private int mLastX;

    /**
     * Last touch y.
     */
    private int mLastY;

    /**
     * The touch y-coord at which drag started
     */
    private int mDragStartY;

    /**
     * Drag flag bit. Floating View can move in the positive
     * x direction.
     */
    public final static int DRAG_POS_X = 0x1;

    /**
     * Drag flag bit. Floating View can move in the negative
     * x direction.
     */
    public final static int DRAG_NEG_X = 0x2;

    /**
     * Drag flag bit. Floating View can move in the positive
     * y direction. This is subtle. What this actually means is
     * that, if enabled, the floating View can be dragged below its starting
     * position. Remove in favor of upper-bounding item position?
     */
    public final static int DRAG_POS_Y = 0x4;

    /**
     * Drag flag bit. Floating View can move in the negative
     * y direction. This is subtle. What this actually means is
     * that the floating View can be dragged above its starting
     * position. Remove in favor of lower-bounding item position?
     */
    public final static int DRAG_NEG_Y = 0x8;

    /**
     * Flags that determine limits on the motion of the
     * floating View. See flags above.
     */
    private int mDragFlags = 0;

    /**
     * Last call to an on*TouchEvent was a call to
     * onInterceptTouchEvent.
     */
    private boolean mLastCallWasIntercept = false;

    /**
     * A touch event is in progress.
     */
    private boolean mInTouchEvent = false;

    /**
     * Let the user customize the floating View.
     */
    private FloatViewManager mFloatViewManager = null;

    /**
     * Given to ListView to cancel its action when a drag-sort
     * begins.
     */
    private MotionEvent mCancelEvent;

    /**
     * Enum telling where to cancel the ListView action when a
     * drag-sort begins
     */
    private static final int NO_CANCEL = 0;
    private static final int ON_TOUCH_EVENT = 1;
    private static final int ON_INTERCEPT_TOUCH_EVENT = 2;

    /**
     * Where to cancel the ListView action when a
     * drag-sort begins
     */ 
    private int mCancelMethod = NO_CANCEL;

    /**
     * Determines when a slide shuffle animation starts. That is,
     * defines how close to the edge of the drop slot the floating
     * View must be to initiate the slide.
     */
    private float mSlideRegionFrac = 0.25f;

    /**
     * Number between 0 and 1 indicating the relative location of
     * a sliding item (only used if drag-sort animations
     * are turned on). Nearly 1 means the item is 
     * at the top of the slide region (nearly full blank item
     * is directly below).
     */
    private float mSlideFrac = 0.0f;

    /**
     * Wraps the user-provided ListAdapter. This is used to wrap each
     * item View given by the user inside another View (currenly
     * a RelativeLayout) which
     * expands and collapses to simulate the item shuffling.
     */
    private AdapterWrapper mAdapterWrapper;

    /**
     * Turn on custom debugger.
     */
    private boolean mTrackDragSort = false;

    /**
     * Debugging class.
     */
    private DragSortTracker mDragSortTracker;

    /**
     * Needed for adjusting item heights from within layoutChildren
     */
    private boolean mBlockLayoutRequests = false;

    /**
     * Set to true when a down event happens during drag sort;
     * for example, when drag finish animations are
     * playing.
     */
    private boolean mIgnoreTouchEvent = false;

    /**
     * Caches DragSortItemView child heights. Sometimes DSLV has to
     * know the height of an offscreen item. Since ListView virtualizes
     * these, DSLV must get the item from the ListAdapter to obtain
     * its height. That process can be expensive, but often the same
     * offscreen item will be requested many times in a row. Once an
     * offscreen item height is calculated, we cache it in this guy.
     * Actually, we cache the height of the child of the
     * DragSortItemView since the item height changes often during a
     * drag-sort.
     */
    private static final int sCacheSize = 3;
    private HeightCache mChildHeightCache = new HeightCache(sCacheSize);

    private RemoveAnimator mRemoveAnimator;

    private LiftAnimator mLiftAnimator;

    private DropAnimator mDropAnimator;

    private boolean mUseRemoveVelocity;
    private float mRemoveVelocityX = 0;

    public DragSortListView(Context context, AttributeSet attrs) {
        super(context, attrs);

        int defaultDuration = 150;
        int removeAnimDuration = defaultDuration; // ms
        int dropAnimDuration = defaultDuration; // ms

        if (attrs != null) {
            TypedArray a = getContext().obtainStyledAttributes(attrs,
                    R.styleable.DragSortListView, 0, 0);

            mItemHeightCollapsed = Math.max(1, a.getDimensionPixelSize(
                    R.styleable.DragSortListView_collapsed_height, 1));

            mTrackDragSort = a.getBoolean(
                    R.styleable.DragSortListView_track_drag_sort, false);

            if (mTrackDragSort) {
                mDragSortTracker = new DragSortTracker();
            }

            // alpha between 0 and 255, 0=transparent, 255=opaque
            mFloatAlpha = a.getFloat(R.styleable.DragSortListView_float_alpha, mFloatAlpha);
            mCurrFloatAlpha = mFloatAlpha;

            mDragEnabled = a.getBoolean(R.styleable.DragSortListView_drag_enabled, mDragEnabled);

            mSlideRegionFrac = Math.max(0.0f,
                    Math.min(1.0f, 1.0f - a.getFloat(
                            R.styleable.DragSortListView_slide_shuffle_speed,
                            0.75f)));

            mAnimate = mSlideRegionFrac > 0.0f;

            float frac = a.getFloat(
                    R.styleable.DragSortListView_drag_scroll_start,
                    mDragUpScrollStartFrac);

            setDragScrollStart(frac);

            mMaxScrollSpeed = a.getFloat(
                    R.styleable.DragSortListView_max_drag_scroll_speed,
                    mMaxScrollSpeed);

            removeAnimDuration = a.getInt(
                    R.styleable.DragSortListView_remove_animation_duration,
                    removeAnimDuration);

            dropAnimDuration = a.getInt(
                    R.styleable.DragSortListView_drop_animation_duration,
                    dropAnimDuration);

            boolean useDefault = a.getBoolean(
                    R.styleable.DragSortListView_use_default_controller,
                    true);

            if (useDefault) {
                boolean removeEnabled = a.getBoolean(
                        R.styleable.DragSortListView_remove_enabled,
                        false);
                int removeMode = a.getInt(
                        R.styleable.DragSortListView_remove_mode,
                        DragSortController.FLING_REMOVE);
                boolean sortEnabled = a.getBoolean(
                        R.styleable.DragSortListView_sort_enabled,
                        true);
                int dragInitMode = a.getInt(
                        R.styleable.DragSortListView_drag_start_mode,
                        DragSortController.ON_DOWN);
                int dragHandleId = a.getResourceId(
                        R.styleable.DragSortListView_drag_handle_id,
                        0);
                int flingHandleId = a.getResourceId(
                        R.styleable.DragSortListView_fling_handle_id,
                        0);
                int clickRemoveId = a.getResourceId(
                        R.styleable.DragSortListView_click_remove_id,
                        0);
                int bgColor = a.getColor(
                        R.styleable.DragSortListView_float_background_color,
                        Color.BLACK);

                DragSortController controller = new DragSortController(
                        this, dragHandleId, dragInitMode, removeMode,
                        clickRemoveId, flingHandleId);
                controller.setRemoveEnabled(removeEnabled);
                controller.setSortEnabled(sortEnabled);
                controller.setBackgroundColor(bgColor);

                mFloatViewManager = controller;
                setOnTouchListener(controller);
            }

            a.recycle();
        }

        mDragScroller = new DragScroller();

        float smoothness = 0.5f;
        if (removeAnimDuration > 0) {
            mRemoveAnimator = new RemoveAnimator(smoothness, removeAnimDuration);
        }
        // mLiftAnimator = new LiftAnimator(smoothness, 100);
        if (dropAnimDuration > 0) {
            mDropAnimator = new DropAnimator(smoothness, dropAnimDuration);
        }

        mCancelEvent = MotionEvent.obtain(0, 0, MotionEvent.ACTION_CANCEL, 0f, 0f, 0f, 0f, 0, 0f,
                0f, 0, 0);

        // construct the dataset observer
        mObserver = new DataSetObserver() {
            private void cancel() {
                if (mDragState == DRAGGING) {
                    cancelDrag();
                }
            }

            @Override
            public void onChanged() {
                cancel();
            }

            @Override
            public void onInvalidated() {
                cancel();
            }
        };
    }

    /**
     * Usually called from a FloatViewManager. The float alpha
     * will be reset to the xml-defined value every time a drag
     * is stopped.
     */
    public void setFloatAlpha(float alpha) {
        mCurrFloatAlpha = alpha;
    }

    public float getFloatAlpha() {
        return mCurrFloatAlpha;
    }

    /**
     * Set maximum drag scroll speed in positions/second. Only applies
     * if using default ScrollSpeedProfile.
     * 
     * @param max Maximum scroll speed.
     */
    public void setMaxScrollSpeed(float max) {
        mMaxScrollSpeed = max;
    }

    /**
     * For each DragSortListView Listener interface implemented by
     * <code>adapter</code>, this method calls the appropriate
     * set*Listener method with <code>adapter</code> as the argument.
     * 
     * @param adapter The ListAdapter providing data to back
     * DragSortListView.
     *
     * @see android.widget.ListView#setAdapter(android.widget.ListAdapter)
     */
    @Override
    public void setAdapter(ListAdapter adapter) {
        if (adapter != null) {
            mAdapterWrapper = new AdapterWrapper(adapter);
            adapter.registerDataSetObserver(mObserver);

            if (adapter instanceof DropListener) {
                setDropListener((DropListener) adapter);
            }
            if (adapter instanceof DragListener) {
                setDragListener((DragListener) adapter);
            }
            if (adapter instanceof RemoveListener) {
                setRemoveListener((RemoveListener) adapter);
            }
        } else {
            mAdapterWrapper = null;
        }

        super.setAdapter(mAdapterWrapper);
    }

    /**
     * As opposed to {@link ListView#getAdapter()}, which returns
     * a heavily wrapped ListAdapter (DragSortListView wraps the
     * input ListAdapter {\emph and} ListView wraps the wrapped one).
     *
     * @return The ListAdapter set as the argument of {@link setAdapter()}
     */
    public ListAdapter getInputAdapter() {
        if (mAdapterWrapper == null) {
            return null;
        } else {
            return mAdapterWrapper.getAdapter();
        }
    }

    private class AdapterWrapper extends BaseAdapter {
        private ListAdapter mAdapter;

        public AdapterWrapper(ListAdapter adapter) {
            super();
            mAdapter = adapter;
            
            mAdapter.registerDataSetObserver(new DataSetObserver() {
                public void onChanged() {
                    notifyDataSetChanged();
                }

                public void onInvalidated() {
                    notifyDataSetInvalidated();
                }
            });
        }

        public ListAdapter getAdapter() {
            return mAdapter;
        }

        @Override
        public long getItemId(int position) {
            return mAdapter.getItemId(position);
        }

        @Override
        public Object getItem(int position) {
            return mAdapter.getItem(position);
        }

        @Override
        public int getCount() {
            return mAdapter.getCount();
        }

        @Override
        public boolean areAllItemsEnabled() {
            return mAdapter.areAllItemsEnabled();
        }

        @Override
        public boolean isEnabled(int position) {
            return mAdapter.isEnabled(position);
        }
        
        @Override
        public int getItemViewType(int position) {
            return mAdapter.getItemViewType(position);
        }

        @Override
        public int getViewTypeCount() {
            return mAdapter.getViewTypeCount();
        }
        
        @Override
        public boolean hasStableIds() {
            return mAdapter.hasStableIds();
        }
        
        @Override
        public boolean isEmpty() {
            return mAdapter.isEmpty();
        }


        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            DragSortItemView v;
            View child;
            // Log.d("mobeta",
            // "getView: position="+position+" convertView="+convertView);
            if (convertView != null) {
                v = (DragSortItemView) convertView;
                View oldChild = v.getChildAt(0);

                child = mAdapter.getView(position, oldChild, DragSortListView.this);
                if (child != oldChild) {
                    // shouldn't get here if user is reusing convertViews
                    // properly
                    if (oldChild != null) {
                        v.removeViewAt(0);
                    }
                    v.addView(child);
                }
            } else {
                child = mAdapter.getView(position, null, DragSortListView.this);
                if (child instanceof Checkable) {
                    v = new DragSortItemViewCheckable(getContext());
                } else {
                    v = new DragSortItemView(getContext());
                }
                v.setLayoutParams(new AbsListView.LayoutParams(
                        ViewGroup.LayoutParams.FILL_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));
                v.addView(child);
            }

            // Set the correct item height given drag state; passed
            // View needs to be measured if measurement is required.
            adjustItem(position + getHeaderViewsCount(), v, true);

            return v;
        }
    }

    private void drawDivider(int expPosition, Canvas canvas) {

        final Drawable divider = getDivider();
        final int dividerHeight = getDividerHeight();
        // Log.d("mobeta", "div="+divider+" divH="+dividerHeight);

        if (divider != null && dividerHeight != 0) {
            final ViewGroup expItem = (ViewGroup) getChildAt(expPosition
                    - getFirstVisiblePosition());
            if (expItem != null) {
                final int l = getPaddingLeft();
                final int r = getWidth() - getPaddingRight();
                final int t;
                final int b;

                final int childHeight = expItem.getChildAt(0).getHeight();

                if (expPosition > mSrcPos) {
                    t = expItem.getTop() + childHeight;
                    b = t + dividerHeight;
                } else {
                    b = expItem.getBottom() - childHeight;
                    t = b - dividerHeight;
                }
                // Log.d("mobeta", "l="+l+" t="+t+" r="+r+" b="+b);

                // Have to clip to support ColorDrawable on <= Gingerbread
                canvas.save();
                canvas.clipRect(l, t, r, b);
                divider.setBounds(l, t, r, b);
                divider.draw(canvas);
                canvas.restore();
            }
        }
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);

        if (mDragState != IDLE) {
            // draw the divider over the expanded item
            if (mFirstExpPos != mSrcPos) {
                drawDivider(mFirstExpPos, canvas);
            }
            if (mSecondExpPos != mFirstExpPos && mSecondExpPos != mSrcPos) {
                drawDivider(mSecondExpPos, canvas);
            }
        }

        if (mFloatView != null) {
            // draw the float view over everything
            final int w = mFloatView.getWidth();
            final int h = mFloatView.getHeight();

            int x = mFloatLoc.x;

            int width = getWidth();
            if (x < 0)
                x = -x;
            float alphaMod;
            if (x < width) {
                alphaMod = ((float) (width - x)) / ((float) width);
                alphaMod *= alphaMod;
            } else {
                alphaMod = 0;
            }

            final int alpha = (int) (255f * mCurrFloatAlpha * alphaMod);

            canvas.save();
            // Log.d("mobeta", "clip rect bounds: " + canvas.getClipBounds());
            canvas.translate(mFloatLoc.x, mFloatLoc.y);
            canvas.clipRect(0, 0, w, h);

            // Log.d("mobeta", "clip rect bounds: " + canvas.getClipBounds());
            canvas.saveLayerAlpha(0, 0, w, h, alpha, Canvas.ALL_SAVE_FLAG);
            mFloatView.draw(canvas);
            canvas.restore();
            canvas.restore();
        }
    }

    private int getItemHeight(int position) {
        View v = getChildAt(position - getFirstVisiblePosition());

        if (v != null) {
            // item is onscreen, just get the height of the View
            return v.getHeight();
        } else {
            // item is offscreen. get child height and calculate
            // item height based on current shuffle state
            return calcItemHeight(position, getChildHeight(position));
        }
    }

    private void printPosData() {
        Log.d("mobeta", "mSrcPos=" + mSrcPos + " mFirstExpPos=" + mFirstExpPos + " mSecondExpPos="
                + mSecondExpPos);
    }

    private class HeightCache {

        private SparseIntArray mMap;
        private ArrayList<Integer> mOrder;
        private int mMaxSize;

        public HeightCache(int size) {
            mMap = new SparseIntArray(size);
            mOrder = new ArrayList<Integer>(size);
            mMaxSize = size;
        }

        /**
         * Add item height at position if doesn't already exist.
         */
        public void add(int position, int height) {
            int currHeight = mMap.get(position, -1);
            if (currHeight != height) {
                if (currHeight == -1) {
                    if (mMap.size() == mMaxSize) {
                        // remove oldest entry
                        mMap.delete(mOrder.remove(0));
                    }
                } else {
                    // move position to newest slot
                    mOrder.remove((Integer) position);
                }
                mMap.put(position, height);
                mOrder.add(position);
            }
        }

        public int get(int position) {
            return mMap.get(position, -1);
        }

        public void clear() {
            mMap.clear();
            mOrder.clear();
        }

    }

    /**
     * Get the shuffle edge for item at position when top of
     * item is at y-coord top. Assumes that current item heights
     * are consistent with current float view location and
     * thus expanded positions and slide fraction. i.e. Should not be
     * called between update of expanded positions/slide fraction
     * and layoutChildren.
     *
     * @param position 
     * @param top
     * @param height Height of item at position. If -1, this function
     * calculates this height.
     *
     * @return Shuffle line between position-1 and position (for
     * the given view of the list; that is, for when top of item at
     * position has y-coord of given `top`). If
     * floating View (treated as horizontal line) is dropped
     * immediately above this line, it lands in position-1. If
     * dropped immediately below this line, it lands in position.
     */
    private int getShuffleEdge(int position, int top) {

        final int numHeaders = getHeaderViewsCount();
        final int numFooters = getFooterViewsCount();

        // shuffle edges are defined between items that can be
        // dragged; there are N-1 of them if there are N draggable
        // items.

        if (position <= numHeaders || (position >= getCount() - numFooters)) {
            return top;
        }

        int divHeight = getDividerHeight();

        int edge;

        int maxBlankHeight = mFloatViewHeight - mItemHeightCollapsed;
        int childHeight = getChildHeight(position);
        int itemHeight = getItemHeight(position);

        // first calculate top of item given that floating View is
        // centered over src position
        int otop = top;
        if (mSecondExpPos <= mSrcPos) {
            // items are expanded on and/or above the source position

            if (position == mSecondExpPos && mFirstExpPos != mSecondExpPos) {
                if (position == mSrcPos) {
                    otop = top + itemHeight - mFloatViewHeight;
                } else {
                    int blankHeight = itemHeight - childHeight;
                    otop = top + blankHeight - maxBlankHeight;
                }
            } else if (position > mSecondExpPos && position <= mSrcPos) {
                otop = top - maxBlankHeight;
            }

        } else {
            // items are expanded on and/or below the source position

            if (position > mSrcPos && position <= mFirstExpPos) {
                otop = top + maxBlankHeight;
            } else if (position == mSecondExpPos && mFirstExpPos != mSecondExpPos) {
                int blankHeight = itemHeight - childHeight;
                otop = top + blankHeight;
            }
        }

        // otop is set
        if (position <= mSrcPos) {
            edge = otop + (mFloatViewHeight - divHeight - getChildHeight(position - 1)) / 2;
        } else {
            edge = otop + (childHeight - divHeight - mFloatViewHeight) / 2;
        }

        return edge;
    }

    private boolean updatePositions() {

        final int first = getFirstVisiblePosition();
        int startPos = mFirstExpPos;
        View startView = getChildAt(startPos - first);

        if (startView == null) {
            startPos = first + getChildCount() / 2;
            startView = getChildAt(startPos - first);
        }
        int startTop = startView.getTop();

        int itemHeight = startView.getHeight();

        int edge = getShuffleEdge(startPos, startTop);
        int lastEdge = edge;

        int divHeight = getDividerHeight();

        // Log.d("mobeta", "float mid="+mFloatViewMid);

        int itemPos = startPos;
        int itemTop = startTop;
        if (mFloatViewMid < edge) {
            // scanning up for float position
            // Log.d("mobeta", "    edge="+edge);
            while (itemPos >= 0) {
                itemPos--;
                itemHeight = getItemHeight(itemPos);

                if (itemPos == 0) {
                    edge = itemTop - divHeight - itemHeight;
                    break;
                }

                itemTop -= itemHeight + divHeight;
                edge = getShuffleEdge(itemPos, itemTop);
                // Log.d("mobeta", "    edge="+edge);

                if (mFloatViewMid >= edge) {
                    break;
                }

                lastEdge = edge;
            }
        } else {
            // scanning down for float position
            // Log.d("mobeta", "    edge="+edge);
            final int count = getCount();
            while (itemPos < count) {
                if (itemPos == count - 1) {
                    edge = itemTop + divHeight + itemHeight;
                    break;
                }

                itemTop += divHeight + itemHeight;
                itemHeight = getItemHeight(itemPos + 1);
                edge = getShuffleEdge(itemPos + 1, itemTop);
                // Log.d("mobeta", "    edge="+edge);

                // test for hit
                if (mFloatViewMid < edge) {
                    break;
                }

                lastEdge = edge;
                itemPos++;
            }
        }

        final int numHeaders = getHeaderViewsCount();
        final int numFooters = getFooterViewsCount();

        boolean updated = false;

        int oldFirstExpPos = mFirstExpPos;
        int oldSecondExpPos = mSecondExpPos;
        float oldSlideFrac = mSlideFrac;

        if (mAnimate) {
            int edgeToEdge = Math.abs(edge - lastEdge);

            int edgeTop, edgeBottom;
            if (mFloatViewMid < edge) {
                edgeBottom = edge;
                edgeTop = lastEdge;
            } else {
                edgeTop = edge;
                edgeBottom = lastEdge;
            }
            // Log.d("mobeta", "edgeTop="+edgeTop+" edgeBot="+edgeBottom);

            int slideRgnHeight = (int) (0.5f * mSlideRegionFrac * edgeToEdge);
            float slideRgnHeightF = (float) slideRgnHeight;
            int slideEdgeTop = edgeTop + slideRgnHeight;
            int slideEdgeBottom = edgeBottom - slideRgnHeight;

            // Three regions
            if (mFloatViewMid < slideEdgeTop) {
                mFirstExpPos = itemPos - 1;
                mSecondExpPos = itemPos;
                mSlideFrac = 0.5f * ((float) (slideEdgeTop - mFloatViewMid)) / slideRgnHeightF;
                // Log.d("mobeta",
                // "firstExp="+mFirstExpPos+" secExp="+mSecondExpPos+" slideFrac="+mSlideFrac);
            } else if (mFloatViewMid < slideEdgeBottom) {
                mFirstExpPos = itemPos;
                mSecondExpPos = itemPos;
            } else {
                mFirstExpPos = itemPos;
                mSecondExpPos = itemPos + 1;
                mSlideFrac = 0.5f * (1.0f + ((float) (edgeBottom - mFloatViewMid))
                        / slideRgnHeightF);
                // Log.d("mobeta",
                // "firstExp="+mFirstExpPos+" secExp="+mSecondExpPos+" slideFrac="+mSlideFrac);
            }

        } else {
            mFirstExpPos = itemPos;
            mSecondExpPos = itemPos;
        }

        // correct for headers and footers
        if (mFirstExpPos < numHeaders) {
            itemPos = numHeaders;
            mFirstExpPos = itemPos;
            mSecondExpPos = itemPos;
        } else if (mSecondExpPos >= getCount() - numFooters) {
            itemPos = getCount() - numFooters - 1;
            mFirstExpPos = itemPos;
            mSecondExpPos = itemPos;
        }

        if (mFirstExpPos != oldFirstExpPos || mSecondExpPos != oldSecondExpPos
                || mSlideFrac != oldSlideFrac) {
            updated = true;
        }

        if (itemPos != mFloatPos) {
            if (mDragListener != null) {
                mDragListener.drag(mFloatPos - numHeaders, itemPos - numHeaders);
            }

            mFloatPos = itemPos;
            updated = true;
        }

        return updated;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mTrackDragSort) {
            mDragSortTracker.appendState();
        }
    }

    private class SmoothAnimator implements Runnable {
        protected long mStartTime;

        private float mDurationF;

        private float mAlpha;
        private float mA, mB, mC, mD;

        private boolean mCanceled;

        public SmoothAnimator(float smoothness, int duration) {
            mAlpha = smoothness;
            mDurationF = (float) duration;
            mA = mD = 1f / (2f * mAlpha * (1f - mAlpha));
            mB = mAlpha / (2f * (mAlpha - 1f));
            mC = 1f / (1f - mAlpha);
        }

        public float transform(float frac) {
            if (frac < mAlpha) {
                return mA * frac * frac;
            } else if (frac < 1f - mAlpha) {
                return mB + mC * frac;
            } else {
                return 1f - mD * (frac - 1f) * (frac - 1f);
            }
        }

        public void start() {
            mStartTime = SystemClock.uptimeMillis();
            mCanceled = false;
            onStart();
            post(this);
        }

        public void cancel() {
            mCanceled = true;
        }

        public void onStart() {
            // stub
        }

        public void onUpdate(float frac, float smoothFrac) {
            // stub
        }

        public void onStop() {
            // stub
        }

        @Override
        public void run() {
            if (mCanceled) {
                return;
            }

            float fraction = ((float) (SystemClock.uptimeMillis() - mStartTime)) / mDurationF;

            if (fraction >= 1f) {
                onUpdate(1f, 1f);
                onStop();
            } else {
                onUpdate(fraction, transform(fraction));
                post(this);
            }
        }
    }

    /**
     * Centers floating View under touch point.
     */
    private class LiftAnimator extends SmoothAnimator {

        private float mInitDragDeltaY;
        private float mFinalDragDeltaY;

        public LiftAnimator(float smoothness, int duration) {
            super(smoothness, duration);
        }

        @Override
        public void onStart() {
            mInitDragDeltaY = mDragDeltaY;
            mFinalDragDeltaY = mFloatViewHeightHalf;
        }

        @Override
        public void onUpdate(float frac, float smoothFrac) {
            if (mDragState != DRAGGING) {
                cancel();
            } else {
                mDragDeltaY = (int) (smoothFrac * mFinalDragDeltaY + (1f - smoothFrac)
                        * mInitDragDeltaY);
                mFloatLoc.y = mY - mDragDeltaY;
                doDragFloatView(true);
            }
        }
    }

    /**
     * Centers floating View over drop slot before destroying.
     */
    private class DropAnimator extends SmoothAnimator {

        private int mDropPos;
        private int srcPos;
        private float mInitDeltaY;
        private float mInitDeltaX;

        public DropAnimator(float smoothness, int duration) {
            super(smoothness, duration);
        }

        @Override
        public void onStart() {
            mDropPos = mFloatPos;
            srcPos = mSrcPos;
            mDragState = DROPPING;
            mInitDeltaY = mFloatLoc.y - getTargetY();
            mInitDeltaX = mFloatLoc.x - getPaddingLeft();
        }

        private int getTargetY() {
            final int first = getFirstVisiblePosition();
            final int otherAdjust = (mItemHeightCollapsed + getDividerHeight()) / 2;
            View v = getChildAt(mDropPos - first);
            int targetY = -1;
            if (v != null) {
                if (mDropPos == srcPos) {
                    targetY = v.getTop();
                } else if (mDropPos < srcPos) {
                    // expanded down
                    targetY = v.getTop() - otherAdjust;
                } else {
                    // expanded up
                    targetY = v.getBottom() + otherAdjust - mFloatViewHeight;
                }
            } else {
                // drop position is not on screen?? no animation
                cancel();
            }

            return targetY;
        }

        @Override
        public void onUpdate(float frac, float smoothFrac) {
            final int targetY = getTargetY();
            final int targetX = getPaddingLeft();
            final float deltaY = mFloatLoc.y - targetY;
            final float deltaX = mFloatLoc.x - targetX;
            final float f = 1f - smoothFrac;
            if (f < Math.abs(deltaY / mInitDeltaY) || f < Math.abs(deltaX / mInitDeltaX)) {
                mFloatLoc.y = targetY + (int) (mInitDeltaY * f);
                mFloatLoc.x = getPaddingLeft() + (int) (mInitDeltaX * f);
                doDragFloatView(true);
            }
        }

        @Override
        public void onStop() {
            dropFloatView();
        }

    }

    /**
     * Collapses expanded items.
     */
    private class RemoveAnimator extends SmoothAnimator {

        private float mFloatLocX;
        private float mFirstStartBlank;
        private float mSecondStartBlank;

        private int mFirstChildHeight = -1;
        private int mSecondChildHeight = -1;

        private int mFirstPos;
        private int mSecondPos;
        private int srcPos;

        public RemoveAnimator(float smoothness, int duration) {
            super(smoothness, duration);
        }

        @Override
        public void onStart() {
            mFirstChildHeight = -1;
            mSecondChildHeight = -1;
            mFirstPos = mFirstExpPos;
            mSecondPos = mSecondExpPos;
            srcPos = mSrcPos;
            mDragState = REMOVING;

            mFloatLocX = mFloatLoc.x;
            if (mUseRemoveVelocity) {
                float minVelocity = 2f * getWidth();
                if (mRemoveVelocityX == 0) {
                    mRemoveVelocityX = (mFloatLocX < 0 ? -1 : 1) * minVelocity;
                } else {
                    minVelocity *= 2;
                    if (mRemoveVelocityX < 0 && mRemoveVelocityX > -minVelocity)
                        mRemoveVelocityX = -minVelocity;
                    else if (mRemoveVelocityX > 0 && mRemoveVelocityX < minVelocity)
                        mRemoveVelocityX = minVelocity;
                }
            } else {
                destroyFloatView();
            }
        }

        @Override
        public void onUpdate(float frac, float smoothFrac) {
            float f = 1f - smoothFrac;

            final int firstVis = getFirstVisiblePosition();
            View item = getChildAt(mFirstPos - firstVis);
            ViewGroup.LayoutParams lp;
            int blank;

            if (mUseRemoveVelocity) {
                float dt = (float) (SystemClock.uptimeMillis() - mStartTime) / 1000;
                if (dt == 0)
                    return;
                float dx = mRemoveVelocityX * dt;
                int w = getWidth();
                mRemoveVelocityX += (mRemoveVelocityX > 0 ? 1 : -1) * dt * w;
                mFloatLocX += dx;
                mFloatLoc.x = (int) mFloatLocX;
                if (mFloatLocX < w && mFloatLocX > -w) {
                    mStartTime = SystemClock.uptimeMillis();
                    doDragFloatView(true);
                    return;
                }
            }

            if (item != null) {
                if (mFirstChildHeight == -1) {
                    mFirstChildHeight = getChildHeight(mFirstPos, item, false);
                    mFirstStartBlank = (float) (item.getHeight() - mFirstChildHeight);
                }
                blank = Math.max((int) (f * mFirstStartBlank), 1);
                lp = item.getLayoutParams();
                lp.height = mFirstChildHeight + blank;
                item.setLayoutParams(lp);
            }
            if (mSecondPos != mFirstPos) {
                item = getChildAt(mSecondPos - firstVis);
                if (item != null) {
                    if (mSecondChildHeight == -1) {
                        mSecondChildHeight = getChildHeight(mSecondPos, item, false);
                        mSecondStartBlank = (float) (item.getHeight() - mSecondChildHeight);
                    }
                    blank = Math.max((int) (f * mSecondStartBlank), 1);
                    lp = item.getLayoutParams();
                    lp.height = mSecondChildHeight + blank;
                    item.setLayoutParams(lp);
                }
            }
        }

        @Override
        public void onStop() {
            doRemoveItem();
        }
    }

    public void removeItem(int which) {

        mUseRemoveVelocity = false;
        removeItem(which, 0);
    }

    /**
     * Removes an item from the list and animates the removal.
     *
     * @param which Position to remove (NOTE: headers/footers ignored!
     * this is a position in your input ListAdapter).
     * @param velocityX 
     */
    public void removeItem(int which, float velocityX) {
        if (mDragState == IDLE || mDragState == DRAGGING) {

            if (mDragState == IDLE) {
                // called from outside drag-sort
                mSrcPos = getHeaderViewsCount() + which;
                mFirstExpPos = mSrcPos;
                mSecondExpPos = mSrcPos;
                mFloatPos = mSrcPos;
                View v = getChildAt(mSrcPos - getFirstVisiblePosition());
                if (v != null) {
                    v.setVisibility(View.INVISIBLE);
                }
            }

            mDragState = REMOVING;
            mRemoveVelocityX = velocityX;

            if (mInTouchEvent) {
                switch (mCancelMethod) {
                    case ON_TOUCH_EVENT:
                        super.onTouchEvent(mCancelEvent);
                        break;
                    case ON_INTERCEPT_TOUCH_EVENT:
                        super.onInterceptTouchEvent(mCancelEvent);
                        break;
                }
            }

            if (mRemoveAnimator != null) {
                mRemoveAnimator.start();
            } else {
                doRemoveItem(which);
            }
        }
    }

    /**
     * Move an item, bypassing the drag-sort process. Simply calls
     * through to {@link DropListener#drop(int, int)}.
     * 
     * @param from Position to move (NOTE: headers/footers ignored!
     * this is a position in your input ListAdapter).
     * @param to Target position (NOTE: headers/footers ignored!
     * this is a position in your input ListAdapter).
     */
    public void moveItem(int from, int to) {
        if (mDropListener != null) {
            final int count = getInputAdapter().getCount();
            if (from >= 0 && from < count && to >= 0 && to < count) {
                mDropListener.drop(from, to);
            }
        }
    }

    /**
     * Cancel a drag. Calls {@link #stopDrag(boolean, boolean)} with
     * <code>true</code> as the first argument.
     */
    public void cancelDrag() {
        if (mDragState == DRAGGING) {
            mDragScroller.stopScrolling(true);
            destroyFloatView();
            clearPositions();
            adjustAllItems();

            if (mInTouchEvent) {
                mDragState = STOPPED;
            } else {
                mDragState = IDLE;
            }
        }
    }

    private void clearPositions() {
        mSrcPos = -1;
        mFirstExpPos = -1;
        mSecondExpPos = -1;
        mFloatPos = -1;
    }

    private void dropFloatView() {
        // must set to avoid cancelDrag being called from the
        // DataSetObserver
        mDragState = DROPPING;

        if (mDropListener != null && mFloatPos >= 0 && mFloatPos < getCount()) {
            final int numHeaders = getHeaderViewsCount();
            mDropListener.drop(mSrcPos - numHeaders, mFloatPos - numHeaders);
        }

        destroyFloatView();

        adjustOnReorder();
        clearPositions();
        adjustAllItems();

        // now the drag is done
        if (mInTouchEvent) {
            mDragState = STOPPED;
        } else {
            mDragState = IDLE;
        }
    }

    private void doRemoveItem() {
        doRemoveItem(mSrcPos - getHeaderViewsCount());
    }

    /**
     * Removes dragged item from the list. Calls RemoveListener.
     */
    private void doRemoveItem(int which) {
        // must set to avoid cancelDrag being called from the
        // DataSetObserver
        mDragState = REMOVING;

        // end it
        if (mRemoveListener != null) {
            mRemoveListener.remove(which);
        }

        destroyFloatView();

        adjustOnReorder();
        clearPositions();

        // now the drag is done
        if (mInTouchEvent) {
            mDragState = STOPPED;
        } else {
            mDragState = IDLE;
        }
    }

    private void adjustOnReorder() {
        final int firstPos = getFirstVisiblePosition();
        // Log.d("mobeta", "first="+firstPos+" src="+mSrcPos);
        if (mSrcPos < firstPos) {
            // collapsed src item is off screen;
            // adjust the scroll after item heights have been fixed
            View v = getChildAt(0);
            int top = 0;
            if (v != null) {
                top = v.getTop();
            }
            // Log.d("mobeta", "top="+top+" fvh="+mFloatViewHeight);
            setSelectionFromTop(firstPos - 1, top - getPaddingTop());
        }
    }

    /**
     * Stop a drag in progress. Pass <code>true</code> if you would
     * like to remove the dragged item from the list.
     *
     * @param remove Remove the dragged item from the list. Calls
     * a registered RemoveListener, if one exists. Otherwise, calls
     * the DropListener, if one exists.
     *
     * @return True if the stop was successful. False if there is
     * no floating View.
     */
    public boolean stopDrag(boolean remove) {
        mUseRemoveVelocity = false;
        return stopDrag(remove, 0);
    }

    public boolean stopDragWithVelocity(boolean remove, float velocityX) {

        mUseRemoveVelocity = true;
        return stopDrag(remove, velocityX);
    }

    public boolean stopDrag(boolean remove, float velocityX) {
        if (mFloatView != null) {
            mDragScroller.stopScrolling(true);

            if (remove) {
                removeItem(mSrcPos - getHeaderViewsCount(), velocityX);
            } else {
                if (mDropAnimator != null) {
                    mDropAnimator.start();
                } else {
                    dropFloatView();
                }
            }

            if (mTrackDragSort) {
                mDragSortTracker.stopTracking();
            }

            return true;
        } else {
            // stop failed
            return false;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (mIgnoreTouchEvent) {
            mIgnoreTouchEvent = false;
            return false;
        }

        if (!mDragEnabled) {
            return super.onTouchEvent(ev);
        }

        boolean more = false;

        boolean lastCallWasIntercept = mLastCallWasIntercept;
        mLastCallWasIntercept = false;

        if (!lastCallWasIntercept) {
            saveTouchCoords(ev);
        }

        // if (mFloatView != null) {
        if (mDragState == DRAGGING) {
            onDragTouchEvent(ev);
            more = true; // give us more!
        } else {
            // what if float view is null b/c we dropped in middle
            // of drag touch event?

            // if (mDragState != STOPPED) {
            if (mDragState == IDLE) {
                if (super.onTouchEvent(ev)) {
                    more = true;
                }
            }

            int action = ev.getAction() & MotionEvent.ACTION_MASK;

            switch (action) {
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
                    doActionUpOrCancel();
                    break;
                default:
                    if (more) {
                        mCancelMethod = ON_TOUCH_EVENT;
                    }
            }
        }

        return more;
    }

    private void doActionUpOrCancel() {
        mCancelMethod = NO_CANCEL;
        mInTouchEvent = false;
        if (mDragState == STOPPED) {
            mDragState = IDLE;
        }
        mCurrFloatAlpha = mFloatAlpha;
        mListViewIntercepted = false;
        mChildHeightCache.clear();
    }

    private void saveTouchCoords(MotionEvent ev) {
        int action = ev.getAction() & MotionEvent.ACTION_MASK;
        if (action != MotionEvent.ACTION_DOWN) {
            mLastX = mX;
            mLastY = mY;
        }
        mX = (int) ev.getX();
        mY = (int) ev.getY();
        if (action == MotionEvent.ACTION_DOWN) {
            mLastX = mX;
            mLastY = mY;
        }
        mOffsetX = (int) ev.getRawX() - mX;
        mOffsetY = (int) ev.getRawY() - mY;
    }

    public boolean listViewIntercepted() {
        return mListViewIntercepted;
    }

    private boolean mListViewIntercepted = false;

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (!mDragEnabled) {
            return super.onInterceptTouchEvent(ev);
        }

        saveTouchCoords(ev);
        mLastCallWasIntercept = true;

        int action = ev.getAction() & MotionEvent.ACTION_MASK;

        if (action == MotionEvent.ACTION_DOWN) {
            if (mDragState != IDLE) {
                // intercept and ignore
                mIgnoreTouchEvent = true;
                return true;
            }
            mInTouchEvent = true;
        }

        boolean intercept = false;

        // the following deals with calls to super.onInterceptTouchEvent
        if (mFloatView != null) {
            // super's touch event canceled in startDrag
            intercept = true;
        } else {
            if (super.onInterceptTouchEvent(ev)) {
                mListViewIntercepted = true;
                intercept = true;
            }

            switch (action) {
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
                    doActionUpOrCancel();
                    break;
                default:
                    if (intercept) {
                        mCancelMethod = ON_TOUCH_EVENT;
                    } else {
                        mCancelMethod = ON_INTERCEPT_TOUCH_EVENT;
                    }
            }
        }

        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
            mInTouchEvent = false;
        }

        return intercept;
    }

    /**
     * Set the width of each drag scroll region by specifying
     * a fraction of the ListView height.
     *
     * @param heightFraction Fraction of ListView height. Capped at
     * 0.5f.
     * 
     */
    public void setDragScrollStart(float heightFraction) {
        setDragScrollStarts(heightFraction, heightFraction);
    }

    /**
     * Set the width of each drag scroll region by specifying
     * a fraction of the ListView height.
     *
     * @param upperFrac Fraction of ListView height for up-scroll bound.
     * Capped at 0.5f.
     * @param lowerFrac Fraction of ListView height for down-scroll bound.
     * Capped at 0.5f.
     * 
     */
    public void setDragScrollStarts(float upperFrac, float lowerFrac) {
        if (lowerFrac > 0.5f) {
            mDragDownScrollStartFrac = 0.5f;
        } else {
            mDragDownScrollStartFrac = lowerFrac;
        }

        if (upperFrac > 0.5f) {
            mDragUpScrollStartFrac = 0.5f;
        } else {
            mDragUpScrollStartFrac = upperFrac;
        }

        if (getHeight() != 0) {
            updateScrollStarts();
        }
    }

    private void continueDrag(int x, int y) {

        // proposed position
        mFloatLoc.x = x - mDragDeltaX;
        mFloatLoc.y = y - mDragDeltaY;

        doDragFloatView(true);

        int minY = Math.min(y, mFloatViewMid + mFloatViewHeightHalf);
        int maxY = Math.max(y, mFloatViewMid - mFloatViewHeightHalf);

        // get the current scroll direction
        int currentScrollDir = mDragScroller.getScrollDir();

        if (minY > mLastY && minY > mDownScrollStartY && currentScrollDir != DragScroller.DOWN) {
            // dragged down, it is below the down scroll start and it is not
            // scrolling up

            if (currentScrollDir != DragScroller.STOP) {
                // moved directly from up scroll to down scroll
                mDragScroller.stopScrolling(true);
            }

            // start scrolling down
            mDragScroller.startScrolling(DragScroller.DOWN);
        } else if (maxY < mLastY && maxY < mUpScrollStartY && currentScrollDir != DragScroller.UP) {
            // dragged up, it is above the up scroll start and it is not
            // scrolling up

            if (currentScrollDir != DragScroller.STOP) {
                // moved directly from down scroll to up scroll
                mDragScroller.stopScrolling(true);
            }

            // start scrolling up
            mDragScroller.startScrolling(DragScroller.UP);
        }
        else if (maxY >= mUpScrollStartY && minY <= mDownScrollStartY
                && mDragScroller.isScrolling()) {
            // not in the upper nor in the lower drag-scroll regions but it is
            // still scrolling

            mDragScroller.stopScrolling(true);
        }
    }

    private void updateScrollStarts() {
        final int padTop = getPaddingTop();
        final int listHeight = getHeight() - padTop - getPaddingBottom();
        float heightF = (float) listHeight;

        mUpScrollStartYF = padTop + mDragUpScrollStartFrac * heightF;
        mDownScrollStartYF = padTop + (1.0f - mDragDownScrollStartFrac) * heightF;

        mUpScrollStartY = (int) mUpScrollStartYF;
        mDownScrollStartY = (int) mDownScrollStartYF;

        mDragUpScrollHeight = mUpScrollStartYF - padTop;
        mDragDownScrollHeight = padTop + listHeight - mDownScrollStartYF;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        updateScrollStarts();
    }

    private void adjustAllItems() {
        final int first = getFirstVisiblePosition();
        final int last = getLastVisiblePosition();

        int begin = Math.max(0, getHeaderViewsCount() - first);
        int end = Math.min(last - first, getCount() - 1 - getFooterViewsCount() - first);

        for (int i = begin; i <= end; ++i) {
            View v = getChildAt(i);
            if (v != null) {
                adjustItem(first + i, v, false);
            }
        }
    }

    private void adjustItem(int position) {
        View v = getChildAt(position - getFirstVisiblePosition());

        if (v != null) {
            adjustItem(position, v, false);
        }
    }

    /**
     * Sets layout param height, gravity, and visibility  on
     * wrapped item.
     */
    private void adjustItem(int position, View v, boolean invalidChildHeight) {

        // Adjust item height
        ViewGroup.LayoutParams lp = v.getLayoutParams();
        int height;
        if (position != mSrcPos && position != mFirstExpPos && position != mSecondExpPos) {
            height = ViewGroup.LayoutParams.WRAP_CONTENT;
        } else {
            height = calcItemHeight(position, v, invalidChildHeight);
        }

        if (height != lp.height) {
            lp.height = height;
            v.setLayoutParams(lp);
        }

        // Adjust item gravity
        if (position == mFirstExpPos || position == mSecondExpPos) {
            if (position < mSrcPos) {
                ((DragSortItemView) v).setGravity(Gravity.BOTTOM);
            } else if (position > mSrcPos) {
                ((DragSortItemView) v).setGravity(Gravity.TOP);
            }
        }

        // Finally adjust item visibility

        int oldVis = v.getVisibility();
        int vis = View.VISIBLE;

        if (position == mSrcPos && mFloatView != null) {
            vis = View.INVISIBLE;
        }

        if (vis != oldVis) {
            v.setVisibility(vis);
        }
    }

    private int getChildHeight(int position) {
        if (position == mSrcPos) {
            return 0;
        }

        View v = getChildAt(position - getFirstVisiblePosition());

        if (v != null) {
            // item is onscreen, therefore child height is valid,
            // hence the "true"
            return getChildHeight(position, v, false);
        } else {
            // item is offscreen
            // first check cache for child height at this position
            int childHeight = mChildHeightCache.get(position);
            if (childHeight != -1) {
                // Log.d("mobeta", "found child height in cache!");
                return childHeight;
            }

            final ListAdapter adapter = getAdapter();
            int type = adapter.getItemViewType(position);

            // There might be a better place for checking for the following
            final int typeCount = adapter.getViewTypeCount();
            if (typeCount != mSampleViewTypes.length) {
                mSampleViewTypes = new View[typeCount];
            }

            if (type >= 0) {
                if (mSampleViewTypes[type] == null) {
                    v = adapter.getView(position, null, this);
                    mSampleViewTypes[type] = v;
                } else {
                    v = adapter.getView(position, mSampleViewTypes[type], this);
                }
            } else {
                // type is HEADER_OR_FOOTER or IGNORE
                v = adapter.getView(position, null, this);
            }

            // current child height is invalid, hence "true" below
            childHeight = getChildHeight(position, v, true);

            // cache it because this could have been expensive
            mChildHeightCache.add(position, childHeight);

            return childHeight;
        }
    }

    private int getChildHeight(int position, View item, boolean invalidChildHeight) {
        if (position == mSrcPos) {
            return 0;
        }

        View child;
        if (position < getHeaderViewsCount() || position >= getCount() - getFooterViewsCount()) {
            child = item;
        } else {
            child = ((ViewGroup) item).getChildAt(0);
        }

        ViewGroup.LayoutParams lp = child.getLayoutParams();

        if (lp != null) {
            if (lp.height > 0) {
                return lp.height;
            }
        }

        int childHeight = child.getHeight();

        if (childHeight == 0 || invalidChildHeight) {
            measureItem(child);
            childHeight = child.getMeasuredHeight();
        }

        return childHeight;
    }

    private int calcItemHeight(int position, View item, boolean invalidChildHeight) {
        return calcItemHeight(position, getChildHeight(position, item, invalidChildHeight));
    }

    private int calcItemHeight(int position, int childHeight) {

        int divHeight = getDividerHeight();

        boolean isSliding = mAnimate && mFirstExpPos != mSecondExpPos;
        int maxNonSrcBlankHeight = mFloatViewHeight - mItemHeightCollapsed;
        int slideHeight = (int) (mSlideFrac * maxNonSrcBlankHeight);

        int height;

        if (position == mSrcPos) {
            if (mSrcPos == mFirstExpPos) {
                if (isSliding) {
                    height = slideHeight + mItemHeightCollapsed;
                } else {
                    height = mFloatViewHeight;
                }
            } else if (mSrcPos == mSecondExpPos) {
                // if gets here, we know an item is sliding
                height = mFloatViewHeight - slideHeight;
            } else {
                height = mItemHeightCollapsed;
            }
        } else if (position == mFirstExpPos) {
            if (isSliding) {
                height = childHeight + slideHeight;
            } else {
                height = childHeight + maxNonSrcBlankHeight;
            }
        } else if (position == mSecondExpPos) {
            // we know an item is sliding (b/c 2ndPos != 1stPos)
            height = childHeight + maxNonSrcBlankHeight - slideHeight;
        } else {
            height = childHeight;
        }

        return height;
    }

    @Override
    public void requestLayout() {
        if (!mBlockLayoutRequests) {
            super.requestLayout();
        }
    }

    private int adjustScroll(int movePos, View moveItem, int oldFirstExpPos, int oldSecondExpPos) {
        int adjust = 0;

        final int childHeight = getChildHeight(movePos);

        int moveHeightBefore = moveItem.getHeight();
        int moveHeightAfter = calcItemHeight(movePos, childHeight);

        int moveBlankBefore = moveHeightBefore;
        int moveBlankAfter = moveHeightAfter;
        if (movePos != mSrcPos) {
            moveBlankBefore -= childHeight;
            moveBlankAfter -= childHeight;
        }

        int maxBlank = mFloatViewHeight;
        if (mSrcPos != mFirstExpPos && mSrcPos != mSecondExpPos) {
            maxBlank -= mItemHeightCollapsed;
        }

        if (movePos <= oldFirstExpPos) {
            if (movePos > mFirstExpPos) {
                adjust += maxBlank - moveBlankAfter;
            }
        } else if (movePos == oldSecondExpPos) {
            if (movePos <= mFirstExpPos) {
                adjust += moveBlankBefore - maxBlank;
            } else if (movePos == mSecondExpPos) {
                adjust += moveHeightBefore - moveHeightAfter;
            } else {
                adjust += moveBlankBefore;
            }
        } else {
            if (movePos <= mFirstExpPos) {
                adjust -= maxBlank;
            } else if (movePos == mSecondExpPos) {
                adjust -= moveBlankAfter;
            }
        }

        return adjust;
    }

    private void measureItem(View item) {
        ViewGroup.LayoutParams lp = item.getLayoutParams();
        if (lp == null) {
            lp = new AbsListView.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            item.setLayoutParams(lp);
        }
        int wspec = ViewGroup.getChildMeasureSpec(mWidthMeasureSpec, getListPaddingLeft()
                + getListPaddingRight(), lp.width);
        int hspec;
        if (lp.height > 0) {
            hspec = MeasureSpec.makeMeasureSpec(lp.height, MeasureSpec.EXACTLY);
        } else {
            hspec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        }
        item.measure(wspec, hspec);
    }

    private void measureFloatView() {
        if (mFloatView != null) {
            measureItem(mFloatView);
            mFloatViewHeight = mFloatView.getMeasuredHeight();
            mFloatViewHeightHalf = mFloatViewHeight / 2;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        // Log.d("mobeta", "onMeasure called");
        if (mFloatView != null) {
            if (mFloatView.isLayoutRequested()) {
                measureFloatView();
            }
            mFloatViewOnMeasured = true; // set to false after layout
        }
        mWidthMeasureSpec = widthMeasureSpec;
    }

    @Override
    protected void layoutChildren() {
        super.layoutChildren();

        if (mFloatView != null) {
            if (mFloatView.isLayoutRequested() && !mFloatViewOnMeasured) {
                // Have to measure here when usual android measure
                // pass is skipped. This happens during a drag-sort
                // when layoutChildren is called directly.
                measureFloatView();
            }
            mFloatView.layout(0, 0, mFloatView.getMeasuredWidth(), mFloatView.getMeasuredHeight());
            mFloatViewOnMeasured = false;
        }
    }

    protected boolean onDragTouchEvent(MotionEvent ev) {
        // we are in a drag
        int action = ev.getAction() & MotionEvent.ACTION_MASK;

        switch (ev.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_CANCEL:
                if (mDragState == DRAGGING) {
                    cancelDrag();
                }
                doActionUpOrCancel();
                break;
            case MotionEvent.ACTION_UP:
                // Log.d("mobeta", "calling stopDrag from onDragTouchEvent");
                if (mDragState == DRAGGING) {
                    stopDrag(false);
                }
                doActionUpOrCancel();
                break;
            case MotionEvent.ACTION_MOVE:
                continueDrag((int) ev.getX(), (int) ev.getY());
                break;
        }

        return true;
    }

    private boolean mFloatViewInvalidated = false;

    private void invalidateFloatView() {
        mFloatViewInvalidated = true;
    }

    /**
     * Start a drag of item at <code>position</code> using the
     * registered FloatViewManager. Calls through
     * to {@link #startDrag(int,View,int,int,int)} after obtaining
     * the floating View from the FloatViewManager.
     *
     * @param position Item to drag.
     * @param dragFlags Flags that restrict some movements of the
     * floating View. For example, set <code>dragFlags |= 
     * ~{@link #DRAG_NEG_X}</code> to allow dragging the floating
     * View in all directions except off the screen to the left.
     * @param deltaX Offset in x of the touch coordinate from the
     * left edge of the floating View (i.e. touch-x minus float View
     * left).
     * @param deltaY Offset in y of the touch coordinate from the
     * top edge of the floating View (i.e. touch-y minus float View
     * top).
     *
     * @return True if the drag was started, false otherwise. This
     * <code>startDrag</code> will fail if we are not currently in
     * a touch event, there is no registered FloatViewManager,
     * or the FloatViewManager returns a null View.
     */
    public boolean startDrag(int position, int dragFlags, int deltaX, int deltaY) {
        if (!mInTouchEvent || mFloatViewManager == null) {
            return false;
        }

        View v = mFloatViewManager.onCreateFloatView(position);

        if (v == null) {
            return false;
        } else {
            return startDrag(position, v, dragFlags, deltaX, deltaY);
        }

    }

    /**
     * Start a drag of item at <code>position</code> without using
     * a FloatViewManager.
     *
     * @param position Item to drag.
     * @param floatView Floating View.
     * @param dragFlags Flags that restrict some movements of the
     * floating View. For example, set <code>dragFlags |= 
     * ~{@link #DRAG_NEG_X}</code> to allow dragging the floating
     * View in all directions except off the screen to the left.
     * @param deltaX Offset in x of the touch coordinate from the
     * left edge of the floating View (i.e. touch-x minus float View
     * left).
     * @param deltaY Offset in y of the touch coordinate from the
     * top edge of the floating View (i.e. touch-y minus float View
     * top).
     *
     * @return True if the drag was started, false otherwise. This
     * <code>startDrag</code> will fail if we are not currently in
     * a touch event, <code>floatView</code> is null, or there is
     * a drag in progress.
     */
    public boolean startDrag(int position, View floatView, int dragFlags, int deltaX, int deltaY) {
        if (mDragState != IDLE || !mInTouchEvent || mFloatView != null || floatView == null
                || !mDragEnabled) {
            return false;
        }

        if (getParent() != null) {
            getParent().requestDisallowInterceptTouchEvent(true);
        }

        int pos = position + getHeaderViewsCount();
        mFirstExpPos = pos;
        mSecondExpPos = pos;
        mSrcPos = pos;
        mFloatPos = pos;

        // mDragState = dragType;
        mDragState = DRAGGING;
        mDragFlags = 0;
        mDragFlags |= dragFlags;

        mFloatView = floatView;
        measureFloatView(); // sets mFloatViewHeight

        mDragDeltaX = deltaX;
        mDragDeltaY = deltaY;
        mDragStartY = mY;

        // updateFloatView(mX - mDragDeltaX, mY - mDragDeltaY);
        mFloatLoc.x = mX - mDragDeltaX;
        mFloatLoc.y = mY - mDragDeltaY;

        // set src item invisible
        final View srcItem = getChildAt(mSrcPos - getFirstVisiblePosition());

        if (srcItem != null) {
            srcItem.setVisibility(View.INVISIBLE);
        }

        if (mTrackDragSort) {
            mDragSortTracker.startTracking();
        }

        // once float view is created, events are no longer passed
        // to ListView
        switch (mCancelMethod) {
            case ON_TOUCH_EVENT:
                super.onTouchEvent(mCancelEvent);
                break;
            case ON_INTERCEPT_TOUCH_EVENT:
                super.onInterceptTouchEvent(mCancelEvent);
                break;
        }

        requestLayout();

        if (mLiftAnimator != null) {
            mLiftAnimator.start();
        }

        return true;
    }

    private void doDragFloatView(boolean forceInvalidate) {
        int movePos = getFirstVisiblePosition() + getChildCount() / 2;
        View moveItem = getChildAt(getChildCount() / 2);

        if (moveItem == null) {
            return;
        }

        doDragFloatView(movePos, moveItem, forceInvalidate);
    }

    private void doDragFloatView(int movePos, View moveItem, boolean forceInvalidate) {
        mBlockLayoutRequests = true;

        updateFloatView();

        int oldFirstExpPos = mFirstExpPos;
        int oldSecondExpPos = mSecondExpPos;

        boolean updated = updatePositions();

        if (updated) {
            adjustAllItems();
            int scroll = adjustScroll(movePos, moveItem, oldFirstExpPos, oldSecondExpPos);
            // Log.d("mobeta", "  adjust scroll="+scroll);

            setSelectionFromTop(movePos, moveItem.getTop() + scroll - getPaddingTop());
            layoutChildren();
        }

        if (updated || forceInvalidate) {
            invalidate();
        }

        mBlockLayoutRequests = false;
    }

    /**
     * Sets float View location based on suggested values and
     * constraints set in mDragFlags.
     */
    private void updateFloatView() {

        if (mFloatViewManager != null) {
            mTouchLoc.set(mX, mY);
            mFloatViewManager.onDragFloatView(mFloatView, mFloatLoc, mTouchLoc);
        }

        final int floatX = mFloatLoc.x;
        final int floatY = mFloatLoc.y;

        // restrict x motion
        int padLeft = getPaddingLeft();
        if ((mDragFlags & DRAG_POS_X) == 0 && floatX > padLeft) {
            mFloatLoc.x = padLeft;
        } else if ((mDragFlags & DRAG_NEG_X) == 0 && floatX < padLeft) {
            mFloatLoc.x = padLeft;
        }

        // keep floating view from going past bottom of last header view
        final int numHeaders = getHeaderViewsCount();
        final int numFooters = getFooterViewsCount();
        final int firstPos = getFirstVisiblePosition();
        final int lastPos = getLastVisiblePosition();

        // Log.d("mobeta",
        // "nHead="+numHeaders+" nFoot="+numFooters+" first="+firstPos+" last="+lastPos);
        int topLimit = getPaddingTop();
        if (firstPos < numHeaders) {
            topLimit = getChildAt(numHeaders - firstPos - 1).getBottom();
        }
        if ((mDragFlags & DRAG_NEG_Y) == 0) {
            if (firstPos <= mSrcPos) {
                topLimit = Math.max(getChildAt(mSrcPos - firstPos).getTop(), topLimit);
            }
        }
        // bottom limit is top of first footer View or
        // bottom of last item in list
        int bottomLimit = getHeight() - getPaddingBottom();
        if (lastPos >= getCount() - numFooters - 1) {
            bottomLimit = getChildAt(getCount() - numFooters - 1 - firstPos).getBottom();
        }
        if ((mDragFlags & DRAG_POS_Y) == 0) {
            if (lastPos >= mSrcPos) {
                bottomLimit = Math.min(getChildAt(mSrcPos - firstPos).getBottom(), bottomLimit);
            }
        }

        // Log.d("mobeta", "dragView top=" + (y - mDragDeltaY));
        // Log.d("mobeta", "limit=" + limit);
        // Log.d("mobeta", "mDragDeltaY=" + mDragDeltaY);

        if (floatY < topLimit) {
            mFloatLoc.y = topLimit;
        } else if (floatY + mFloatViewHeight > bottomLimit) {
            mFloatLoc.y = bottomLimit - mFloatViewHeight;
        }

        // get y-midpoint of floating view (constrained to ListView bounds)
        mFloatViewMid = mFloatLoc.y + mFloatViewHeightHalf;
    }

    private void destroyFloatView() {
        if (mFloatView != null) {
            mFloatView.setVisibility(GONE);
            if (mFloatViewManager != null) {
                mFloatViewManager.onDestroyFloatView(mFloatView);
            }
            mFloatView = null;
            invalidate();
        }
    }

    /**
     * Interface for customization of the floating View appearance
     * and dragging behavior. Implement
     * your own and pass it to {@link #setFloatViewManager}. If
     * your own is not passed, the default {@link SimpleFloatViewManager}
     * implementation is used.
     */
    public interface FloatViewManager {
        /**
         * Return the floating View for item at <code>position</code>.
         * DragSortListView will measure and layout this View for you,
         * so feel free to just inflate it. You can help DSLV by
         * setting some {@link ViewGroup.LayoutParams} on this View;
         * otherwise it will set some for you (with a width of FILL_PARENT
         * and a height of WRAP_CONTENT).
         *
         * @param position Position of item to drag (NOTE:
         * <code>position</code> excludes header Views; thus, if you
         * want to call {@link ListView#getChildAt(int)}, you will need
         * to add {@link ListView#getHeaderViewsCount()} to the index).
         *
         * @return The View you wish to display as the floating View.
         */
        public View onCreateFloatView(int position);

        /**
         * Called whenever the floating View is dragged. Float View
         * properties can be changed here. Also, the upcoming location
         * of the float View can be altered by setting
         * <code>location.x</code> and <code>location.y</code>.
         *
         * @param floatView The floating View.
         * @param location The location (top-left; relative to DSLV
         * top-left) at which the float
         * View would like to appear, given the current touch location
         * and the offset provided in {@link DragSortListView#startDrag}.
         * @param touch The current touch location (relative to DSLV
         * top-left).
         * @param pendingScroll 
         */
        public void onDragFloatView(View floatView, Point location, Point touch);

        /**
         * Called when the float View is dropped; lets you perform
         * any necessary cleanup. The internal DSLV floating View
         * reference is set to null immediately after this is called.
         *
         * @param floatView The floating View passed to
         * {@link #onCreateFloatView(int)}.
         */
        public void onDestroyFloatView(View floatView);
    }

    public void setFloatViewManager(FloatViewManager manager) {
        mFloatViewManager = manager;
    }

    public void setDragListener(DragListener l) {
        mDragListener = l;
    }

    /**
     * Allows for easy toggling between a DragSortListView
     * and a regular old ListView. If enabled, items are
     * draggable, where the drag init mode determines how
     * items are lifted (see {@link setDragInitMode(int)}).
     * If disabled, items cannot be dragged.
     *
     * @param enabled Set <code>true</code> to enable list
     * item dragging
     */
    public void setDragEnabled(boolean enabled) {
        mDragEnabled = enabled;
    }

    public boolean isDragEnabled() {
        return mDragEnabled;
    }

    /**
     * This better reorder your ListAdapter! DragSortListView does not do this
     * for you; doesn't make sense to. Make sure
     * {@link BaseAdapter#notifyDataSetChanged()} or something like it is called
     * in your implementation. Furthermore, if you have a choiceMode other than
     * none and the ListAdapter does not return true for
     * {@link ListAdapter#hasStableIds()}, you will need to call
     * {@link #moveCheckState(int, int)} to move the check boxes along with the
     * list items.
     * 
     * @param l
     */
    public void setDropListener(DropListener l) {
        mDropListener = l;
    }

    /**
     * Probably a no-brainer, but make sure that your remove listener
     * calls {@link BaseAdapter#notifyDataSetChanged()} or something like it.
     * When an item removal occurs, DragSortListView
     * relies on a redraw of all the items to recover invisible views
     * and such. Strictly speaking, if you remove something, your dataset
     * has changed...
     * 
     * @param l
     */
    public void setRemoveListener(RemoveListener l) {
        mRemoveListener = l;
    }

    public interface DragListener {
        public void drag(int from, int to);
    }

    /**
     * Your implementation of this has to reorder your ListAdapter! 
     * Make sure to call
     * {@link BaseAdapter#notifyDataSetChanged()} or something like it
     * in your implementation.
     * 
     * @author heycosmo
     *
     */
    public interface DropListener {
        public void drop(int from, int to);
    }

    /**
     * Make sure to call
     * {@link BaseAdapter#notifyDataSetChanged()} or something like it
     * in your implementation.
     * 
     * @author heycosmo
     *
     */
    public interface RemoveListener {
        public void remove(int which);
    }

    public interface DragSortListener extends DropListener, DragListener, RemoveListener {
    }

    public void setDragSortListener(DragSortListener l) {
        setDropListener(l);
        setDragListener(l);
        setRemoveListener(l);
    }

    /**
     * Completely custom scroll speed profile. Default increases linearly
     * with position and is constant in time. Create your own by implementing
     * {@link DragSortListView.DragScrollProfile}.
     * 
     * @param ssp
     */
    public void setDragScrollProfile(DragScrollProfile ssp) {
        if (ssp != null) {
            mScrollProfile = ssp;
        }
    }

    /**
     * Use this to move the check state of an item from one position to another
     * in a drop operation. If you have a choiceMode which is not none, this
     * method must be called when the order of items changes in an underlying
     * adapter which does not have stable IDs (see
     * {@link ListAdapter#hasStableIds()}). This is because without IDs, the
     * ListView has no way of knowing which items have moved where, and cannot
     * update the check state accordingly.
     * <p>
     * A word of warning about a "feature" in Android that you may run into when
     * dealing with movable list items: for an adapter that <em>does</em> have
     * stable IDs, ListView will attempt to locate each item based on its ID and
     * move the check state from the item's old position to the new position —
     * which is all fine and good (and removes the need for calling this
     * function), except for the half-baked approach. Apparently to save time in
     * the naive algorithm used, ListView will only search for an ID in the
     * close neighborhood of the old position. If the user moves an item too far
     * (specifically, more than 20 rows away), ListView will give up and just
     * force the item to be unchecked. So if there is a reasonable chance that
     * the user will move items more than 20 rows away from the original
     * position, you may wish to use an adapter with unstable IDs and call this
     * method manually instead.
     * 
     * @param from
     * @param to
     */
    public void moveCheckState(int from, int to) {
        // This method runs in O(n log n) time (n being the number of list
        // items). The bottleneck is the call to AbsListView.setItemChecked,
        // which is O(log n) because of the binary search involved in calling
        // SparseBooleanArray.put().
        //
        // To improve on the average time, we minimize the number of calls to
        // setItemChecked by only calling it for items that actually have a
        // changed state. This is achieved by building a list containing the
        // start and end of the "runs" of checked items, and then moving the
        // runs. Note that moving an item from A to B is essentially a rotation
        // of the range of items in [A, B]. Let's say we have
        // . . U V X Y Z . .
        // and move U after Z. This is equivalent to a rotation one step to the
        // left within the range you are moving across:
        // . . V X Y Z U . .
        //
        // So, to perform the move we enumerate all the runs within the move
        // range, then rotate each run one step to the left or right (depending
        // on move direction). For example, in the list:
        // X X . X X X . X
        // we have two runs. One begins at the last item of the list and wraps
        // around to the beginning, ending at position 1. The second begins at
        // position 3 and ends at position 5. To rotate a run, regardless of
        // length, we only need to set a check mark at one end of the run, and
        // clear a check mark at the other end:
        // X . X X X . X X
        SparseBooleanArray cip = getCheckedItemPositions();
        int rangeStart = from;
        int rangeEnd = to;
        if (to < from) {
            rangeStart = to;
            rangeEnd = from;
        }
        rangeEnd += 1;

        int[] runStart = new int[cip.size()];
        int[] runEnd = new int[cip.size()];
        int runCount = buildRunList(cip, rangeStart, rangeEnd, runStart, runEnd);
        if (runCount == 1 && (runStart[0] == runEnd[0])) {
            // Special case where all items are checked, we can never set any
            // item to false like we do below.
            return;
        }

        if (from < to) {
            for (int i = 0; i != runCount; i++) {
                setItemChecked(rotate(runStart[i], -1, rangeStart, rangeEnd), true);
                setItemChecked(rotate(runEnd[i], -1, rangeStart, rangeEnd), false);
            }

        } else {
            for (int i = 0; i != runCount; i++) {
                setItemChecked(runStart[i], false);
                setItemChecked(runEnd[i], true);
            }
        }
    }

    /**
     * Use this when an item has been deleted, to move the check state of all
     * following items up one step. If you have a choiceMode which is not none,
     * this method must be called when the order of items changes in an
     * underlying adapter which does not have stable IDs (see
     * {@link ListAdapter#hasStableIds()}). This is because without IDs, the
     * ListView has no way of knowing which items have moved where, and cannot
     * update the check state accordingly.
     * 
     * See also further comments on {@link #moveCheckState(int, int)}.
     * 
     * @param position
     */
    public void removeCheckState(int position) {
        SparseBooleanArray cip = getCheckedItemPositions();

        if (cip.size() == 0)
            return;
        int[] runStart = new int[cip.size()];
        int[] runEnd = new int[cip.size()];
        int rangeStart = position;
        int rangeEnd = cip.keyAt(cip.size() - 1) + 1;
        int runCount = buildRunList(cip, rangeStart, rangeEnd, runStart, runEnd);
        for (int i = 0; i != runCount; i++) {
            if (!(runStart[i] == position || (runEnd[i] < runStart[i] && runEnd[i] > position))) {
                // Only set a new check mark in front of this run if it does
                // not contain the deleted position. If it does, we only need
                // to make it one check mark shorter at the end.
                setItemChecked(rotate(runStart[i], -1, rangeStart, rangeEnd), true);
            }
            setItemChecked(rotate(runEnd[i], -1, rangeStart, rangeEnd), false);
        }
    }

    private static int buildRunList(SparseBooleanArray cip, int rangeStart,
            int rangeEnd, int[] runStart, int[] runEnd) {
        int runCount = 0;

        int i = findFirstSetIndex(cip, rangeStart, rangeEnd);
        if (i == -1)
            return 0;

        int position = cip.keyAt(i);
        int currentRunStart = position;
        int currentRunEnd = currentRunStart + 1;
        for (i++; i < cip.size() && (position = cip.keyAt(i)) < rangeEnd; i++) {
            if (!cip.valueAt(i)) // not checked => not interesting
                continue;
            if (position == currentRunEnd) {
                currentRunEnd++;
            } else {
                runStart[runCount] = currentRunStart;
                runEnd[runCount] = currentRunEnd;
                runCount++;
                currentRunStart = position;
                currentRunEnd = position + 1;
            }
        }

        if (currentRunEnd == rangeEnd) {
            // rangeStart and rangeEnd are equivalent positions so to be
            // consistent we translate them to the same integer value. That way
            // we can check whether a run covers the entire range by just
            // checking if the start equals the end position.
            currentRunEnd = rangeStart;
        }
        runStart[runCount] = currentRunStart;
        runEnd[runCount] = currentRunEnd;
        runCount++;

        if (runCount > 1) {
            if (runStart[0] == rangeStart && runEnd[runCount - 1] == rangeStart) {
                // The last run ends at the end of the range, and the first run
                // starts at the beginning of the range. So they are actually
                // part of the same run, except they wrap around the end of the
                // range. To avoid adjacent runs, we need to merge them.
                runStart[0] = runStart[runCount - 1];
                runCount--;
            }
        }
        return runCount;
    }

    private static int rotate(int value, int offset, int lowerBound, int upperBound) {
        int windowSize = upperBound - lowerBound;

        value += offset;
        if (value < lowerBound) {
            value += windowSize;
        } else if (value >= upperBound) {
            value -= windowSize;
        }
        return value;
    }

    private static int findFirstSetIndex(SparseBooleanArray sba, int rangeStart, int rangeEnd) {
        int size = sba.size();
        int i = insertionIndexForKey(sba, rangeStart);
        while (i < size && sba.keyAt(i) < rangeEnd && !sba.valueAt(i))
            i++;
        if (i == size || sba.keyAt(i) >= rangeEnd)
            return -1;
        return i;
    }

    private static int insertionIndexForKey(SparseBooleanArray sba, int key) {
        int low = 0;
        int high = sba.size();
        while (high - low > 0) {
            int middle = (low + high) >> 1;
            if (sba.keyAt(middle) < key)
                low = middle + 1;
            else
                high = middle;
        }
        return low;
    }

    /**
     * Interface for controlling
     * scroll speed as a function of touch position and time. Use
     * {@link DragSortListView#setDragScrollProfile(DragScrollProfile)} to
     * set custom profile.
     * 
     * @author heycosmo
     *
     */
    public interface DragScrollProfile {
        /**
         * Return a scroll speed in pixels/millisecond. Always return a
         * positive number.
         * 
         * @param w Normalized position in scroll region (i.e. w \in [0,1]).
         * Small w typically means slow scrolling.
         * @param t Time (in milliseconds) since start of scroll (handy if you
         * want scroll acceleration).
         * @return Scroll speed at position w and time t in pixels/ms.
         */
        float getSpeed(float w, long t);
    }

    private class DragScroller implements Runnable {

        private boolean mAbort;

        private long mPrevTime;
        private long mCurrTime;

        private int dy;
        private float dt;
        private long tStart;
        private int scrollDir;

        public final static int STOP = -1;
        public final static int UP = 0;
        public final static int DOWN = 1;

        private float mScrollSpeed; // pixels per ms

        private boolean mScrolling = false;

        private int mLastHeader;
        private int mFirstFooter;

        public boolean isScrolling() {
            return mScrolling;
        }

        public int getScrollDir() {
            return mScrolling ? scrollDir : STOP;
        }

        public DragScroller() {
        }

        public void startScrolling(int dir) {
            if (!mScrolling) {
                // Debug.startMethodTracing("dslv-scroll");
                mAbort = false;
                mScrolling = true;
                tStart = SystemClock.uptimeMillis();
                mPrevTime = tStart;
                scrollDir = dir;
                post(this);
            }
        }

        public void stopScrolling(boolean now) {
            if (now) {
                DragSortListView.this.removeCallbacks(this);
                mScrolling = false;
            } else {
                mAbort = true;
            }

            // Debug.stopMethodTracing();
        }

        @Override
        public void run() {
            if (mAbort) {
                mScrolling = false;
                return;
            }

            // Log.d("mobeta", "scroll");

            final int first = getFirstVisiblePosition();
            final int last = getLastVisiblePosition();
            final int count = getCount();
            final int padTop = getPaddingTop();
            final int listHeight = getHeight() - padTop - getPaddingBottom();

            int minY = Math.min(mY, mFloatViewMid + mFloatViewHeightHalf);
            int maxY = Math.max(mY, mFloatViewMid - mFloatViewHeightHalf);

            if (scrollDir == UP) {
                View v = getChildAt(0);
                // Log.d("mobeta", "vtop="+v.getTop()+" padtop="+padTop);
                if (v == null) {
                    mScrolling = false;
                    return;
                } else {
                    if (first == 0 && v.getTop() == padTop) {
                        mScrolling = false;
                        return;
                    }
                }
                mScrollSpeed = mScrollProfile.getSpeed((mUpScrollStartYF - maxY)
                        / mDragUpScrollHeight, mPrevTime);
            } else {
                View v = getChildAt(last - first);
                if (v == null) {
                    mScrolling = false;
                    return;
                } else {
                    if (last == count - 1 && v.getBottom() <= listHeight + padTop) {
                        mScrolling = false;
                        return;
                    }
                }
                mScrollSpeed = -mScrollProfile.getSpeed((minY - mDownScrollStartYF)
                        / mDragDownScrollHeight, mPrevTime);
            }

            mCurrTime = SystemClock.uptimeMillis();
            dt = (float) (mCurrTime - mPrevTime);

            // dy is change in View position of a list item; i.e. positive dy
            // means user is scrolling up (list item moves down the screen,
            // remember
            // y=0 is at top of View).
            dy = (int) Math.round(mScrollSpeed * dt);

            int movePos;
            if (dy >= 0) {
                dy = Math.min(listHeight, dy);
                movePos = first;
            } else {
                dy = Math.max(-listHeight, dy);
                movePos = last;
            }

            final View moveItem = getChildAt(movePos - first);
            int top = moveItem.getTop() + dy;

            if (movePos == 0 && top > padTop) {
                top = padTop;
            }

            // always do scroll
            mBlockLayoutRequests = true;

            setSelectionFromTop(movePos, top - padTop);
            DragSortListView.this.layoutChildren();
            invalidate();

            mBlockLayoutRequests = false;

            // scroll means relative float View movement
            doDragFloatView(movePos, moveItem, false);

            mPrevTime = mCurrTime;
            // Log.d("mobeta", "  updated prevTime="+mPrevTime);

            post(this);
        }
    }

    private class DragSortTracker {
        StringBuilder mBuilder = new StringBuilder();

        File mFile;

        private int mNumInBuffer = 0;
        private int mNumFlushes = 0;

        private boolean mTracking = false;

        public DragSortTracker() {
            File root = Environment.getExternalStorageDirectory();
            mFile = new File(root, "dslv_state.txt");

            if (!mFile.exists()) {
                try {
                    mFile.createNewFile();
                    Log.d("mobeta", "file created");
                } catch (IOException e) {
                    Log.w("mobeta", "Could not create dslv_state.txt");
                    Log.d("mobeta", e.getMessage());
                }
            }

        }

        public void startTracking() {
            mBuilder.append("<DSLVStates>\n");
            mNumFlushes = 0;
            mTracking = true;
        }

        public void appendState() {
            if (!mTracking) {
                return;
            }

            mBuilder.append("<DSLVState>\n");
            final int children = getChildCount();
            final int first = getFirstVisiblePosition();
            mBuilder.append("    <Positions>");
            for (int i = 0; i < children; ++i) {
                mBuilder.append(first + i).append(",");
            }
            mBuilder.append("</Positions>\n");

            mBuilder.append("    <Tops>");
            for (int i = 0; i < children; ++i) {
                mBuilder.append(getChildAt(i).getTop()).append(",");
            }
            mBuilder.append("</Tops>\n");
            mBuilder.append("    <Bottoms>");
            for (int i = 0; i < children; ++i) {
                mBuilder.append(getChildAt(i).getBottom()).append(",");
            }
            mBuilder.append("</Bottoms>\n");

            mBuilder.append("    <FirstExpPos>").append(mFirstExpPos).append("</FirstExpPos>\n");
            mBuilder.append("    <FirstExpBlankHeight>")
                    .append(getItemHeight(mFirstExpPos) - getChildHeight(mFirstExpPos))
                    .append("</FirstExpBlankHeight>\n");
            mBuilder.append("    <SecondExpPos>").append(mSecondExpPos).append("</SecondExpPos>\n");
            mBuilder.append("    <SecondExpBlankHeight>")
                    .append(getItemHeight(mSecondExpPos) - getChildHeight(mSecondExpPos))
                    .append("</SecondExpBlankHeight>\n");
            mBuilder.append("    <SrcPos>").append(mSrcPos).append("</SrcPos>\n");
            mBuilder.append("    <SrcHeight>").append(mFloatViewHeight + getDividerHeight())
                    .append("</SrcHeight>\n");
            mBuilder.append("    <ViewHeight>").append(getHeight()).append("</ViewHeight>\n");
            mBuilder.append("    <LastY>").append(mLastY).append("</LastY>\n");
            mBuilder.append("    <FloatY>").append(mFloatViewMid).append("</FloatY>\n");
            mBuilder.append("    <ShuffleEdges>");
            for (int i = 0; i < children; ++i) {
                mBuilder.append(getShuffleEdge(first + i, getChildAt(i).getTop())).append(",");
            }
            mBuilder.append("</ShuffleEdges>\n");

            mBuilder.append("</DSLVState>\n");
            mNumInBuffer++;

            if (mNumInBuffer > 1000) {
                flush();
                mNumInBuffer = 0;
            }
        }

        public void flush() {
            if (!mTracking) {
                return;
            }

            // save to file on sdcard
            try {
                boolean append = true;
                if (mNumFlushes == 0) {
                    append = false;
                }
                FileWriter writer = new FileWriter(mFile, append);

                writer.write(mBuilder.toString());
                mBuilder.delete(0, mBuilder.length());

                writer.flush();
                writer.close();

                mNumFlushes++;
            } catch (IOException e) {
                // do nothing
            }
        }

        public void stopTracking() {
            if (mTracking) {
                mBuilder.append("</DSLVStates>\n");
                flush();
                mTracking = false;
            }
        }

    }

}
