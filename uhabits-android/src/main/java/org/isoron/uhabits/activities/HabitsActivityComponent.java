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

import org.isoron.androidbase.activities.*;
import org.isoron.uhabits.*;
import org.isoron.uhabits.activities.common.dialogs.*;
import org.isoron.uhabits.activities.habits.list.*;
import org.isoron.uhabits.activities.habits.list.controllers.*;
import org.isoron.uhabits.activities.habits.list.model.*;
import org.isoron.uhabits.activities.habits.show.*;
import org.isoron.uhabits.core.ui.*;

import dagger.*;

@ActivityScope
@Component(modules = {
    ActivityModule.class,
    HabitsActivityModule.class,
    ListHabitsModule.class,
    ShowHabitModule.class,
    HabitModule.class
}, dependencies = { HabitsApplicationComponent.class })
public interface HabitsActivityComponent
{
    BaseActivity getActivity();

    CheckmarkButtonControllerFactory getCheckmarkButtonControllerFactory();

    ColorPickerDialogFactory getColorPickerDialogFactory();

    HabitCardListAdapter getHabitCardListAdapter();

    HabitCardListController getHabitCardListController();

    ListHabitsController getListHabitsController();

    ListHabitsMenu getListHabitsMenu();

    ListHabitsRootView getListHabitsRootView();

    ListHabitsScreen getListHabitsScreen();

    ListHabitsSelectionMenu getListHabitsSelectionMenu();

    NumberButtonControllerFactory getNumberButtonControllerFactory();

    ShowHabitController getShowHabitController();

    ShowHabitsMenu getShowHabitMenu();

    ShowHabitRootView getShowHabitRootView();

    ShowHabitScreen getShowHabitScreen();

    ThemeSwitcher getThemeSwitcher();
}
