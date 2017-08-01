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
 *
 *
 */

package org.isoron.uhabits.core.models.sqlite;

import org.isoron.uhabits.core.database.*;
import org.isoron.uhabits.core.models.*;
import org.isoron.uhabits.core.models.memory.*;
import org.isoron.uhabits.core.models.sqlite.records.*;

import javax.inject.*;

/**
 * Factory that provides models backed by an SQLite database.
 */
public class SQLModelFactory implements ModelFactory
{
    private final Database db;

    @Inject
    public SQLModelFactory(Database db)
    {
        this.db = db;
    }

    @Override
    public CheckmarkList buildCheckmarkList(Habit habit)
    {
        return new MemoryCheckmarkList(habit);
    }

    @Override
    public HabitList buildHabitList()
    {
        return new SQLiteHabitList(this);
    }

    @Override
    public RepetitionList buildRepetitionList(Habit habit)
    {
        return new SQLiteRepetitionList(habit, this);
    }

    @Override
    public ScoreList buildScoreList(Habit habit)
    {
        return new MemoryScoreList(habit);
    }

    @Override
    public StreakList buildStreakList(Habit habit)
    {
        return new MemoryStreakList(habit);
    }

    @Override
    public Repository<HabitRecord> buildHabitListRepository()
    {
        return new Repository<>(HabitRecord.class, db);
    }

    @Override
    public Repository<RepetitionRecord> buildRepetitionListRepository()
    {
        return new Repository<>(RepetitionRecord.class, db);
    }
}
