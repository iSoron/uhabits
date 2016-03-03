/* Copyright (C) 2016 Alinson Santos Xavier
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied  warranty of MERCHANTABILITY or
 * FITNESS  FOR  A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You  should  have  received  a  copy  of the GNU General Public License
 * along  with  this  program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.isoron.uhabits.models;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.Cache;
import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;

import org.isoron.helpers.DateHelper;

public class ScoreList
{
    private Habit habit;

    public ScoreList(Habit habit)
    {
        this.habit = habit;
    }

    public int getCurrentStarStatus()
    {
        int score = getNewestValue();

        if(score >= Score.FULL_STAR_CUTOFF) return 2;
        else if(score >= Score.HALF_STAR_CUTOFF) return 1;
        else return 0;
    }

    public Score getNewest()
    {
        return new Select().from(Score.class)
                .where("habit = ?", habit.getId())
                .orderBy("timestamp desc")
                .limit(1)
                .executeSingle();
    }

    public void deleteNewerThan(long timestamp)
    {
        new Delete().from(Score.class)
                .where("habit = ?", habit.getId())
                .and("timestamp >= ?", timestamp)
                .execute();
    }

    public Integer getNewestValue()
    {
        int beginningScore;
        long beginningTime;

        long today = DateHelper.getStartOfDay(DateHelper.getLocalTime());
        long day = DateHelper.millisecondsInOneDay;

        double freq = ((double) habit.freqNum) / habit.freqDen;
        double multiplier = Math.pow(0.5, 1.0 / (14.0 / freq - 1));

        Score newestScore = getNewest();
        if (newestScore == null)
        {
            Repetition oldestRep = habit.repetitions.getOldest();
            if (oldestRep == null) return 0;
            beginningTime = oldestRep.timestamp;
            beginningScore = 0;
        }
        else
        {
            beginningTime = newestScore.timestamp + day;
            beginningScore = newestScore.score;
        }

        long nDays = (today - beginningTime) / day;
        if (nDays < 0) return newestScore.score;

        int reps[] = habit.checkmarks.getValues(beginningTime, today);

        ActiveAndroid.beginTransaction();
        int lastScore = beginningScore;

        try
        {
            for (int i = 0; i < reps.length; i++)
            {
                Score s = new Score();
                s.habit = habit;
                s.timestamp = beginningTime + day * i;
                s.score = (int) (lastScore * multiplier);
                if (reps[reps.length - i - 1] == 2)
                {
                    s.score += 1000000;
                    s.score = Math.min(s.score, Score.MAX_SCORE);
                }
                s.save();

                lastScore = s.score;
            }

            ActiveAndroid.setTransactionSuccessful();
        } finally
        {
            ActiveAndroid.endTransaction();
        }

        return lastScore;
    }

    public int[] getAllValues(Long fromTimestamp, Long toTimestamp, Integer divisor, Long offset)
    {
        String query = "select score from Score where habit = ? and timestamp > ? and " +
                "timestamp <= ? and (timestamp - ?) % ? = 0 order by timestamp desc";

        String params[] = { habit.getId().toString(), fromTimestamp.toString(),
                toTimestamp.toString(), offset.toString(), divisor.toString()};

        SQLiteDatabase db = Cache.openDatabase();
        Cursor cursor = db.rawQuery(query, params);

        if(!cursor.moveToFirst()) return new int[0];

        int k = 0;
        int[] scores = new int[cursor.getCount()];

        do
        {
            scores[k++] = cursor.getInt(0);
        }
        while (cursor.moveToNext());

        cursor.close();
        return scores;

    }

    public int[] getAllValues(int divisor)
    {
        Repetition oldestRep = habit.repetitions.getOldest();
        if(oldestRep == null) return new int[0];

        long fromTimestamp = oldestRep.timestamp;
        long toTimestamp = DateHelper.getStartOfToday();
        return getAllValues(fromTimestamp, toTimestamp, divisor, toTimestamp);
    }
}
