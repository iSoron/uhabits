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

import com.activeandroid.*;
import com.activeandroid.query.*;

import org.isoron.uhabits.models.*;
import org.isoron.uhabits.models.sqlite.records.*;

import java.util.*;

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
    @NonNull
    public List<Score> getAll()
    {
        computeAll();

        List<ScoreRecord> records = select().execute();
        List<Score> scores = new LinkedList<>();

        for (ScoreRecord rec : records)
            scores.add(rec.toScore());

        return scores;
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

    @Override
    public void add(List<Score> scores)
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

    @Override
    @Nullable
    public Score getByTimestamp(long timestamp)
    {
        computeAll();

        ScoreRecord record =
            select().where("timestamp = ?", timestamp).executeSingle();

        if (record == null) return null;
        return record.toScore();
    }

    @Nullable
    @Override
    protected Score getNewestComputed()
    {
        ScoreRecord record = select().limit(1).executeSingle();
        if (record == null) return null;
        return record.toScore();
    }

    private From select()
    {
        return new Select()
            .from(ScoreRecord.class)
            .where("habit = ?", habit.getId())
            .orderBy("timestamp desc");
    }
}
