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

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.Cache;
import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Delete;
import com.activeandroid.query.From;
import com.activeandroid.query.Select;
import com.activeandroid.query.Update;
import com.activeandroid.util.SQLiteUtils;

import org.isoron.helpers.ColorHelper;
import org.isoron.helpers.Command;
import org.isoron.helpers.DateHelper;
import org.isoron.uhabits.R;

import java.util.ArrayList;
import java.util.List;

@Table(name = "Habits")
public class Habit extends Model
{

    public static final int HALF_STAR_CUTOFF =  9629750;
    public static final int FULL_STAR_CUTOFF = 15407600;
    public static final int MAX_SCORE        = 19259500;

    @Column(name = "name")
    public String name;

    @Column(name = "description")
    public String description;

    @Column(name = "freq_num")
    public Integer freqNum;

    @Column(name = "freq_den")
    public Integer freqDen;

    @Column(name = "color")
    public Integer color;

    @Column(name = "position")
    public Integer position;

    @Column(name = "reminder_hour")
    public Integer reminderHour;

    @Column(name = "reminder_min")
    public Integer reminderMin;

    @Column(name = "reminder_days")
    public Integer reminderDays;

    @Column(name = "highlight")
    public Integer highlight;

    @Column(name = "archived")
    public Integer archived;

    public Habit(Habit model)
    {
        copyAttributes(model);
    }

    public Habit()
    {
        this.color = ColorHelper.palette[5];
        this.position = Habit.getCount();
        this.highlight = 0;
        this.archived = 0;
        this.freqDen = 7;
        this.freqNum = 3;
        this.reminderDays = 127;
    }

    public static Habit get(Long id)
    {
        return Habit.load(Habit.class, id);
    }

    public static List<Habit> getAll(boolean includeArchive)
    {
        if(includeArchive) return selectWithArchived().execute();
        else return select().execute();
    }

    @SuppressLint("DefaultLocale")
    public static void updateId(long oldId, long newId)
    {
        SQLiteUtils.execSql(String.format("update Habits set Id = %d where Id = %d", newId, oldId));
    }

    protected static From select()
    {
        return new Select().from(Habit.class).where("archived = 0").orderBy("position");
    }

    public static From selectWithArchived()
    {
        return new Select().from(Habit.class).orderBy("position");
    }

    public static int getCount()
    {
        return select().count();
    }

    public static java.util.List<Habit> getHighlightedHabits()
    {
        return select().where("highlight = 1")
                .orderBy("reminder_hour desc, reminder_min desc")
                .execute();
    }

    public static java.util.List<Habit> getHabitsWithReminder()
    {
        return select().where("reminder_hour is not null").execute();
    }

    public static void reorder(Habit from, Habit to)
    {
        if(from == to) return;

        if (to.position < from.position)
        {
            new Update(Habit.class).set("position = position + 1")
                    .where("position >= ? and position < ?", to.position, from.position)
                    .execute();
        }
        else
        {
            new Update(Habit.class).set("position = position - 1")
                    .where("position > ? and position <= ?", from.position, to.position)
                    .execute();
        }

        from.position = to.position;
        from.save();
    }

    public static void rebuildOrder()
    {
        List<Habit> habits = selectWithArchived().execute();

        ActiveAndroid.beginTransaction();
        try
        {
            int i = 0;
            for (Habit h : habits)
            {
                h.position = i++;
                h.save();
            }

            ActiveAndroid.setTransactionSuccessful();
        } finally
        {
            ActiveAndroid.endTransaction();
        }

    }

    public void copyAttributes(Habit model)
    {
        this.name = model.name;
        this.description = model.description;
        this.freqNum = model.freqNum;
        this.freqDen = model.freqDen;
        this.color = model.color;
        this.position = model.position;
        this.reminderHour = model.reminderHour;
        this.reminderMin = model.reminderMin;
        this.reminderDays = model.reminderDays;
        this.highlight = model.highlight;
        this.archived = model.archived;
    }

    public void save(Long id)
    {
        save();
        Habit.updateId(getId(), id);
    }

    protected From selectReps()
    {
        return new Select().from(Repetition.class).where("habit = ?", getId()).orderBy("timestamp");
    }

