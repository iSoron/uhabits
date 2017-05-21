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
import android.support.annotation.*;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.*;
import android.view.*;

import org.isoron.uhabits.*;
import org.junit.*;
import org.junit.runner.*;
import org.robolectric.*;
import org.robolectric.annotation.*;

import java.util.*;

import static android.view.View.*;
import static junit.framework.Assert.assertNotNull;
import static org.hamcrest.core.IsEqual.*;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.robolectric.Robolectric.*;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class)
public class BaseScreenTest
{
    @Test
    public void selectionMenuTest()
    {
        BaseSelectionMenu selectionMenu = spy(new BaseSelectionMenu()
        {
            @Override
            protected int getResourceId()
            {
                return R.menu.list_habits_selection;
            }
        });

        ActionModeActivity activity = setupActivity(ActionModeActivity.class);
        BaseScreen screen = new BaseScreen(activity);
        screen.setSelectionMenu(selectionMenu);
        activity.setScreen(screen);

        screen.startSelection();
        assertNotNull(activity.callback);
        verify(selectionMenu).onCreate(any(), any(), any());
        verify(selectionMenu).onPrepare(any());

        ActionMode mode = mock(ActionMode.class);
        MenuItem item = mock(MenuItem.class);

        activity.callback.onActionItemClicked(mode, item);
        verify(selectionMenu).onItemClicked(item);

        activity.callback.onDestroyActionMode(mode);
        verify(selectionMenu).onFinish();
    }

    @Test
    public void showMessageTest()
    {
        EmptyActivity activity = setupActivity(EmptyActivity.class);
        ConcreteRootView rootView = new ConcreteRootView(activity);
        View decor = activity.getWindow().getDecorView();
        BaseScreen screen = new BaseScreen(activity);
        screen.setRootView(rootView);
        activity.setScreen(screen);

        ArrayList<View> matches = new ArrayList<>();

        screen.showMessage(R.string.checkmark);
        decor.findViewsWithText(matches, "Checkmark", FIND_VIEWS_WITH_TEXT);
        assertThat(matches.size(), equalTo(1));
        assertTrue(matches.get(0).isShown());

        screen.showMessage(R.string.frequency);
        decor.findViewsWithText(matches, "Frequency", FIND_VIEWS_WITH_TEXT);
        assertThat(matches.size(), equalTo(1));
        assertTrue(matches.get(0).isShown());
    }

    static class ActionModeActivity extends BaseActivity
    {
        private ActionMode.Callback callback;

        @Nullable
        @Override
        public ActionMode startSupportActionMode(
            @NonNull ActionMode.Callback callback)
        {
            this.callback = callback;
            return super.startSupportActionMode(this.callback);
        }
    }

    static class ConcreteRootView extends BaseRootView
    {
        private final Toolbar toolbar;

        public ConcreteRootView(@NonNull Context context)
        {
            super(context);
            toolbar = new Toolbar(context);
            addView(toolbar);
        }

        @NonNull
        @Override
        public Toolbar getToolbar()
        {
            return toolbar;
        }
    }

    static class EmptyActivity extends BaseActivity
    {

    }
}
