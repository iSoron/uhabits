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

package org.isoron.uhabits;

import org.isoron.uhabits.tasks.ToggleRepetitionTask;
import org.isoron.uhabits.ui.habits.edit.BaseDialogFragment;
import org.isoron.uhabits.ui.habits.list.ListHabitsSelectionMenu;
import org.isoron.uhabits.ui.habits.list.model.HabitCardListAdapter;
import org.isoron.uhabits.ui.habits.list.model.HabitCardListCache;
import org.isoron.uhabits.ui.habits.list.ListHabitsController;
import org.isoron.uhabits.ui.habits.list.controllers.CheckmarkButtonController;
import org.isoron.uhabits.ui.habits.list.model.HintList;
import org.isoron.uhabits.ui.habits.list.views.CheckmarkPanelView;

public interface BaseComponent
{
    void inject(CheckmarkButtonController checkmarkButtonController);

    void inject(ListHabitsController listHabitsController);

    void inject(CheckmarkPanelView checkmarkPanelView);

    void inject(ToggleRepetitionTask toggleRepetitionTask);

    void inject(BaseDialogFragment baseDialogFragment);

    void inject(HabitCardListCache habitCardListCache);

    void inject(HabitBroadcastReceiver habitBroadcastReceiver);

    void inject(ListHabitsSelectionMenu listHabitsSelectionMenu);

    void inject(HintList hintList);

    void inject(HabitCardListAdapter habitCardListAdapter);
}
