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

package org.isoron.uhabits.models.sqlite;

import org.isoron.uhabits.*;
import org.isoron.uhabits.models.*;

import dagger.*;

/**
 * Factory that provides models backed by an SQLite database.
 */
@Module
public class SQLModelFactory implements ModelFactory
{
    @Provides
    public static ModelFactory provideModelFactory()
    {
        return new SQLModelFactory();
    }

    @Provides
    @AppScope
    public static HabitList provideHabitList()
    {
        return SQLiteHabitList.getInstance(provideModelFactory());
    }

    @Override
    public CheckmarkList buildCheckmarkList(Habit habit)
    {
        return new SQLiteCheckmarkList(habit);
    }

    @Override
    public HabitList buildHabitList()
    {
        return SQLiteHabitList.getInstance(provideModelFactory());
    }

    @Override
    public RepetitionList buildRepetitionList(Habit habit)
    {
        return new SQLiteRepetitionList(habit);
    }

    @Override
    public ScoreList buildScoreList(Habit habit)
    {
        return new SQLiteScoreList(habit);
    }

    @Override
    public StreakList buildStreakList(Habit habit)
    {
        return new SQLiteStreakList(habit);
    }
}
