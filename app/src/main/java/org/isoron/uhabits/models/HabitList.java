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

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.opencsv.CSVWriter;

import org.isoron.uhabits.utils.ColorUtils;

import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * An ordered collection of {@link Habit}s.
 */
public abstract class HabitList
{
    private ModelObservable observable;

    /**
     * Creates a new HabitList.
     * <p>
     * Depending on the implementation, this list can either be empty or be
     * populated by some pre-existing habits.
     */
    public HabitList()
    {
        observable = new ModelObservable();
    }

    /**
     * Inserts a new habit in the list.
     *
     * @param habit the habit to be inserted
     */
    public abstract void add(@NonNull Habit habit);

    /**
     * Returns the total number of unarchived habits.
     *
     * @return number of unarchived habits
     */
    public abstract int count();

    /**
     * Returns the total number of habits, including archived habits.
     *
     * @return number of habits, including archived
     */
    public abstract int countWithArchived();

    /**
     * Returns a list of all habits, optionally including archived habits.
     *
     * @param includeArchive whether archived habits should be included the
     *                       list
     * @return list of all habits
     */
    @NonNull
    public abstract List<Habit> getAll(boolean includeArchive);

    /**
     * Returns the habit with specified id.
     *
     * @param id the id of the habit
     * @return the habit, or null if none exist
     */
    @Nullable
    public abstract Habit getById(long id);

    /**
     * Returns the habit that occupies a certain position.
     *
     * @param position the position of the desired habit
     * @return the habit at that position, or null if there is none
     */
    @Nullable
    public abstract Habit getByPosition(int position);

    /**
     * Returns the list of habits that match a given condition.
     *
     * @param matcher the matcher that checks the condition
     * @return the list of matching habits
     */
    @NonNull
    public List<Habit> getFiltered(HabitMatcher matcher)
    {
        LinkedList<Habit> habits = new LinkedList<>();
        for (Habit h : getAll(true)) if (matcher.matches(h)) habits.add(h);
        return habits;
    }

    public ModelObservable getObservable()
    {
        return observable;
    }

    /**
     * Returns a list the habits that have a reminder. Does not include archived
     * habits.
     *
     * @return list of habits with reminder
     */
    @NonNull
    public List<Habit> getWithReminder()
    {
        return getFiltered(habit -> habit.hasReminder());
    }

    /**
     * Returns the index of the given habit in the list, or -1 if the list does
     * not contain the habit.
     *
     * @param h the habit
     * @return the index of the habit, or -1 if not in the list
     */
    public abstract int indexOf(@NonNull Habit h);

    /**
     * Removes the given habit from the list.
     * <p>
     * If the given habit is not in the list, does nothing.
     *
     * @param h the habit to be removed.
     */
    public abstract void remove(@NonNull Habit h);

    /**
     * Changes the position of a habit in the list.
     *
     * @param from the habit that should be moved
     * @param to   the habit that currently occupies the desired position
     */
    public abstract void reorder(Habit from, Habit to);

    /**
     * Notifies the list that a certain list of habits has been modified.
     * <p>
     * Depending on the implementation, this operation might trigger a write to
     * disk, or do nothing at all. To make sure that the habits get persisted,
     * this operation must be called.
     *
     * @param habits the list of habits that have been modified.
     */
    public abstract void update(List<Habit> habits);

    /**
     * Notifies the list that a certain habit has been modified.
     * <p>
     * See {@link #update(List)} for more details.
     *
     * @param habit the habit that has been modified.
     */
    public void update(@NonNull Habit habit)
    {
        update(Collections.singletonList(habit));
    }

    /**
     * Writes the list of habits to the given writer, in CSV format. There is
     * one line for each habit, containing the fields name, description,
     * frequency numerator, frequency denominator and color. The color is
     * written in HTML format (#000000).
     *
     * @param out the writer that will receive the result
     * @throws IOException if write operations fail
     */
    public void writeCSV(@NonNull Writer out) throws IOException
    {
        String header[] = {
            "Position",
            "Name",
            "Description",
            "NumRepetitions",
            "Interval",
            "Color"
        };

        CSVWriter csv = new CSVWriter(out);
        csv.writeNext(header, false);

        for (Habit habit : getAll(true))
        {
            String[] cols = {
                String.format("%03d", indexOf(habit) + 1),
                habit.getName(),
                habit.getDescription(),
                Integer.toString(habit.getFreqNum()),
                Integer.toString(habit.getFreqDen()),
                ColorUtils.CSV_PALETTE[habit.getColor()]
            };

            csv.writeNext(cols, false);
        }

        csv.close();
    }

    /**
     * A HabitMatcher decides whether habits match or not a certain condition.
     * They can be used to produce filtered lists of habits.
     */
    public interface HabitMatcher
    {
        /**
         * Returns true if the given habit matches.
         *
         * @param habit the habit to be checked.
         * @return true if matches, false otherwise.
         */
        boolean matches(Habit habit);
    }
}
