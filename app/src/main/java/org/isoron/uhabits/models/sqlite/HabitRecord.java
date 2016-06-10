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

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Delete;
import com.activeandroid.util.SQLiteUtils;

import org.isoron.uhabits.models.Habit;
import org.isoron.uhabits.utils.DatabaseUtils;

/**
 * The SQLite database record corresponding to a {@link Habit}.
 */
@Table(name = "Habits")
public class HabitRecord extends Model
{
    public static final String HABIT_URI_FORMAT =
        "content://org.isoron.uhabits/habit/%d";

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

    @Nullable
    @Column(name = "reminder_hour")
    public Integer reminderHour;

    @Nullable
    @Column(name = "reminder_min")
    public Integer reminderMin;

    @NonNull
    @Column(name = "reminder_days")
    public Integer reminderDays;

    @Column(name = "highlight")
    public Integer highlight;

    @Column(name = "archived")
    public Integer archived;

    public HabitRecord()
    {

    }

    @Nullable
    public static HabitRecord get(Long id)
    {
        return HabitRecord.load(HabitRecord.class, id);
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
        SQLiteUtils.execSql(
            String.format("update Habits set Id = %d where Id = %d", newId,
                oldId));
    }

    /**
     * Deletes the habit and all data associated to it, including checkmarks,
     * repetitions and scores.
     */
    public void cascadeDelete()
    {
        Long id = getId();

        DatabaseUtils.executeAsTransaction(() -> {
            new Delete()
                .from(CheckmarkRecord.class)
                .where("habit = ?", id)
                .execute();

            new Delete()
                .from(RepetitionRecord.class)
                .where("habit = ?", id)
                .execute();

            new Delete()
                .from(ScoreRecord.class)
                .where("habit = ?", id)
                .execute();

            new Delete()
                .from(StreakRecord.class)
                .where("habit = ?", id)
                .execute();

            delete();
        });
    }

    public void copyFrom(Habit model)
    {
        this.name = model.getName();
        this.description = model.getDescription();
        this.freqNum = model.getFreqNum();
        this.freqDen = model.getFreqDen();
        this.color = model.getColor();
        this.reminderHour = model.getReminderHour();
        this.reminderMin = model.getReminderMin();
        this.reminderDays = model.getReminderDays();
        this.highlight = model.getHighlight();
        this.archived = model.getArchived();
    }

    public void copyTo(Habit habit)
    {
        habit.setName(this.name);
        habit.setDescription(this.description);
        habit.setFreqNum(this.freqNum);
        habit.setFreqDen(this.freqDen);
        habit.setColor(this.color);
        habit.setReminderHour(this.reminderHour);
        habit.setReminderMin(this.reminderMin);
        habit.setReminderDays(this.reminderDays);
        habit.setHighlight(this.highlight);
        habit.setArchived(this.archived);
        habit.setId(this.getId());
    }

    /**
     * Saves the habit on the database, and assigns the specified id to it.
     *
     * @param id the id that the habit should receive
     */
    public void save(long id)
    {
        save();
        updateId(getId(), id);
    }
}
