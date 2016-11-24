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

import android.support.annotation.*;

import com.opencsv.*;

import org.isoron.uhabits.utils.*;

import java.io.*;
import java.util.*;

/**
 * An ordered collection of {@link Habit}s.
 */
public abstract class HabitList implements Iterable<Habit>
{
    private ModelObservable observable;

    @NonNull
    protected final HabitMatcher filter;

    /**
     * Creates a new HabitList.
     * <p>
     * Depending on the implementation, this list can either be empty or be
     * populated by some pre-existing habits, for example, from a certain
     * database.
     */
    public HabitList()
    {
        observable = new ModelObservable();
        filter = new HabitMatcherBuilder().setArchivedAllowed(true).build();
    }

    protected HabitList(@NonNull HabitMatcher filter)
    {
        observable = new ModelObservable();
        this.filter = filter;
    }

    /**
     * Inserts a new habit in the list.
     * <p>
     * If the id of the habit is null, the list will assign it a new id, which
     * is guaranteed to be unique in the scope of the list. If id is not null,
     * the caller should make sure that the list does not already contain
     * another habit with same id, otherwise a RuntimeException will be thrown.
     *
     * @param habit the habit to be inserted
     * @throws IllegalArgumentException if the habit is already on the list.
     */
    public abstract void add(@NonNull Habit habit)
        throws IllegalArgumentException;

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
     * @return the habit at that position
     * @throws IndexOutOfBoundsException when the position is invalid
     */
    @NonNull
    public abstract Habit getByPosition(int position);

    /**
     * Returns the list of habits that match a given condition.
     *
     * @param matcher the matcher that checks the condition
     * @return the list of matching habits
     */
    @NonNull
    public abstract HabitList getFiltered(HabitMatcher matcher);

    public ModelObservable getObservable()
    {
        return observable;
    }

    public abstract Order getOrder();

    /**
     * Changes the order of the elements on the list.
     *
     * @param order the new order criterea
     */
    public abstract void setOrder(@NonNull Order order);

    /**
     * Returns the index of the given habit in the list, or -1 if the list does
     * not contain the habit.
     *
     * @param h the habit
     * @return the index of the habit, or -1 if not in the list
     */
    public abstract int indexOf(@NonNull Habit h);

    public boolean isEmpty()
    {
        return size() == 0;
    }

    /**
     * Removes the given habit from the list.
     * <p>
     * If the given habit is not in the list, does nothing.
     *
     * @param h the habit to be removed.
     */
    public abstract void remove(@NonNull Habit h);

    /**
     * Removes all the habits from the list.
     */
    public void removeAll()
    {
        List<Habit> copy = new LinkedList<>();
        for (Habit h : this) copy.add(h);
        for (Habit h : copy) remove(h);
    }

    /**
     * Changes the position of a habit in the list.
     *
     * @param from the habit that should be moved
     * @param to   the habit that currently occupies the desired position
     */
    public abstract void reorder(Habit from, Habit to);

    public void repair()
    {
        for (Habit h : this)
        {
            h.getCheckmarks().invalidateNewerThan(0);
            h.getStreaks().invalidateNewerThan(0);
            h.getScores().invalidateNewerThan(0);
        }
    }

    /**
     * Returns the number of habits in this list.
     *
     * @return number of habits
     */
    public abstract int size();

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

        for (Habit habit : this)
        {
            Frequency freq = habit.getFrequency();

            String[] cols = {
                String.format("%03d", indexOf(habit) + 1),
                habit.getName(),
                habit.getDescription(),
                Integer.toString(freq.getNumerator()),
                Integer.toString(freq.getDenominator()),
                ColorUtils.CSV_PALETTE[habit.getColor()]
            };

            csv.writeNext(cols, false);
        }

        csv.close();
    }

    public enum Order
    {
        BY_NAME,
        BY_COLOR,
        BY_SCORE,
        BY_POSITION
    }
}
