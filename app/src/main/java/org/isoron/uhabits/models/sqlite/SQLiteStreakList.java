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
import com.activeandroid.query.Select;

import org.isoron.uhabits.models.Habit;
import org.isoron.uhabits.models.Streak;
import org.isoron.uhabits.models.StreakList;
import org.isoron.uhabits.utils.DatabaseUtils;
import org.isoron.uhabits.utils.DateUtils;

import java.util.LinkedList;
import java.util.List;

/**
 * Implementation of a StreakList that is backed by SQLite.
 */
public class SQLiteStreakList extends StreakList
{
    public SQLiteStreakList(Habit habit)
    {
        super(habit);
    }

    @Override
    public List<Streak> getAll()
    {
        rebuild();
        List<StreakRecord> records = new Select()
            .from(StreakRecord.class)
            .where("habit = ?", habit.getId())
            .orderBy("end desc")
            .execute();

        return recordsToStreaks(records);
    }

    @Override
    public Streak getNewestComputed()
    {
        StreakRecord newestRecord = getNewestRecord();
        if(newestRecord == null) return null;
        return newestRecord.toStreak();
    }

    @Override
    public void invalidateNewerThan(long timestamp)
    {
        new Delete()
            .from(StreakRecord.class)
            .where("habit = ?", habit.getId())
            .and("end >= ?", timestamp - DateUtils.millisecondsInOneDay)
            .execute();

        observable.notifyListeners();
    }

    @Nullable
    private StreakRecord getNewestRecord()
    {
        return new Select()
            .from(StreakRecord.class)
            .where("habit = ?", habit.getId())
            .orderBy("end desc")
            .limit(1)
            .executeSingle();
    }

    @Override
    protected void insert(@NonNull List<Streak> streaks)
    {
        DatabaseUtils.executeAsTransaction(() -> {
            for (Streak streak : streaks)
            {
                StreakRecord record = new StreakRecord();
                record.copyFrom(streak);
                record.save();
            }
        });
    }

    @NonNull
    private List<Streak> recordsToStreaks(List<StreakRecord> records)
    {
        LinkedList<Streak> streaks = new LinkedList<>();

        for (StreakRecord record : records)
            streaks.add(record.toStreak());

        return streaks;
    }

    @Override
    protected void removeNewestComputed()
    {
        StreakRecord newestStreak = getNewestRecord();
        if (newestStreak != null) newestStreak.delete();
    }
}
