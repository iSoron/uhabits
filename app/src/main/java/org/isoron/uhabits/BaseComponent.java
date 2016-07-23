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

import org.isoron.uhabits.commands.*;
import org.isoron.uhabits.intents.*;
import org.isoron.uhabits.io.*;
import org.isoron.uhabits.models.*;
import org.isoron.uhabits.receivers.*;
import org.isoron.uhabits.tasks.*;
import org.isoron.uhabits.ui.*;
import org.isoron.uhabits.ui.common.dialogs.*;
import org.isoron.uhabits.ui.habits.edit.*;
import org.isoron.uhabits.ui.habits.list.*;
import org.isoron.uhabits.ui.habits.list.controllers.*;
import org.isoron.uhabits.ui.habits.list.model.*;
import org.isoron.uhabits.ui.habits.list.views.*;
import org.isoron.uhabits.ui.habits.show.*;
import org.isoron.uhabits.ui.widgets.*;
import org.isoron.uhabits.utils.*;
import org.isoron.uhabits.widgets.*;

/**
 * Base component for dependency injection.
 */
public interface BaseComponent
{
    CommandRunner getCommandRunner();

    HabitList getHabitList();

    IntentFactory getIntentFactory();

    ReminderScheduler getReminderScheduler();

    TaskRunner getTaskRunner();

    void inject(CheckmarkButtonController checkmarkButtonController);

    void inject(ListHabitsController listHabitsController);

    void inject(CheckmarkPanelView checkmarkPanelView);

    void inject(WidgetReceiver widgetReceiver);

    void inject(ListHabitsSelectionMenu listHabitsSelectionMenu);

    void inject(HintList hintList);

    void inject(HabitCardListAdapter habitCardListAdapter);

    void inject(ArchiveHabitsCommand archiveHabitsCommand);

    void inject(ChangeHabitColorCommand changeHabitColorCommand);

    void inject(UnarchiveHabitsCommand unarchiveHabitsCommand);

    void inject(EditHabitCommand editHabitCommand);

    void inject(CreateHabitCommand createHabitCommand);

    void inject(HabitPickerDialog habitPickerDialog);

    void inject(BaseWidgetProvider baseWidgetProvider);

    void inject(ShowHabitActivity showHabitActivity);

    void inject(DeleteHabitsCommand deleteHabitsCommand);

    void inject(ListHabitsActivity listHabitsActivity);

    void inject(BaseSystem baseSystem);

    void inject(HabitsApplication application);

    void inject(Habit habit);

    void inject(AbstractImporter abstractImporter);

    void inject(HabitsCSVExporter habitsCSVExporter);

    void inject(BaseDialog baseDialog);

    void inject(ShowHabitController showHabitController);

    void inject(BaseWidget baseWidget);

    void inject(WidgetUpdater widgetManager);

    void inject(ListHabitsMenu listHabitsMenu);

    void inject(PebbleReceiver receiver);

    void inject(HeaderView headerView);

    void inject(ReceiverActions receiverActions);

    void inject(ReminderScheduler reminderScheduler);

    void inject(ListHabitsScreen listHabitsScreen);

    void inject(ShowHabitScreen showHabitScreen);

    void inject(ConfirmDeleteDialog confirmDeleteDialog);

    void inject(PendingIntentFactory pendingIntentFactory);
}
