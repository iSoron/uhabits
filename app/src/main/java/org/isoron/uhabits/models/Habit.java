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

import android.net.*;
import android.support.annotation.*;

import org.apache.commons.lang3.builder.*;

import java.util.*;

import javax.inject.*;

/**
 * The thing that the user wants to track.
 */
public class Habit
{
    public static final String HABIT_URI_FORMAT =
        "content://org.isoron.uhabits/habit/%d";

    @Nullable
    private Long id;

    @NonNull
    private String name;

    @NonNull
    private String description;

    @NonNull
    private Frequency frequency;

    @NonNull
    private Integer color;

    @NonNull
    private boolean archived;

    @NonNull
    private StreakList streaks;

    @NonNull
    private ScoreList scores;

    @NonNull
    private RepetitionList repetitions;

    @NonNull
    private CheckmarkList checkmarks;

    @Nullable
    private Reminder reminder;

    private ModelObservable observable = new ModelObservable();

    /**
     * Constructs a habit with default attributes.
     * <p>
     * The habit is not archived, not highlighted, has no reminders and is
     * placed in the last position of the list of habits.
     */
    @Inject
    Habit(@NonNull ModelFactory factory)
    {
        this.color = 5;
        this.archived = false;
        this.frequency = new Frequency(3, 7);

        checkmarks = factory.buildCheckmarkList(this);
        streaks = factory.buildStreakList(this);
        scores = factory.buildScoreList(this);
        repetitions = factory.buildRepetitionList(this);
    }

    /**
     * Clears the reminder for a habit.
     */
    public void clearReminder()
    {
        reminder = null;
        observable.notifyListeners();
    }

    /**
     * Copies all the attributes of the specified habit into this habit
     *
     * @param model the model whose attributes should be copied from
     */
    public void copyFrom(@NonNull Habit model)
    {
        this.name = model.getName();
        this.description = model.getDescription();
        this.color = model.getColor();
        this.archived = model.isArchived();
        this.frequency = model.frequency;
        this.reminder = model.reminder;
        observable.notifyListeners();
    }

    /**
     * List of checkmarks belonging to this habit.
     */
    @NonNull
    public CheckmarkList getCheckmarks()
    {
        return checkmarks;
    }

    /**
     * Color of the habit.
     * <p>
     * This number is not an android.graphics.Color, but an index to the
     * activity color palette, which changes according to the theme. To convert
     * this color into an android.graphics.Color, use ColorHelper.getColor(context,
     * habit.color).
     */
    @NonNull
    public Integer getColor()
    {
        return color;
    }

    public void setColor(@NonNull Integer color)
    {
        this.color = color;
    }

    @NonNull
    public String getDescription()
    {
        return description;
    }

    public void setDescription(@NonNull String description)
    {
        this.description = description;
    }

    @NonNull
    public Frequency getFrequency()
    {
        return frequency;
    }

    public void setFrequency(@NonNull Frequency frequency)
    {
        this.frequency = frequency;
    }

    @Nullable
    public Long getId()
    {
        return id;
    }

    public void setId(@Nullable Long id)
    {
        this.id = id;
    }

    @NonNull
    public String getName()
    {
        return name;
    }

    public void setName(@NonNull String name)
    {
        this.name = name;
    }

    public ModelObservable getObservable()
    {
        return observable;
    }

    /**
     * Returns the reminder for this habit.
     * <p>
     * Before calling this method, you should call {@link #hasReminder()} to
     * verify that a reminder does exist, otherwise an exception will be
     * thrown.
     *
     * @return the reminder for this habit
     * @throws IllegalStateException if habit has no reminder
     */
    @NonNull
    public Reminder getReminder()
    {
        if (reminder == null) throw new IllegalStateException();
        return reminder;
    }

    public void setReminder(@Nullable Reminder reminder)
    {
        this.reminder = reminder;
    }

    @NonNull
    public RepetitionList getRepetitions()
    {
        return repetitions;
    }

    @NonNull
    public ScoreList getScores()
    {
        return scores;
    }

    @NonNull
    public StreakList getStreaks()
    {
        return streaks;
    }

    /**
     * Returns the public URI that identifies this habit
     *
     * @return the uri
     */
    public Uri getUri()
    {
        String s = String.format(Locale.US, HABIT_URI_FORMAT, getId());
        return Uri.parse(s);
    }

    /**
     * Returns whether the habit has a reminder.
     *
     * @return true if habit has reminder, false otherwise
     */
    public boolean hasReminder()
    {
        return reminder != null;
    }

    public boolean isArchived()
    {
        return archived;
    }

    public void setArchived(boolean archived)
    {
        this.archived = archived;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this)
            .append("id", id)
            .append("name", name)
            .append("description", description)
            .append("color", color)
            .append("archived", archived)
            .toString();
    }
}
