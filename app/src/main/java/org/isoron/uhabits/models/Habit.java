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

import android.annotation.SuppressLint;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Delete;
import com.activeandroid.query.From;
import com.activeandroid.query.Select;
import com.activeandroid.query.Update;
import com.activeandroid.util.SQLiteUtils;
import com.opencsv.CSVWriter;

import org.isoron.uhabits.utils.ColorUtils;
import org.isoron.uhabits.utils.DateUtils;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Locale;

@Table(name = "Habits")
public class Habit extends Model
{
    /**
     * Name of the habit
     */
    @Column(name = "name")
    public String name;

    /**
     * Description of the habit
     */
    @Column(name = "description")
    public String description;

    /**
     * Frequency numerator. If a habit is performed 3 times in 7 days, this field equals 3.
     */
    @Column(name = "freq_num")
    public Integer freqNum;

    /**
     * Frequency denominator. If a habit is performed 3 times in 7 days, this field equals 7.
     */
    @Column(name = "freq_den")
    public Integer freqDen;

    /**
     * Color of the habit.
     *
     * This number is not an android.graphics.Color, but an index to the activity color palette,
     * which changes according to the theme. To convert this color into an android.graphics.Color,
     * use ColorHelper.getColor(context, habit.color).
     */
    @Column(name = "color")
    public Integer color;

    /**
     * Position of the habit. Habits are usually sorted by this field.
     */
    @Column(name = "position")
    public Integer position;

    /**
     * Hour of the day the reminder should be shown. If there is no reminder, this equals to null.
     */
    @Nullable
    @Column(name = "reminder_hour")
    public Integer reminderHour;

    /**
     * Minute the reminder should be shown. If there is no reminder, this equals to null.
     */
    @Nullable
    @Column(name = "reminder_min")
    public Integer reminderMin;

    /**
     * Days of the week the reminder should be shown. This field can be converted to a list of
     * booleans using the method DateHelper.unpackWeekdayList and converted back to an integer by
     * using the method DateHelper.packWeekdayList. If the habit has no reminders, this value
     * should be ignored.
     */
    @NonNull
    @Column(name = "reminder_days")
    public Integer reminderDays;

    /**
     * Not currently used.
     */
    @Column(name = "highlight")
    public Integer highlight;

    /**
     * Flag that indicates whether the habit is archived. Archived habits are usually omitted from
     * listings, unless explicitly included.
     */
    @Column(name = "archived")
    public Integer archived;

    /**
     * List of streaks belonging to this habit.
     */
    @NonNull
    public StreakList streaks;

    /**
     * List of scores belonging to this habit.
     */
    @NonNull
    public ScoreList scores;

    /**
     * List of repetitions belonging to this habit.
     */
    @NonNull
    public RepetitionList repetitions;

    /**
     * List of checkmarks belonging to this habit.
     */
    @NonNull
    public CheckmarkList checkmarks;

    /**
     * Constructs a habit with the same attributes as the specified habit.
     *
     * @param model the model whose attributes should be copied from
     */
    public Habit(Habit model)
    {
        reminderDays = DateUtils.ALL_WEEK_DAYS;

        copyAttributes(model);

        checkmarks = new CheckmarkList(this);
        streaks = new StreakList(this);
        scores = new ScoreList(this);
        repetitions = new RepetitionList(this);
    }

    /**
     * Constructs a habit with default attributes. The habit is not archived, not highlighted, has
     * no reminders and is placed in the last position of the list of habits.
     */
    public Habit()
    {
        this.color = 5;
        this.position = Habit.countWithArchived();
        this.highlight = 0;
        this.archived = 0;
        this.freqDen = 7;
        this.freqNum = 3;
        this.reminderDays = DateUtils.ALL_WEEK_DAYS;

        checkmarks = new CheckmarkList(this);
        streaks = new StreakList(this);
        scores = new ScoreList(this);
        repetitions = new RepetitionList(this);
    }

    /**
     * Returns the habit with specified id.
     *
     * @param id the id of the habit
     * @return the habit, or null if none exist
     */
    @Nullable
    public static Habit get(long id)
    {
        return Habit.load(Habit.class, id);
    }

    /**
     * Returns a list of all habits, optionally including archived habits.
     *
     * @param includeArchive whether archived habits should be included the list
     * @return list of all habits
     */
    @NonNull
    public static List<Habit> getAll(boolean includeArchive)
    {
        if(includeArchive) return selectWithArchived().execute();
        else return select().execute();
    }

    /**
     * Returns the habit that occupies a certain position.
     *
     * @param position the position of the desired habit
     * @return the habit at that position, or null if there is none
     */
    @Nullable
    public static Habit getByPosition(int position)
    {
        return selectWithArchived().where("position = ?", position).executeSingle();
    }

