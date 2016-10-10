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

import android.database.sqlite.*;
import android.support.annotation.*;
import android.support.annotation.Nullable;

import com.activeandroid.*;
import com.activeandroid.query.*;

import org.isoron.uhabits.models.*;
import org.isoron.uhabits.models.sqlite.records.*;
import org.jetbrains.annotations.*;

import java.util.*;

/**
 * Implementation of a ScoreList that is backed by SQLite.
 */
public class SQLiteScoreList extends ScoreList
{
    @Nullable
    private HabitRecord habitRecord;

    @NonNull
    private final SQLiteUtils<ScoreRecord> sqlite;

    /**
     * Constructs a new ScoreList associated with the given habit.
     *
     * @param habit the habit this list should be associated with
     */
    public SQLiteScoreList(@NonNull Habit habit)
    {
        super(habit);
        sqlite = new SQLiteUtils<>(ScoreRecord.class);
    }

    @Override
    public void add(List<Score> scores)
    {
        check(habit.getId());
        String query =
            "insert into Score(habit, timestamp, score) values (?,?,?)";

        SQLiteDatabase db = Cache.openDatabase();
        db.beginTransaction();

        try
        {
            SQLiteStatement statement = db.compileStatement(query);

            for (Score s : scores)
            {
                statement.bindLong(1, habit.getId());
                statement.bindLong(2, s.getTimestamp());
                statement.bindLong(3, s.getValue());
                statement.execute();
            }

            db.setTransactionSuccessful();
        }
        finally
        {
            db.endTransaction();
        }
    }

    @NonNull
    @Override
    public List<Score> getByInterval(long fromTimestamp, long toTimestamp)
    {
        check(habit.getId());
        compute(fromTimestamp, toTimestamp);

        String query = "select habit, timestamp, score " +
                "from Score " +
                "where habit = ? and timestamp >= ? and timestamp <= ? " +
                "order by timestamp desc";

        String params[] = {
                Long.toString(habit.getId()),
                Long.toString(fromTimestamp),
                Long.toString(toTimestamp)
        };

        List<ScoreRecord> records = sqlite.query(query, params);
        for (ScoreRecord record : records) record.habit = habitRecord;
        return toScores(records);
    }

    @Override
    @Nullable
    public Score getComputedByTimestamp(long timestamp)
    {
        check(habit.getId());

        String query = "select habit, timestamp, score from Score " +
                       "where habit = ? and timestamp = ? " +
                       "order by timestamp desc";

        String params[] =
            { Long.toString(habit.getId()), Long.toString(timestamp) };

        return getScoreFromQuery(query, params);
    }

    @Override
    public void invalidateNewerThan(long timestamp)
    {
        new Delete()
            .from(ScoreRecord.class)
            .where("habit = ?", habit.getId())
            .and("timestamp >= ?", timestamp)
            .execute();

        getObservable().notifyListeners();
    }

    @Override
    @NonNull
    public List<Score> toList()
    {
        check(habit.getId());
        computeAll();

        String query = "select habit, timestamp, score from Score " +
                       "where habit = ? order by timestamp desc";

        String params[] = { Long.toString(habit.getId()) };

        List<ScoreRecord> records = sqlite.query(query, params);
        for (ScoreRecord record : records) record.habit = habitRecord;

        return toScores(records);
    }

    @Nullable
    @Override
    protected Score getNewestComputed()
    {
        check(habit.getId());
        String query = "select habit, timestamp, score from Score " +
                       "where habit = ? order by timestamp desc " +
                       "limit 1";

        String params[] = { Long.toString(habit.getId()) };
        return getScoreFromQuery(query, params);
    }

    @Nullable
    @Override
    protected Score getOldestComputed()
    {
        check(habit.getId());
        String query = "select habit, timestamp, score from Score " +
                       "where habit = ? order by timestamp asc " +
                       "limit 1";

        String params[] = { Long.toString(habit.getId()) };
        return getScoreFromQuery(query, params);
    }

    @Contract("null -> fail")
    private void check(Long id)
    {
        if (id == null) throw new RuntimeException("habit is not saved");
        if (habitRecord != null) return;
        habitRecord = HabitRecord.get(id);
        if (habitRecord == null) throw new RuntimeException("habit not found");
    }

    @Nullable
    private Score getScoreFromQuery(String query, String[] params)
    {
        ScoreRecord record = sqlite.querySingle(query, params);
        if (record == null) return null;
        record.habit = habitRecord;
        return record.toScore();
    }

    @NonNull
    private List<Score> toScores(@NonNull List<ScoreRecord> records)
    {
        List<Score> scores = new LinkedList<>();
        for (ScoreRecord r : records) scores.add(r.toScore());
        return scores;
    }
}
