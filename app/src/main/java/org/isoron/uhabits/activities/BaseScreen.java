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

package org.isoron.uhabits.activities;

import android.content.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.net.*;
import android.os.*;
import android.support.annotation.*;
import android.support.design.widget.*;
import android.support.v4.content.res.*;
import android.support.v7.app.*;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.Toolbar;
import android.view.*;
import android.widget.*;

import org.isoron.uhabits.*;
import org.isoron.uhabits.utils.*;

import java.io.*;

import static android.os.Build.VERSION.*;
import static android.os.Build.VERSION_CODES.*;
import static android.support.v4.content.FileProvider.*;

/**
 * Base class for all screens in the application.
 * <p>
 * Screens are responsible for deciding what root views and what menus should be
 * attached to the main window. They are also responsible for showing other
 * screens and for receiving their results.
 */
public class BaseScreen
{
    public static final int REQUEST_CREATE_DOCUMENT = 1;

    protected BaseActivity activity;

    @Nullable
    private BaseRootView rootView;

    @Nullable
    private BaseSelectionMenu selectionMenu;

    private Snackbar snackbar;

    public BaseScreen(@NonNull BaseActivity activity)
    {
        this.activity = activity;
    }

    @Deprecated
    public static void setupActionBarColor(@NonNull AppCompatActivity activity,
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

        if (SDK_INT >= LOLLIPOP)
        {
            int darkerColor = ColorUtils.mixColors(color, Color.BLACK, 0.75f);
            activity.getWindow().setStatusBarColor(darkerColor);

            toolbar.setElevation(InterfaceUtils.dpToPixels(activity, 2));

            View view = activity.findViewById(R.id.toolbarShadow);
            if (view != null) view.setVisibility(View.GONE);

            view = activity.findViewById(R.id.headerShadow);
            if (view != null) view.setVisibility(View.GONE);
        }
    }

    @Deprecated
    public static int getDefaultActionBarColor(Context context)
    {
        if (SDK_INT < LOLLIPOP)
        {
            return ResourcesCompat.getColor(context.getResources(),
                R.color.grey_900, context.getTheme());
        }
        else
        {
            StyledResources res = new StyledResources(context);
            return res.getColor(R.attr.colorPrimary);
        }
    }

    /**
     * Notifies the screen that its contents should be updated.
     */
    public void invalidate()
    {
        if (rootView == null) return;
        rootView.invalidate();
    }

    public void invalidateToolbar()
    {
        if (rootView == null) return;

        activity.runOnUiThread(() -> {
            Toolbar toolbar = rootView.getToolbar();
            activity.setSupportActionBar(toolbar);
            ActionBar actionBar = activity.getSupportActionBar();
            if (actionBar == null) return;

            actionBar.setDisplayHomeAsUpEnabled(rootView.getDisplayHomeAsUp());

            int color = rootView.getToolbarColor();
            setActionBarColor(actionBar, color);
            setStatusBarColor(color);
        });
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

        invalidateToolbar();
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
    public void showMessage(@StringRes Integer stringId)
    {
        if (stringId == null || rootView == null) return;
        if (snackbar == null)
        {
            snackbar = Snackbar.make(rootView, stringId, Snackbar.LENGTH_SHORT);
            int tvId = android.support.design.R.id.snackbar_text;
            TextView tv = (TextView) snackbar.getView().findViewById(tvId);
            tv.setTextColor(Color.WHITE);
        }
        else snackbar.setText(stringId);
        snackbar.show();
    }

    public void showSendEmailScreen(@StringRes int toId,
                                    @StringRes int subjectId,
                                    String content)
    {
        String to = activity.getString(toId);
        String subject = activity.getString(subjectId);

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("message/rfc822");
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{ to });
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, content);
        activity.startActivity(intent);
    }

    public void showSendFileScreen(@NonNull String archiveFilename)
    {
        File file = new File(archiveFilename);
        Uri fileUri = getUriForFile(activity, "org.isoron.uhabits", file);

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("application/zip");
        intent.putExtra(Intent.EXTRA_STREAM, fileUri);
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
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

    private void setActionBarColor(@NonNull ActionBar actionBar, int color)
    {
        ColorDrawable drawable = new ColorDrawable(color);
        actionBar.setBackgroundDrawable(drawable);
    }

    private void setStatusBarColor(int baseColor)
    {
        if (SDK_INT < LOLLIPOP) return;

        int darkerColor = ColorUtils.mixColors(baseColor, Color.BLACK, 0.75f);
        activity.getWindow().setStatusBarColor(darkerColor);
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
