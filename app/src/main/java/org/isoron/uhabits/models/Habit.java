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
import android.net.Uri;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Delete;
import com.activeandroid.query.From;
import com.activeandroid.query.Select;
import com.activeandroid.query.Update;
import com.activeandroid.util.SQLiteUtils;

import org.isoron.helpers.ColorHelper;

import java.util.List;

@Table(name = "Habits")
public class Habit extends Model
{
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

    public StreakList streaks;
    public ScoreList scores;
    public RepetitionList repetitions;
    public CheckmarkList checkmarks;

    public Habit(Habit model)
    {
        copyAttributes(model);
        initializeLists();
    }

    public Habit()
    {
        this.color = ColorHelper.palette[5];
        this.position = Habit.countWithArchived();
        this.highlight = 0;
        this.archived = 0;
        this.freqDen = 7;
        this.freqNum = 3;
        this.reminderDays = 127;
        initializeLists();
    }

    private void initializeLists()
    {
        streaks = new StreakList(this);
        scores = new ScoreList(this);
        repetitions = new RepetitionList(this);
        checkmarks = new CheckmarkList(this);
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

    public static int count()
    {
        return select().count();
    }

    public static int countWithArchived()
    {
        return selectWithArchived().count();
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
        }
        finally
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

    public Uri getUri()
    {
        return Uri.parse(String.format("content://org.isoron.uhabits/habit/%d", getId()));
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
}
