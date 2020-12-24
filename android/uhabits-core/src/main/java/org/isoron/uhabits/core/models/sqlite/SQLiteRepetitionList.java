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

import androidx.annotation.*;
import androidx.annotation.Nullable;

import org.isoron.uhabits.core.database.*;
import org.isoron.uhabits.core.models.*;
import org.isoron.uhabits.core.models.memory.*;
import org.isoron.uhabits.core.models.sqlite.records.*;
import org.jetbrains.annotations.*;

import java.util.*;

/**
 * Implementation of a {@link RepetitionList} that is backed by SQLite.
 */
public class SQLiteRepetitionList extends RepetitionList
{
    private final Repository<RepetitionRecord> repository;

    private final MemoryRepetitionList list;

    private boolean loaded = false;

    public SQLiteRepetitionList(@NonNull Habit habit,
                                @NonNull ModelFactory modelFactory)
    {
        super(habit);
        repository = modelFactory.buildRepetitionListRepository();
        list = new MemoryRepetitionList(habit);
    }

    private void loadRecords()
    {
        if (loaded) return;
        loaded = true;

        check(habit.getId());
        List<RepetitionRecord> records =
            repository.findAll("where habit = ? order by timestamp",
                habit.getId().toString());

        for (RepetitionRecord rec : records)
            list.add(rec.toCheckmark());
    }

    @Override
    public void add(Entry entry)
    {
        loadRecords();
        list.add(entry);
        check(habit.getId());
        RepetitionRecord record = new RepetitionRecord();
        record.habit_id = habit.getId();
        record.copyFrom(entry);
        repository.save(record);
    }

    @Override
    public List<Entry> getByInterval(Timestamp timeFrom, Timestamp timeTo)
    {
        loadRecords();
        return list.getByInterval(timeFrom, timeTo);
    }

    @Override
    @Nullable
    public Entry getByTimestamp(Timestamp timestamp)
    {
        loadRecords();
        return list.getByTimestamp(timestamp);
    }

    @Override
    public Entry getOldest()
    {
        loadRecords();
        return list.getOldest();
    }

    @Override
    public Entry getNewest()
    {
        loadRecords();
        return list.getNewest();
    }

    @Override
    public void remove(@NonNull Entry entry)
    {
        loadRecords();
        list.remove(entry);
        check(habit.getId());
        repository.execSQL(
            "delete from repetitions where habit = ? and timestamp = ?",
            habit.getId(), entry.getTimestamp().getUnixTime());
    }

    public void removeAll()
    {
        loadRecords();
        list.removeAll();
        check(habit.getId());
        repository.execSQL("delete from repetitions where habit = ?",
            habit.getId());
    }

    @Override
    public long getTotalCount()
    {
        loadRecords();
        return list.getTotalCount();
    }

    public void reload()
    {
        loaded = false;
    }

    @Contract("null -> fail")
    private void check(Long value)
    {
        if (value == null) throw new RuntimeException("null check failed");
    }
}
