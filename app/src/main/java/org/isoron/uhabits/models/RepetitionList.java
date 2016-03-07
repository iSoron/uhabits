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

package org.isoron.uhabits.models;

import com.activeandroid.query.Delete;
import com.activeandroid.query.From;
import com.activeandroid.query.Select;

import org.isoron.helpers.DateHelper;

public class RepetitionList
{

    private Habit habit;

    public RepetitionList(Habit habit)
    {
        this.habit = habit;
    }

    protected From select()
    {
        return new Select().from(Repetition.class)
                .where("habit = ?", habit.getId())
                .orderBy("timestamp");
    }

    protected From selectFromTo(long timeFrom, long timeTo)
    {
        return select().and("timestamp >= ?", timeFrom).and("timestamp <= ?", timeTo);
    }

    public boolean contains(long timestamp)
    {
        int count = select().where("timestamp = ?", timestamp).count();
        return (count > 0);
    }

    public void delete(long timestamp)
    {
        new Delete().from(Repetition.class)
                .where("habit = ?", habit.getId())
                .and("timestamp = ?", timestamp)
                .execute();
    }

    public Repetition getOldestNewerThan(long timestamp)
    {
        return select().where("timestamp > ?", timestamp).limit(1).executeSingle();
    }

    public void toggle(long timestamp)
    {
        if (contains(timestamp))
        {
            delete(timestamp);
        }
        else
        {
            Repetition rep = new Repetition();
            rep.habit = habit;
            rep.timestamp = timestamp;
            rep.save();
        }

        habit.scores.deleteNewerThan(timestamp);
        habit.checkmarks.deleteNewerThan(timestamp);
        habit.streaks.deleteNewerThan(timestamp);
    }

    public Repetition getOldest()
    {
        return (Repetition) select().limit(1).executeSingle();
    }

    public boolean hasImplicitRepToday()
    {
        long today = DateHelper.getStartOfToday();
        int reps[] = habit.checkmarks.getValues(today - DateHelper.millisecondsInOneDay, today);
        return (reps[0] > 0);
    }
}
