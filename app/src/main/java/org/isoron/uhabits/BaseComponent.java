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
import org.isoron.uhabits.tasks.*;
import org.isoron.uhabits.ui.common.dialogs.*;
import org.isoron.uhabits.utils.*;

/**
 * Base component for dependency injection.
 */
public interface BaseComponent
{
    CommandRunner getCommandRunner();

    DialogFactory getDialogFactory();

    DirFinder getDirFinder();

    HabitList getHabitList();

    HabitLogger getHabitLogger();

    IntentFactory getIntentFactory();

    IntentScheduler getIntentScheduler();

    ModelFactory getModelFactory();

    PendingIntentFactory getPendingIntentFactory();

    Preferences getPreferences();

    ReminderScheduler getReminderScheduler();

    TaskRunner getTaskRunner();

    WidgetPreferences getWidgetPreferences();
}
