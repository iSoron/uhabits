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

import android.support.annotation.*;

import com.activeandroid.query.*;

import org.isoron.uhabits.models.*;
import org.isoron.uhabits.models.sqlite.records.*;
import org.isoron.uhabits.utils.*;

import java.util.*;

/**
 * Implementation of a {@link RepetitionList} that is backed by SQLite.
 */
public class SQLiteRepetitionList extends RepetitionList
{
    public SQLiteRepetitionList(@NonNull Habit habit)
    {
        super(habit);
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
        RepetitionRecord record = new RepetitionRecord();
        record.copyFrom(rep);
        record.save();
        observable.notifyListeners();
    }

    @Override
    public List<Repetition> getByInterval(long timeFrom, long timeTo)
    {
        return toRepetitions(selectFromTo(timeFrom, timeTo).execute());
    }

    @Override
    @Nullable
    public Repetition getByTimestamp(long timestamp)
    {
        RepetitionRecord record =
            select().where("timestamp = ?", timestamp).executeSingle();

        if (record == null) return null;
        return record.toRepetition();
    }

    @Override
    public Repetition getOldest()
    {
        RepetitionRecord record = select().limit(1).executeSingle();
        if (record == null) return null;
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

    @NonNull
    private List<Repetition> toRepetitions(
        @Nullable List<RepetitionRecord> records)
    {
        List<Repetition> reps = new LinkedList<>();
        if (records == null) return reps;

        for (RepetitionRecord record : records)
            reps.add(record.toRepetition());

        return reps;
    }
}
