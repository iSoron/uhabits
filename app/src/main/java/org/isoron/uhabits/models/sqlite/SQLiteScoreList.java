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

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.activeandroid.Cache;
import com.activeandroid.query.Delete;
import com.activeandroid.query.From;
import com.activeandroid.query.Select;
import com.activeandroid.util.SQLiteUtils;

import org.isoron.uhabits.models.Habit;
import org.isoron.uhabits.models.Repetition;
import org.isoron.uhabits.models.Score;
import org.isoron.uhabits.models.ScoreList;
import org.isoron.uhabits.utils.DateUtils;

import java.util.List;

/**
 * Implementation of a ScoreList that is backed by SQLite.
 */
public class SQLiteScoreList extends ScoreList
{
    /**
     * Constructs a new ScoreList associated with the given habit.
     *
     * @param habit the habit this list should be associated with
     */
    public SQLiteScoreList(@NonNull Habit habit)
    {
        super(habit);
    }

    @Override
    public int getValue(long timestamp)
    {
        computeAll();
        String[] args = {habit.getId().toString(), Long.toString(timestamp)};
        return SQLiteUtils.intQuery(
            "select score from Score where habit = ? and timestamp = ?", args);
    }

    @Override
    public void invalidateNewerThan(long timestamp)
    {
        new Delete()
            .from(ScoreRecord.class)
            .where("habit = ?", habit.getId())
            .and("timestamp >= ?", timestamp)
            .execute();
    }

    @Nullable
    @Override
    protected Score getNewestComputed()
    {
        ScoreRecord record = select().limit(1).executeSingle();
        return record.toScore();
    }

    @Override
    @Nullable
    protected Score get(long timestamp)
    {
        Repetition oldestRep = habit.getRepetitions().getOldest();
        if (oldestRep == null) return null;
        compute(oldestRep.getTimestamp(), timestamp);

        ScoreRecord record =
            select().where("timestamp = ?", timestamp).executeSingle();

        return record.toScore();
    }

    @Override
    @NonNull
    protected int[] getValues(long from, long to, long divisor)
    {
        compute(from, to);

        divisor *= DateUtils.millisecondsInOneDay;
        Long offset = to + divisor;

        String query =
            "select ((timestamp - ?) / ?) as time, avg(score) from Score " +
            "where habit = ? and timestamp >= ? and timestamp <= ? " +
            "group by time order by time desc";

        String params[] = {
            offset.toString(),
            Long.toString(divisor),
            habit.getId().toString(),
            Long.toString(from),
            Long.toString(to)
        };

        SQLiteDatabase db = Cache.openDatabase();
        Cursor cursor = db.rawQuery(query, params);

        if (!cursor.moveToFirst()) return new int[0];

        int k = 0;
        int[] scores = new int[cursor.getCount()];

        do
        {
            scores[k++] = (int) cursor.getFloat(1);
        } while (cursor.moveToNext());

        cursor.close();
        return scores;
    }

    @Override
    protected void add(List<Score> scores)
    {
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

    protected From select()
    {
        return new Select()
            .from(ScoreRecord.class)
            .where("habit = ?", habit.getId())
            .orderBy("timestamp desc");
    }
}
