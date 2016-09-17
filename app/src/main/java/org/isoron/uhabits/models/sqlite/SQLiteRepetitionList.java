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

import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.*;
import android.support.annotation.Nullable;

import com.activeandroid.Cache;
import com.activeandroid.query.*;

import org.isoron.uhabits.models.*;
import org.isoron.uhabits.models.sqlite.records.*;
import org.jetbrains.annotations.*;

import java.util.*;

/**
 * Implementation of a {@link RepetitionList} that is backed by SQLite.
 */
public class SQLiteRepetitionList extends RepetitionList
{
    private final SQLiteUtils<RepetitionRecord> sqlite;

    @Nullable
    private HabitRecord habitRecord;

    public SQLiteRepetitionList(@NonNull Habit habit)
    {
        super(habit);
        sqlite = new SQLiteUtils<>(RepetitionRecord.class);
    }

    /**
     * Adds a repetition to the global SQLite database.
     * <p>
     * Given a repetition, this creates and saves the corresponding
     * RepetitionRecord to the database.
     *
     * @param rep the repetition to be added
     */
    @Override
    public void add(Repetition rep)
    {
        check(habit.getId());

        RepetitionRecord record = new RepetitionRecord();
        record.copyFrom(rep);
        record.habit = habitRecord;
        record.save();
        observable.notifyListeners();
    }

    @Override
    public List<Repetition> getByInterval(long timeFrom, long timeTo)
    {
        check(habit.getId());
        String query = "select habit, timestamp " +
                       "from Repetitions " +
                       "where habit = ? and timestamp >= ? and timestamp <= ? " +
                       "order by timestamp";

        String params[] = {
            Long.toString(habit.getId()),
            Long.toString(timeFrom),
            Long.toString(timeTo)
        };

        List<RepetitionRecord> records = sqlite.query(query, params);
        return toRepetitions(records);
    }

    @Override
    @Nullable
    public Repetition getByTimestamp(long timestamp)
    {
        check(habit.getId());
        String query = "select habit, timestamp " +
                       "from Repetitions " +
                       "where habit = ? and timestamp = ? " +
                       "limit 1";

        String params[] =
            { Long.toString(habit.getId()), Long.toString(timestamp) };

        RepetitionRecord record = sqlite.querySingle(query, params);
        if (record == null) return null;
        record.habit = habitRecord;
        return record.toRepetition();
    }

    @Override
    public Repetition getOldest()
    {
        check(habit.getId());
        String query = "select habit, timestamp " +
                       "from Repetitions " +
                       "where habit = ? " +
                       "order by timestamp asc " +
                       "limit 1";

        String params[] = { Long.toString(habit.getId()) };

        RepetitionRecord record = sqlite.querySingle(query, params);
        if (record == null) return null;
        record.habit = habitRecord;
        return record.toRepetition();
    }

    @Override
    public Repetition getNewest()
    {
        check(habit.getId());
        String query = "select habit, timestamp " +
                "from Repetitions " +
                "where habit = ? " +
                "order by timestamp desc " +
                "limit 1";

        String params[] = { Long.toString(habit.getId()) };

        RepetitionRecord record = sqlite.querySingle(query, params);
        if (record == null) return null;
        record.habit = habitRecord;
        return record.toRepetition();
    }

    @Override
    public void remove(@NonNull Repetition repetition)
    {
        new Delete()
            .from(RepetitionRecord.class)
            .where("habit = ?", habit.getId())
            .and("timestamp = ?", repetition.getTimestamp())
            .execute();

        observable.notifyListeners();
    }

    @Contract("null -> fail")
    private void check(Long id)
    {
        if (id == null) throw new RuntimeException("habit is not saved");

        if (habitRecord != null) return;

        habitRecord = HabitRecord.get(id);
        if (habitRecord == null) throw new RuntimeException("habit not found");
    }

    @NonNull
    private List<Repetition> toRepetitions(
        @NonNull List<RepetitionRecord> records)
    {
        check(habit.getId());

        List<Repetition> reps = new LinkedList<>();
        for (RepetitionRecord record : records)
        {
            record.habit = habitRecord;
            reps.add(record.toRepetition());
        }

        return reps;
    }

    @NonNull
    @Override
    public long getTotalCount()
    {
        SQLiteDatabase db = Cache.openDatabase();

        return DatabaseUtils.queryNumEntries(db, "Repetitions",
                "habit=?", new String[] { Long.toString(habit.getId()) });
    }
}
