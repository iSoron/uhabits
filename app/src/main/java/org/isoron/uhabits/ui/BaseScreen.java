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

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatDialogFragment;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import org.isoron.uhabits.tasks.ProgressBar;
import org.isoron.uhabits.utils.ColorUtils;

import java.io.File;

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

    public void finishSelection()
    {
        if (selectionMenu == null) return;
        selectionMenu.finish();
    }

    @Nullable
    public ProgressBar getProgressBar()
    {
        if (rootView == null) return null;
        return new ProgressBarWrapper(rootView.getProgressBar());
    }

    public void invalidate()
    {
        if (rootView == null) return;
        rootView.invalidate();
    }

    public void onResult(int requestCode, int resultCode, Intent data)
    {
    }

    public void setMenu(@Nullable BaseMenu menu)
    {
        activity.setBaseMenu(menu);
    }

    public void setRootView(@Nullable BaseRootView rootView)
    {
        this.rootView = rootView;
        activity.setContentView(rootView);
        if (rootView == null) return;

        initToolbar();
    }

    /**
     * Set the menu to be shown when a selection is active on the screen.
     *
     * @param menu the menu to be shown during a selection
     */
    public void setSelectionMenu(@Nullable BaseSelectionMenu menu)
    {
        this.selectionMenu = menu;
    }

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
     * Instructs the screen to start a selection. If a selection menu was
     * provided, this menu will be shown instead of the regular one.
     */
    public void startSelection()
    {
        activity.startSupportActionMode(new ActionModeWrapper());
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
    }

    private void setActionBarColor(@NonNull ActionBar actionBar, int color)
    {
        ColorDrawable drawable = new ColorDrawable(color);
        actionBar.setBackgroundDrawable(drawable);
    }

    private void setStatusBarColor(int baseColor)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            int darkerColor =
                ColorUtils.mixColors(baseColor, Color.BLACK, 0.75f);
            activity.getWindow().setStatusBarColor(darkerColor);
        }
    }

    protected void showDialog(AppCompatDialogFragment dialog, String tag)
    {
        dialog.show(activity.getSupportFragmentManager(), tag);
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
            selectionMenu.onDestroy();
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
