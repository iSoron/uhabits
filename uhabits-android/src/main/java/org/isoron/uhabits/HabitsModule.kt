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

package org.isoron.uhabits

import dagger.*
import org.isoron.uhabits.core.*
import org.isoron.uhabits.core.commands.*
import org.isoron.uhabits.core.database.*
import org.isoron.uhabits.core.models.*
import org.isoron.uhabits.core.models.sqlite.*
import org.isoron.uhabits.core.preferences.*
import org.isoron.uhabits.core.reminders.*
import org.isoron.uhabits.core.tasks.*
import org.isoron.uhabits.core.ui.*
import org.isoron.uhabits.database.*
import org.isoron.uhabits.intents.*
import org.isoron.uhabits.notifications.*
import org.isoron.uhabits.preferences.*
import org.isoron.uhabits.utils.*

@Module
class HabitsModule {
    @Provides
    @AppScope
    fun getPreferences(storage: SharedPreferencesStorage): Preferences {
        return Preferences(storage)
    }

    @Provides
    @AppScope
    fun getReminderScheduler(
            sys: IntentScheduler,
            commandRunner: CommandRunner,
            habitList: HabitList
    ): ReminderScheduler {
        return ReminderScheduler(commandRunner, habitList, sys)
    }

    @Provides
    @AppScope
    fun getTray(
            taskRunner: TaskRunner,
            commandRunner: CommandRunner,
            preferences: Preferences,
            screen: AndroidNotificationTray
    ): NotificationTray {
        return NotificationTray(taskRunner, commandRunner, preferences, screen)
    }

    @Provides
    @AppScope
    fun getWidgetPreferences(
            storage: SharedPreferencesStorage
    ): WidgetPreferences {
        return WidgetPreferences(storage)
    }

    @Provides
    @AppScope
    fun getModelFactory(): ModelFactory {
        return SQLModelFactory(AndroidDatabase(DatabaseUtils.openDatabase()))
    }

    @Provides
    @AppScope
    fun getHabitList(list: SQLiteHabitList): HabitList {
        return list
    }

    @Provides
    @AppScope
    fun getDatabaseOpener(opener: AndroidDatabaseOpener): DatabaseOpener {
        return opener
    }
}

