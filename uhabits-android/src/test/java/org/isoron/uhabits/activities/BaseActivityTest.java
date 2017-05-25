/*
 * Copyright (C) 2017 Álinson Santos Xavier <isoron@gmail.com>
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
import android.support.v4.app.*;
import android.support.v7.app.*;
import android.support.v7.widget.Toolbar;
import android.view.*;
import android.widget.*;

import org.isoron.uhabits.*;
import org.isoron.uhabits.activities.common.dialogs.*;
import org.junit.*;
import org.junit.runner.*;
import org.robolectric.*;
import org.robolectric.annotation.*;
import org.robolectric.shadows.*;

import static org.hamcrest.core.IsEqual.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.robolectric.Robolectric.*;
import static org.robolectric.Shadows.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = BuildConfig.roboSdk, constants = BuildConfig.class)
public class BaseActivityTest
{
    private static boolean hasCrashed = false;

    @Test
    public void activityResultTest()
    {
        ScreenActivity activity = spy(setupActivity(ScreenActivity.class));
        activity.onActivityResult(0, 0, null);
        verify(activity.screen).onResult(0, 0, null);
    }

    @Test
    public void componentTest()
    {
        EmptyActivity activity = setupActivity(EmptyActivity.class);
        ActivityComponent component = activity.getComponent();
        assertThat(component.getActivity(), equalTo(activity));
    }

    @Test
    public void dialogFragmentTest()
    {
        EmptyActivity activity = setupActivity(EmptyActivity.class);
        FragmentManager manager = activity.getSupportFragmentManager();
        ColorPickerDialog d = new ColorPickerDialogFactory(activity).create(0);

        activity.showDialog(d, "picker");
        assertTrue(d.getDialog().isShowing());
        assertThat(d, equalTo(manager.findFragmentByTag("picker")));
    }

    @Test
    public void dialogTest()
    {
        EmptyActivity activity = setupActivity(EmptyActivity.class);
        AlertDialog dialog =
            new AlertDialog.Builder(activity).setTitle("Hello world").create();

        activity.showDialog(dialog);
        assertTrue(dialog.isShowing());
    }

    @Test
    public void restartTest() throws Exception
    {
        EmptyActivity activity = setupActivity(EmptyActivity.class);

        activity.restartWithFade(EmptyActivity.class);
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks();

        Intent nextStartedActivity = shadowOf(activity).getNextStartedActivity();
        assertNotNull(nextStartedActivity);

        assertTrue(activity.isFinishing());
        assertThat(shadowOf(nextStartedActivity).getIntentClass(),
            equalTo(EmptyActivity.class));
    }

    @Test
    public void exceptionHandlerTest() throws InterruptedException
    {
        assertFalse(hasCrashed);

        Thread crashThread = new Thread()
        {
            @Override
            public void run()
            {
                Looper.prepare();
                CrashActivity activity = setupActivity(CrashActivity.class);
                activity.crash();
            }
        };

        crashThread.start();
        crashThread.join();
        assertTrue(hasCrashed);
    }

    @Test
    public void menuTest()
    {
        MenuActivity activity = setupActivity(MenuActivity.class);
        verify(activity.baseMenu).onCreate(eq(activity.getMenuInflater()),
            any());

        Menu menu = activity.toolbar.getMenu();
        MenuItem item = menu.getItem(0);
        activity.onMenuItemSelected(0, item);
        verify(activity.baseMenu).onItemSelected(item);
    }

    static class CrashActivity extends BaseActivity
    {
        public void crash()
        {
            throw new RuntimeException("crash!");
        }

        @Override
        protected Thread.UncaughtExceptionHandler getExceptionHandler()
        {
            return (t, e) -> hasCrashed = true;
        }
    }

    static class EmptyActivity extends BaseActivity
    {

    }

    static class MenuActivity extends BaseActivity
    {
        public BaseMenu baseMenu;

        public Toolbar toolbar;

        @Override
        protected void onCreate(Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            toolbar = new Toolbar(this);
            LinearLayout layout = new LinearLayout(this);
            layout.addView(toolbar);
            setContentView(layout);
            setSupportActionBar(toolbar);

            baseMenu = spy(new BaseMenu(this)
            {
                @Override
                protected int getMenuResourceId()
                {
                    return R.menu.list_habits;
                }
            });

            setBaseMenu(baseMenu);
        }
    }

    static class ScreenActivity extends BaseActivity
    {
        private BaseScreen screen;

        @Override
        protected void onCreate(Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            screen = spy(new BaseScreen(this));
            setScreen(screen);
        }
    }
}
