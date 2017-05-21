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

import android.os.*;
import android.support.v7.widget.Toolbar;
import android.view.*;
import android.widget.*;

import org.isoron.uhabits.*;
import org.junit.*;
import org.junit.runner.*;
import org.robolectric.*;
import org.robolectric.annotation.*;

import static org.mockito.Mockito.*;
import static org.robolectric.Robolectric.*;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class)
public class BaseActivityTest
{
    @Test
    public void menuTest()
    {
        MenuActivity activity = setupActivity(MenuActivity.class);
        verify(activity.baseMenu).onCreate(
            eq(activity.getMenuInflater()), any());

        Menu menu = activity.toolbar.getMenu();
        MenuItem item = menu.getItem(0);
        activity.onMenuItemSelected(0, item);
        verify(activity.baseMenu).onItemSelected(item);
    }

    public static class MenuActivity extends BaseActivity
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
}