    protected From selectRepsFromTo(long timeFrom, long timeTo)
    {
        return selectReps().and("timestamp >= ?", timeFrom).and("timestamp <= ?", timeTo);
    }

    public boolean hasRep(long timestamp)
    {
        int count = selectReps().where("timestamp = ?", timestamp).count();
        return (count > 0);
    }

    public void cascadeDelete()
    {
        Long id = getId();

        ActiveAndroid.beginTransaction();
        try
        {
            new Delete().from(Checkmark.class).where("habit = ?", id).execute();
            new Delete().from(Repetition.class).where("habit = ?", id).execute();
            new Delete().from(Score.class).where("habit = ?", id).execute();
            new Delete().from(Streak.class).where("habit = ?", id).execute();
            delete();

            ActiveAndroid.setTransactionSuccessful();
        }
        finally
        {
            ActiveAndroid.endTransaction();
        }
    }

    public void deleteReps(long timestamp)
    {
        new Delete().from(Repetition.class)
                .where("habit = ?", getId())
                .and("timestamp = ?", timestamp)
                .execute();
    }

    public void deleteCheckmarksNewerThan(long timestamp)
    {
        new Delete().from(Checkmark.class)
                .where("habit = ?", getId())
                .and("timestamp >= ?", timestamp)
                .execute();
    }

    public void deleteStreaksNewerThan(long timestamp)
    {
        new Delete().from(Streak.class)
                .where("habit = ?", getId())
                .and("end >= ?", timestamp - DateHelper.millisecondsInOneDay)
                .execute();
    }

    public int[] getCheckmarks(Long fromTimestamp, Long toTimestamp)
    {
        updateCheckmarks();

        if(fromTimestamp > toTimestamp) return new int[0];

        String query = "select value, timestamp from Checkmarks where " +
                "habit = ? and timestamp >= ? and timestamp <= ?";

        SQLiteDatabase db = Cache.openDatabase();
        String args[] = {getId().toString(), fromTimestamp.toString(), toTimestamp.toString()};
        Cursor cursor = db.rawQuery(query, args);

        long day = DateHelper.millisecondsInOneDay;
        int nDays = (int) ((toTimestamp - fromTimestamp) / day) + 1;
        int[] checks = new int[nDays];

        if (cursor.moveToFirst())
        {
            do
            {
                long timestamp = cursor.getLong(1);
                int offset = (int) ((timestamp - fromTimestamp) / day);
                checks[nDays - offset - 1] = cursor.getInt(0);

            } while (cursor.moveToNext());
        }

        cursor.close();
        return checks;
    }

    public int[] getAllCheckmarks()
    {
        Repetition oldestRep = getOldestRep();
        if(oldestRep == null) return new int[0];

        Long toTimestamp = DateHelper.getStartOfToday();
        Long fromTimestamp = oldestRep.timestamp;
        return getCheckmarks(fromTimestamp, toTimestamp);
    }

    public void updateCheckmarks()
    {
        long beginning;
        long today = DateHelper.getStartOfToday();
        long day = DateHelper.millisecondsInOneDay;

        Checkmark newestCheckmark = getNewestCheckmark();
        if (newestCheckmark == null)
        {
            Repetition oldestRep = getOldestRep();
            if (oldestRep == null) return;

            beginning = oldestRep.timestamp;
        }
        else
        {
            beginning = newestCheckmark.timestamp + day;
        }

        if (beginning > today) return;

        long beginningExtended = beginning - (long) (freqDen) * day;
        List<Repetition> reps = selectRepsFromTo(beginningExtended, today).execute();

        int nDays = (int) ((today - beginning) / day) + 1;
        int nDaysExtended = (int) ((today - beginningExtended) / day) + 1;

        int checks[] = new int[nDaysExtended];

        // explicit checks
        for (Repetition rep : reps)
        {
            int offset = (int) ((rep.timestamp - beginningExtended) / day);
            checks[nDaysExtended - offset - 1] = 2;
        }

        // implicit checks
        for (int i = 0; i < nDays; i++)
        {
            int counter = 0;

            for (int j = 0; j < freqDen; j++)
                if (checks[i + j] == 2) counter++;

            if (counter >= freqNum) checks[i] = Math.max(checks[i], 1);
        }

        ActiveAndroid.beginTransaction();

        try
        {
            for (int i = 0; i < nDays; i++)
            {
                Checkmark c = new Checkmark();
                c.habit = this;
                c.timestamp = today - i * day;
                c.value = checks[i];
                c.save();
            }

            ActiveAndroid.setTransactionSuccessful();
        } finally
        {
            ActiveAndroid.endTransaction();
        }
    }

