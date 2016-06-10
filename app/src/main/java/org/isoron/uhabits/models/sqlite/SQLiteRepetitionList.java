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

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.activeandroid.query.Delete;
import com.activeandroid.query.From;
import com.activeandroid.query.Select;

import org.isoron.uhabits.models.Habit;
import org.isoron.uhabits.models.Repetition;
import org.isoron.uhabits.models.RepetitionList;
import org.isoron.uhabits.utils.DateUtils;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * Implementation of a {@link RepetitionList} that is backed by SQLite.
 */
public class SQLiteRepetitionList extends RepetitionList
{
    HashMap<Long, Repetition> cache;

    public SQLiteRepetitionList(@NonNull Habit habit)
    {
        super(habit);
        this.cache = new HashMap<>();
    }

    @Override
    public void add(Repetition rep)
    {
        RepetitionRecord record = new RepetitionRecord();
        record.copyFrom(rep);
        long id = record.save();
        cache.put(id, rep);
        observable.notifyListeners();
    }

    @Override
    public List<Repetition> getByInterval(long timeFrom, long timeTo)
    {
        return getFromRecord(selectFromTo(timeFrom, timeTo).execute());
    }

    @Override
    public Repetition getByTimestamp(long timestamp)
    {
        RepetitionRecord record =
            select().where("timestamp = ?", timestamp).executeSingle();
        return getFromRecord(record);
    }

    @Override
    public Repetition getOldest()
    {
        RepetitionRecord record = select().limit(1).executeSingle();
        return getFromRecord(record);
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

    @NonNull
    private List<Repetition> getFromRecord(
        @Nullable List<RepetitionRecord> records)
    {
        List<Repetition> reps = new LinkedList<>();
        if (records == null) return reps;

        for (RepetitionRecord record : records)
        {
            Repetition rep = getFromRecord(record);
            reps.add(rep);
        }

        return reps;
    }

    @Nullable
    private Repetition getFromRecord(@Nullable RepetitionRecord record)
    {
        if (record == null) return null;

        Long id = record.getId();

        if (!cache.containsKey(id))
        {
            Repetition repetition = record.toRepetition();
            cache.put(id, repetition);
        }

        return cache.get(id);
    }

    @NonNull
    private From select()
    {
        return new Select()
            .from(RepetitionRecord.class)
            .where("habit = ?", habit.getId())
            .and("timestamp <= ?", DateUtils.getStartOfToday())
            .orderBy("timestamp");
    }

    @NonNull
    private From selectFromTo(long timeFrom, long timeTo)
    {
        return select()
            .and("timestamp >= ?", timeFrom)
            .and("timestamp <= ?", timeTo);
    }
}
