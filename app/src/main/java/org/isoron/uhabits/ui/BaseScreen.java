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

package org.isoron.uhabits.ui;

import android.content.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.net.*;
import android.os.*;
import android.support.annotation.*;
import android.support.v7.app.*;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.Toolbar;
import android.view.*;
import android.widget.*;

import org.isoron.uhabits.*;
import org.isoron.uhabits.tasks.ProgressBar;
import org.isoron.uhabits.utils.*;

import java.io.*;

/**
 * Base class for all screens in the application.
 * <p>
 * Screens are responsible for deciding what root views and what menus should be
 * attached to the main window. They are also responsible for showing other
 * screens and for receiving their results.
 */
public abstract class BaseScreen
{
    protected BaseActivity activity;

    private Toast toast;

    @Nullable
    private BaseRootView rootView;

    @Nullable
    private BaseSelectionMenu selectionMenu;

    public BaseScreen(BaseActivity activity)
    {
        this.activity = activity;
    }

    @Deprecated
    public static void setupActionBarColor(AppCompatActivity activity,
                                           int color)
    {

        Toolbar toolbar = (Toolbar) activity.findViewById(R.id.toolbar);
        if (toolbar == null) return;

        activity.setSupportActionBar(toolbar);

        ActionBar actionBar = activity.getSupportActionBar();
        if (actionBar == null) return;

        actionBar.setDisplayHomeAsUpEnabled(true);

        ColorDrawable drawable = new ColorDrawable(color);
        actionBar.setBackgroundDrawable(drawable);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            int darkerColor = ColorUtils.mixColors(color, Color.BLACK, 0.75f);
            activity.getWindow().setStatusBarColor(darkerColor);

            toolbar.setElevation(InterfaceUtils.dpToPixels(activity, 2));

            View view = activity.findViewById(R.id.toolbarShadow);
            if (view != null) view.setVisibility(View.GONE);

//            view = activity.findViewById(R.id.headerShadow);
//            if (view != null) view.setVisibility(View.GONE);
        }
    }

    /**
     * Ends the current selection operation.
     */
    public void finishSelection()
    {
        if (selectionMenu == null) return;
        selectionMenu.finish();
    }

    /**
     * Returns the progress bar that is currently visible on the screen.
     * <p>
     * If the root view attached to the screen does not provide any progress
     * bars, returns null.
     *
     * @return current progress bar, or null if there are none.
     */
    @Nullable
    public ProgressBar getProgressBar()
    {
        if (rootView == null) return null;
        return new AndroidProgressBar(rootView.getProgressBar());
    }

    /**
     * Notifies the screen that its contents should be updated.
     */
    public void invalidate()
    {
        if (rootView == null) return;
        rootView.invalidate();
    }

    /**
     * Called when another Activity has finished, and has returned some result.
     *
     * @param requestCode the request code originally supplied to {@link
     *                    android.app.Activity#startActivityForResult(Intent,
     *                    int, Bundle)}.
     * @param resultCode  the result code sent by the other activity.
     * @param data        an Intent containing extra data sent by the other
     *                    activity.
     * @see {@link android.app.Activity#onActivityResult(int, int, Intent)}
     */
    public void onResult(int requestCode, int resultCode, Intent data)
    {
    }

    /**
     * Sets the menu to be shown by this screen.
     * <p>
     * This menu will be visible if when there is no active selection operation.
     * If the provided menu is null, then no menu will be shown.
     *
     * @param menu the menu to be shown.
     */
    public void setMenu(@Nullable BaseMenu menu)
    {
        activity.setBaseMenu(menu);
    }

    /**
     * Sets the root view for this screen.
     *
     * @param rootView the root view for this screen.
     */
    public void setRootView(@Nullable BaseRootView rootView)
    {
        this.rootView = rootView;
        activity.setContentView(rootView);
        if (rootView == null) return;

        initToolbar();
    }

    /**
     * Sets the menu to be shown when a selection is active on the screen.
     *
     * @param menu the menu to be shown during a selection
     */
    public void setSelectionMenu(@Nullable BaseSelectionMenu menu)
    {
        this.selectionMenu = menu;
    }

    /**
     * Shows a message on the screen.
     *
     * @param stringId the string resource id for this message.
     */
    public void showMessage(@Nullable Integer stringId)
    {
        if (stringId == null) return;
        if (toast == null)
            toast = Toast.makeText(activity, stringId, Toast.LENGTH_SHORT);
        else toast.setText(stringId);
        toast.show();
    }

    public void showSendEmailScreen(String to, String subject, String content)
    {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("message/rfc822");
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{to});
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, content);
        activity.startActivity(intent);
    }

    public void showSendFileScreen(@NonNull String archiveFilename)
    {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("application/zip");
        intent.putExtra(Intent.EXTRA_STREAM,
            Uri.fromFile(new File(archiveFilename)));
        activity.startActivity(intent);
    }

    /**
     * Instructs the screen to start a selection.
     * <p>
     * If a selection menu was provided, this menu will be shown instead of the
     * regular one.
     */
    public void startSelection()
    {
        activity.startSupportActionMode(new ActionModeWrapper());
    }

    protected void showDialog(AppCompatDialogFragment dialog, String tag)
    {
        dialog.show(activity.getSupportFragmentManager(), tag);
    }

    private void initToolbar()
    {
        if (rootView == null) return;

        Toolbar toolbar = rootView.getToolbar();
        activity.setSupportActionBar(toolbar);
        ActionBar actionBar = activity.getSupportActionBar();
        if (actionBar == null) return;

        actionBar.setDisplayHomeAsUpEnabled(rootView.getDisplayHomeAsUp());

        int color = rootView.getToolbarColor();
        setActionBarColor(actionBar, color);
        setStatusBarColor(color);
        setupToolbarElevation(toolbar);
    }

    private void setActionBarColor(@NonNull ActionBar actionBar, int color)
    {
        ColorDrawable drawable = new ColorDrawable(color);
        actionBar.setBackgroundDrawable(drawable);
    }

    private void setStatusBarColor(int baseColor)
    {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return;

        int darkerColor = ColorUtils.mixColors(baseColor, Color.BLACK, 0.75f);
        activity.getWindow().setStatusBarColor(darkerColor);
    }

    private void setupToolbarElevation(Toolbar toolbar)
    {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return;

        toolbar.setElevation(InterfaceUtils.dpToPixels(activity, 2));

        View view = activity.findViewById(R.id.toolbarShadow);
        if (view != null) view.setVisibility(View.GONE);

//        view = activity.findViewById(R.id.headerShadow);
//        if (view != null) view.setVisibility(View.GONE);
    }

    private class ActionModeWrapper implements ActionMode.Callback
    {
        @Override
        public boolean onActionItemClicked(@Nullable ActionMode mode,
                                           @Nullable MenuItem item)
        {
            if (item == null || selectionMenu == null) return false;
            return selectionMenu.onItemClicked(item);
        }

        @Override
        public boolean onCreateActionMode(@Nullable ActionMode mode,
                                          @Nullable Menu menu)
        {
            if (selectionMenu == null) return false;
            if (mode == null || menu == null) return false;
            selectionMenu.onCreate(activity.getMenuInflater(), mode, menu);
            return true;
        }

        @Override
        public void onDestroyActionMode(@Nullable ActionMode mode)
        {
            if (selectionMenu == null) return;
            selectionMenu.onFinish();
        }

        @Override
        public boolean onPrepareActionMode(@Nullable ActionMode mode,
                                           @Nullable Menu menu)
        {
            if (selectionMenu == null || menu == null) return false;
            return selectionMenu.onPrepare(menu);
        }
    }
}