    public Checkmark getNewestCheckmark()
    {
        return new Select().from(Checkmark.class)
                .where("habit = ?", getId())
                .orderBy("timestamp desc")
                .limit(1)
                .executeSingle();
    }

    public int getRepsCount(int days)
    {
        long timeTo = DateHelper.getStartOfToday();
        long timeFrom = timeTo - DateHelper.millisecondsInOneDay * days;
        return selectRepsFromTo(timeFrom, timeTo).count();
    }

    public boolean hasImplicitRepToday()
    {
        long today = DateHelper.getStartOfToday();
        int reps[] = getCheckmarks(today - DateHelper.millisecondsInOneDay, today);
        return (reps[0] > 0);
    }

    public Repetition getOldestRep()
    {
        return (Repetition) selectReps().limit(1).executeSingle();
    }

    public Repetition getOldestRepNewerThan(long timestamp)
    {
        return selectReps().where("timestamp > ?", timestamp).limit(1).executeSingle();
    }

    public void toggleRepetition(long timestamp)
    {
        if (hasRep(timestamp))
        {
            deleteReps(timestamp);
        }
        else
        {
            Repetition rep = new Repetition();
            rep.habit = this;
            rep.timestamp = timestamp;
            rep.save();
        }

        deleteScoresNewerThan(timestamp);
        deleteCheckmarksNewerThan(timestamp);
        deleteStreaksNewerThan(timestamp);
    }

    public void archive()
    {
        archived = 1;
        save();
    }

    public void unarchive()
    {
        archived = 0;
        save();
    }

    public boolean isArchived()
    {
        return archived != 0;
    }

    public void toggleRepetitionToday()
    {
        toggleRepetition(DateHelper.getStartOfToday());
    }

    public Score getNewestScore()
    {
        return new Select().from(Score.class)
                .where("habit = ?", getId())
                .orderBy("timestamp desc")
                .limit(1)
                .executeSingle();
    }

    public void deleteScoresNewerThan(long timestamp)
    {
        new Delete().from(Score.class)
                .where("habit = ?", getId())
                .and("timestamp >= ?", timestamp)
                .execute();
    }

