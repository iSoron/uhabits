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

package org.isoron.uhabits.automation;

import android.os.*;

import org.isoron.uhabits.*;
import org.isoron.uhabits.activities.*;
import org.isoron.uhabits.models.*;

public class EditSettingActivity extends BaseActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        HabitsApplication app = (HabitsApplication) getApplicationContext();

        HabitList habits = app.getComponent().getHabitList();
        habits = habits.getFiltered(new HabitMatcherBuilder()
            .setArchivedAllowed(false)
            .setCompletedAllowed(true)
            .build());

        EditSettingController controller = new EditSettingController(this);
        EditSettingRootView rootView =
            new EditSettingRootView(this, habits, controller);

        BaseScreen screen = new BaseScreen(this);
        screen.setRootView(rootView);
        setScreen(screen);
    }
}