    /**
     * Changes the id of a habit on the database.
     *
     * @param oldId the original id
     * @param newId the new id
     */
    @SuppressLint("DefaultLocale")
    public static void updateId(long oldId, long newId)
    {
        SQLiteUtils.execSql(String.format("update Habits set Id = %d where Id = %d", newId, oldId));
    }

    @NonNull
    protected static From select()
    {
        return new Select().from(Habit.class).where("archived = 0").orderBy("position");
    }

    @NonNull
    protected static From selectWithArchived()
    {
        return new Select().from(Habit.class).orderBy("position");
    }

    /**
     * Returns the total number of unarchived habits.
     *
     * @return number of unarchived habits
     */
    public static int count()
    {
        return select().count();
    }

    /**
     * Returns the total number of habits, including archived habits.
     *
     * @return number of habits, including archived
     */
    public static int countWithArchived()
    {
        return selectWithArchived().count();
    }

    /**
     * Returns a list the habits that have a reminder. Does not include archived habits.
     *
     * @return list of habits with reminder
     */
    @NonNull
    public static List<Habit> getHabitsWithReminder()
    {
        return select().where("reminder_hour is not null").execute();
    }

    /**
     * Changes the position of a habit on the list.
     *
     * @param from the habit that should be moved
     * @param to the habit that currently occupies the desired position
     */
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

    /**
     * Recomputes the position for every habit in the database. It should never be necessary
     * to call this method.
     */
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

    /**
     * Copies all the attributes of the specified habit into this habit
     *
     * @param model the model whose attributes should be copied from
     */
    public void copyAttributes(@NonNull Habit model)
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

    /**
     * Saves the habit on the database, and assigns the specified id to it.
     *
     * @param id the id that the habit should receive
     */
    public void save(long id)
    {
        save();
        Habit.updateId(getId(), id);
    }

    /**
     * Deletes the habit and all data associated to it, including checkmarks, repetitions and
     * scores.
     */
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

    /**
     * Returns the public URI that identifies this habit
     * @return the uri
     */
    public Uri getUri()
    {
        String s = String.format(Locale.US, "content://org.isoron.uhabits/habit/%d", getId());
        return Uri.parse(s);
    }

    /**
     * Returns whether the habit is archived or not.
     * @return true if archived
     */
    public boolean isArchived()
    {
        return archived != 0;
    }

    private static void updateAttributes(@NonNull List<Habit> habits, @Nullable Integer color,
                                         @Nullable Integer archived)
    {
        ActiveAndroid.beginTransaction();

        try
        {
            for (Habit h : habits)
            {
                if(color != null) h.color = color;
                if(archived != null) h.archived = archived;
                h.save();
            }

            ActiveAndroid.setTransactionSuccessful();
        }
        finally
        {
            ActiveAndroid.endTransaction();
        }
    }

    /**
     * Archives an entire list of habits
     *
     * @param habits the habits to be archived
     */
    public static void archive(@NonNull List<Habit> habits)
    {
        updateAttributes(habits, null, 1);
    }

    /**
     * Unarchives an entire list of habits
     *
     * @param habits the habits to be unarchived
     */
    public static void unarchive(@NonNull List<Habit> habits)
    {
        updateAttributes(habits, null, 0);
    }

    /**
     * Sets the color for an entire list of habits.
     *
     * @param habits the habits to be modified
     * @param color the new color to be set
     */
    public static void setColor(@NonNull List<Habit> habits, int color)
    {
        updateAttributes(habits, color, null);
    }

    /**
     * Checks whether the habit has a reminder set.
     *
     * @return true if habit has reminder
     */
    public boolean hasReminder()
    {
        return (reminderHour != null && reminderMin != null);
    }

    /**
     * Clears the reminder for a habit. This sets all the related fields to null.
     */
    public void clearReminder()
    {
        reminderHour = null;
        reminderMin = null;
        reminderDays = DateUtils.ALL_WEEK_DAYS;
    }

    /**
     * Writes the list of habits to the given writer, in CSV format. There is one line for each
     * habit, containing the fields name, description, frequency numerator, frequency denominator
     * and color. The color is written in HTML format (#000000).
     *
     * @param habits the list of habits to write
     * @param out the writer that will receive the result
     * @throws IOException if write operations fail
     */
    public static void writeCSV(List<Habit> habits, Writer out) throws IOException
    {
        String header[] = { "Name", "Description", "NumRepetitions", "Interval", "Color" };

        CSVWriter csv = new CSVWriter(out);
        csv.writeNext(header, false);

        for(Habit habit : habits)
        {
            String[] cols =
            {
                    habit.name,
                    habit.description,
                    Integer.toString(habit.freqNum),
                    Integer.toString(habit.freqDen),
                    ColorUtils.toHTML(ColorUtils.CSV_PALETTE[habit.color])
            };

            csv.writeNext(cols, false);
        }

        csv.close();
    }
}
