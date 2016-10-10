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
import android.os.*;
import android.support.annotation.*;
import android.support.v7.app.*;
import android.view.*;

import org.isoron.uhabits.*;
import org.isoron.uhabits.activities.habits.list.*;
import org.isoron.uhabits.models.*;
import org.isoron.uhabits.models.sqlite.*;

import static android.R.anim.*;

/**
 * Base class for all activities in the application.
 * <p>
 * This class delegates the responsibilities of an Android activity to other
 * classes. For example, callbacks related to menus are forwarded to a {@link
 * BaseMenu}, while callbacks related to activity results are forwarded to a
 * {@link BaseScreen}.
 * <p>
 * A BaseActivity also installs an {@link java.lang.Thread.UncaughtExceptionHandler}
 * to the main thread that logs the exception to the disk before the application
 * crashes.
 */
abstract public class BaseActivity extends AppCompatActivity
    implements Thread.UncaughtExceptionHandler
{
    @Nullable
    private BaseMenu baseMenu;

    @Nullable
    private Thread.UncaughtExceptionHandler androidExceptionHandler;

    @Nullable
    private BaseScreen screen;

    private ActivityComponent component;

    public ActivityComponent getComponent()
    {
        return component;
    }

    @Override
    public boolean onCreateOptionsMenu(@Nullable Menu menu)
    {
        if (menu == null) return true;
        if (baseMenu == null) return true;
        baseMenu.onCreate(getMenuInflater(), menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@Nullable MenuItem item)
    {
        if (item == null) return false;
        if (baseMenu == null) return false;
        return baseMenu.onItemSelected(item);
    }

    public void restartWithFade()
    {
        new Handler().postDelayed(() -> {
            Intent intent = new Intent(this, ListHabitsActivity.class);
            finish();
            overridePendingTransition(fade_in, fade_out);
            startActivity(intent);

        }, 500); // HACK: Let the menu disappear first
    }

    public void setBaseMenu(@Nullable BaseMenu baseMenu)
    {
        this.baseMenu = baseMenu;
    }

    public void setScreen(@Nullable BaseScreen screen)
    {
        this.screen = screen;
    }

    public void showDialog(AppCompatDialogFragment dialog, String tag)
    {
        dialog.show(getSupportFragmentManager(), tag);
    }

    public void showDialog(AppCompatDialog dialog)
    {
        dialog.show();
    }

    @Override
    public void uncaughtException(@Nullable Thread thread,
                                  @Nullable Throwable ex)
    {
        if (ex == null) return;

        try
        {
            ex.printStackTrace();
            new BaseSystem(this).dumpBugReportToFile();
        }
        catch (Exception e)
        {
            // ignored
        }

        if (ex.getCause() instanceof InconsistentDatabaseException)
        {
            HabitsApplication app = (HabitsApplication) getApplication();
            HabitList habits = app.getComponent().getHabitList();
            habits.repair();
            System.exit(0);
        }

        if (androidExceptionHandler != null)
            androidExceptionHandler.uncaughtException(thread, ex);
        else System.exit(1);
    }

    @Override
    protected void onActivityResult(int request, int result, Intent data)
    {
        if (screen == null) super.onActivityResult(request, result, data);
        else screen.onResult(request, result, data);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        androidExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);

        HabitsApplication app = (HabitsApplication) getApplicationContext();

        component = DaggerActivityComponent
            .builder()
            .activityModule(new ActivityModule(this))
            .appComponent(app.getComponent())
            .build();

        component.getThemeSwitcher().apply();
    }
}
