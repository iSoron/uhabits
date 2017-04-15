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

import static android.R.attr.*;
import static org.isoron.uhabits.models.Checkmark.*;

/**
 * The thing that the user wants to track.
 */
public class Habit
{
    public static final int AT_LEAST = 0;

    public static final int AT_MOST = 1;

    public static final String HABIT_URI_FORMAT =
        "content://org.isoron.uhabits/habit/%d";

    public static final int NUMBER_HABIT = 1;

    public static final int YES_NO_HABIT = 0;

    @Nullable
    public Long id;

    @NonNull
    private HabitData data;

    @NonNull
    private StreakList streaks;

    @NonNull
    private ScoreList scores;

    @NonNull
    private RepetitionList repetitions;

    @NonNull
    private CheckmarkList checkmarks;

    private ModelObservable observable = new ModelObservable();

    /**
     * Constructs a habit with default data.
     * <p>
     * The habit is not archived, not highlighted, has no reminders and is
     * placed in the last position of the list of habits.
     */
    @Inject
    Habit(@NonNull ModelFactory factory)
    {
        this.data = new HabitData();
        checkmarks = factory.buildCheckmarkList(this);
        streaks = factory.buildStreakList(this);
        scores = factory.buildScoreList(this);
        repetitions = factory.buildRepetitionList(this);
    }

    Habit(@NonNull ModelFactory factory, @NonNull HabitData data)
    {
        this.data = new HabitData(data);
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
        data.reminder = null;
        observable.notifyListeners();
    }

    /**
     * Copies all the attributes of the specified habit into this habit
     *
     * @param model the model whose attributes should be copied from
     */
    public void copyFrom(@NonNull Habit model)
    {
        this.data = new HabitData(model.data);
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
        return data.color;
    }

    public void setColor(@NonNull Integer color)
    {
        data.color = color;
    }

    @NonNull
    public String getDescription()
    {
        return data.description;
    }

    public void setDescription(@NonNull String description)
    {
        data.description = description;
    }

    @NonNull
    public Frequency getFrequency()
    {
        return data.frequency;
    }

    public void setFrequency(@NonNull Frequency frequency)
    {
        data.frequency = frequency;
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
        return data.name;
    }

    public void setName(@NonNull String name)
    {
        data.name = name;
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
        if (data.reminder == null) throw new IllegalStateException();
        return data.reminder;
    }

    public void setReminder(@Nullable Reminder reminder)
    {
        data.reminder = reminder;
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

    public int getTargetType()
    {
        return data.targetType;
    }

    public void setTargetType(int targetType)
    {
        if (targetType != AT_LEAST && targetType != AT_MOST)
            throw new IllegalArgumentException();
        data.targetType = targetType;
    }

    public double getTargetValue()
    {
        return data.targetValue;
    }

    public void setTargetValue(double targetValue)
    {
        if (targetValue < 0) throw new IllegalArgumentException();
        data.targetValue = targetValue;
    }

    public int getType()
    {
        return data.type;
    }

    public void setType(int type)
    {
        if (type != YES_NO_HABIT && type != NUMBER_HABIT)
            throw new IllegalArgumentException();

        data.type = type;
    }

    @NonNull
    public String getUnit()
    {
        return data.unit;
    }

    public void setUnit(@NonNull String unit)
    {
        data.unit = unit;
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

    public boolean hasId()
    {
        return getId() != null;
    }

    /**
     * Returns whether the habit has a reminder.
     *
     * @return true if habit has reminder, false otherwise
     */
    public boolean hasReminder()
    {
        return data.reminder != null;
    }

    public void invalidateNewerThan(long timestamp)
    {
        getScores().invalidateNewerThan(timestamp);
        getCheckmarks().invalidateNewerThan(timestamp);
        getStreaks().invalidateNewerThan(timestamp);
    }

    public boolean isArchived()
    {
        return data.archived;
    }

    public void setArchived(boolean archived)
    {
        data.archived = archived;
    }

    public boolean isCompletedToday()
    {
        int todayCheckmark = getCheckmarks().getTodayValue();
        if (isNumerical()) return todayCheckmark >= data.targetValue;
        else return (todayCheckmark != UNCHECKED);
    }

    public boolean isNumerical()
    {
        return type == NUMBER_HABIT;
    }

    public HabitData getData()
    {
        return new HabitData(data);
    }

    public static class HabitData
    {
        @NonNull
        public String name;

        @NonNull
        public String description;

        @NonNull
        public Frequency frequency;

        public int color;

        public boolean archived;

        public int targetType;

        public double targetValue;

        public int type;

        @NonNull
        public String unit;

        @Nullable
        public Reminder reminder;

        public HabitData()
        {
            this.color = 5;
            this.archived = false;
            this.frequency = new Frequency(3, 7);
            this.type = YES_NO_HABIT;
            this.name = "";
            this.description = "";
            this.targetType = AT_LEAST;
            this.targetValue = 100;
            this.unit = "";
        }

        public HabitData(@NonNull HabitData model)
        {
            this.name = model.name;
            this.description = model.description;
            this.frequency = model.frequency;
            this.color = model.color;
            this.archived = model.archived;
            this.targetType = model.targetType;
            this.targetValue = model.targetValue;
            this.type = model.type;
            this.unit = model.unit;
            this.reminder = model.reminder;
        }

        @Override
        public String toString()
        {
            return new ToStringBuilder(this)
                .append("name", name)
                .append("description", description)
                .append("frequency", frequency)
                .append("color", color)
                .append("archived", archived)
                .append("targetType", targetType)
                .append("targetValue", targetValue)
                .append("type", type)
                .append("unit", unit)
                .append("reminder", reminder)
                .toString();
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) return true;

            if (o == null || getClass() != o.getClass()) return false;

            HabitData habitData = (HabitData) o;

            return new EqualsBuilder()
                .append(color, habitData.color)
                .append(archived, habitData.archived)
                .append(targetType, habitData.targetType)
                .append(targetValue, habitData.targetValue)
                .append(type, habitData.type)
                .append(name, habitData.name)
                .append(description, habitData.description)
                .append(frequency, habitData.frequency)
                .append(unit, habitData.unit)
                .append(reminder, habitData.reminder)
                .isEquals();
        }

        @Override
        public int hashCode()
        {
            return new HashCodeBuilder(17, 37)
                .append(name)
                .append(description)
                .append(frequency)
                .append(color)
                .append(archived)
                .append(targetType)
                .append(targetValue)
                .append(type)
                .append(unit)
                .append(reminder)
                .toHashCode();
        }
    }
}