    public Integer getScore()
    {
        int beginningScore;
        long beginningTime;

        long today = DateHelper.getStartOfDay(DateHelper.getLocalTime());
        long day = DateHelper.millisecondsInOneDay;

        double freq = ((double) freqNum) / freqDen;
        double multiplier = Math.pow(0.5, 1.0 / (14.0 / freq - 1));

        Score newestScore = getNewestScore();
        if (newestScore == null)
        {
            Repetition oldestRep = getOldestRep();
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

        int reps[] = getCheckmarks(beginningTime, today);

        ActiveAndroid.beginTransaction();
        int lastScore = beginningScore;

        try
        {
            for (int i = 0; i < reps.length; i++)
            {
                Score s = new Score();
                s.habit = this;
                s.timestamp = beginningTime + day * i;
                s.score = (int) (lastScore * multiplier);
                if (reps[reps.length - i - 1] == 2)
                {
                    s.score += 1000000;
                    s.score = Math.min(s.score, MAX_SCORE);
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

    public int[] getScores(Long fromTimestamp, Long toTimestamp, Integer divisor, Long offset)
    {
        String query = "select score from Score where habit = ? and timestamp > ? and " +
                "timestamp <= ? and (timestamp - ?) % ? = 0 order by timestamp desc";

        String params[] = {getId().toString(), fromTimestamp.toString(), toTimestamp.toString(),
                offset.toString(), divisor.toString()};

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

    public int[] getAllScores(int divisor)
    {
        Repetition oldestRep = getOldestRep();
        if(oldestRep == null) return new int[0];

        long fromTimestamp = oldestRep.timestamp;
        long toTimestamp = DateHelper.getStartOfToday();
        return getScores(fromTimestamp, toTimestamp, divisor, toTimestamp);
    }

    public List<Streak> getStreaks()
    {
        updateStreaks();

        return new Select().from(Streak.class)
                .where("habit = ?", getId())
                .orderBy("end asc")
                .execute();
    }

    public Streak getNewestStreak()
    {
        return new Select().from(Streak.class)
                .where("habit = ?", getId())
                .orderBy("end desc")
                .limit(1)
                .executeSingle();
    }

    public void updateStreaks()
    {
        long beginning;
        long today = DateHelper.getStartOfToday();
        long day = DateHelper.millisecondsInOneDay;

        Streak newestStreak = getNewestStreak();
        if (newestStreak == null)
        {
            Repetition oldestRep = getOldestRep();
            if (oldestRep == null) return;

            beginning = oldestRep.timestamp;
        }
        else
        {
            Repetition oldestRep = getOldestRepNewerThan(newestStreak.end);
            if (oldestRep == null) return;

            beginning = oldestRep.timestamp;
        }

        if (beginning > today) return;

        int checks[] = getCheckmarks(beginning, today);
        ArrayList<Long> list = new ArrayList<>();

        long current = beginning;
        list.add(current);

        for (int i = 1; i < checks.length; i++)
        {
            current += day;
            int j = checks.length - i - 1;

            if ((checks[j + 1] == 0 && checks[j] > 0)) list.add(current);
            if ((checks[j + 1] > 0 && checks[j] == 0)) list.add(current - day);
        }

        if (list.size() % 2 == 1) list.add(current);

        ActiveAndroid.beginTransaction();

        try
        {
            for (int i = 0; i < list.size(); i += 2)
            {
                Streak streak = new Streak();
                streak.habit = this;
                streak.start = list.get(i);
                streak.end = list.get(i + 1);
                streak.length = (streak.end - streak.start) / day + 1;
                streak.save();
            }

            ActiveAndroid.setTransactionSuccessful();
        } finally
        {
            ActiveAndroid.endTransaction();
        }
    }

    public static class CreateCommand extends Command
    {
        private Habit model;
        private Long savedId;

        public CreateCommand(Habit model)
        {
            this.model = model;
        }

        @Override
        public void execute()
        {
            Habit savedHabit = new Habit(model);
            if (savedId == null)
            {
                savedHabit.save();
                savedId = savedHabit.getId();
            }
            else
            {
                savedHabit.save(savedId);
            }
        }

        @Override
        public void undo()
        {
            Habit.get(savedId).delete();
        }

        @Override
        public Integer getExecuteStringId()
        {
            return R.string.toast_habit_created;
        }

        @Override
        public Integer getUndoStringId()
        {
            return R.string.toast_habit_deleted;
        }

    }

    public class EditCommand extends Command
    {
        private Habit original;
        private Habit modified;
        private long savedId;
        private boolean hasIntervalChanged;

        public EditCommand(Habit modified)
        {
            this.savedId = getId();
            this.modified = new Habit(modified);
            this.original = new Habit(Habit.this);

            hasIntervalChanged = (this.original.freqDen != this.modified.freqDen ||
                    this.original.freqNum != this.modified.freqNum);
        }

        public void execute()
        {
            Habit habit = Habit.get(savedId);
            habit.copyAttributes(modified);
            habit.save();
            if (hasIntervalChanged)
            {
                habit.deleteCheckmarksNewerThan(0);
                habit.deleteStreaksNewerThan(0);
                habit.deleteScoresNewerThan(0);
            }
        }

        public void undo()
        {
            Habit habit = Habit.get(savedId);
            habit.copyAttributes(original);
            habit.save();
            if (hasIntervalChanged)
            {
                habit.deleteCheckmarksNewerThan(0);
                habit.deleteStreaksNewerThan(0);
                habit.deleteScoresNewerThan(0);
            }
        }

        public Integer getExecuteStringId()
        {
            return R.string.toast_habit_changed;
        }

        public Integer getUndoStringId()
        {
            return R.string.toast_habit_changed_back;
        }
    }

    public class ToggleRepetitionCommand extends Command
    {
        private Long offset;

        public ToggleRepetitionCommand(long offset)
        {
            this.offset = offset;
        }

        @Override
        public void execute()
        {
            toggleRepetition(offset);
        }

        @Override
        public void undo()
        {
            execute();
        }
    }
}
