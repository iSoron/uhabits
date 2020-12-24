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

package org.isoron.uhabits.core.models;

import androidx.annotation.*;

import org.apache.commons.lang3.builder.*;

import java.util.*;

import javax.annotation.concurrent.*;
import javax.inject.*;

import static org.isoron.uhabits.core.models.Entry.*;
import static org.isoron.uhabits.core.utils.StringUtils.defaultToStringStyle;

/**
 * The thing that the user wants to track.
 */
@ThreadSafe
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
    private EntryList computedEntries;

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
        computedEntries = factory.buildEntryList(this);
        streaks = factory.buildStreakList(this);
        scores = factory.buildScoreList(this);
        repetitions = factory.buildRepetitionList(this);
    }

    Habit(@NonNull ModelFactory factory, @NonNull HabitData data)
    {
        this.data = new HabitData(data);
        computedEntries = factory.buildEntryList(this);
        streaks = factory.buildStreakList(this);
        scores = factory.buildScoreList(this);
        repetitions = factory.buildRepetitionList(this);
        observable = new ModelObservable();
    }

    /**
     * Clears the reminder for a habit.
     */
    public synchronized void clearReminder()
    {
        data.reminder = null;
        observable.notifyListeners();
    }

    /**
     * Copies all the attributes of the specified habit into this habit
     *
     * @param model the model whose attributes should be copied from
     */
    public synchronized void copyFrom(@NonNull Habit model)
    {
        this.data = new HabitData(model.data);
        observable.notifyListeners();
    }

    @NonNull
    public synchronized EntryList getComputedEntries()
    {
        return computedEntries;
    }

    @NonNull
    public synchronized PaletteColor getColor()
    {
        return data.color;
    }

    public synchronized void setColor(@NonNull PaletteColor color)
    {
        data.color = color;
    }

    @NonNull
    public synchronized String getDescription()
    {
        return data.description;
    }

    public synchronized void setDescription(@NonNull String description)
    {
        data.description = description;
    }

    @NonNull
    public synchronized Frequency getFrequency()
    {
        return data.frequency;
    }

    public synchronized void setFrequency(@NonNull Frequency frequency)
    {
        data.frequency = frequency;
        invalidateNewerThan(Timestamp.ZERO);
    }

    @Nullable
    public synchronized Long getId()
    {
        return id;
    }

    public synchronized void setId(@Nullable Long id)
    {
        this.id = id;
    }

    @NonNull
    public synchronized String getName()
    {
        return data.name;
    }

    public synchronized void setName(@NonNull String name)
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
    public synchronized Reminder getReminder()
    {
        if (data.reminder == null) throw new IllegalStateException();
        return data.reminder;
    }

    public synchronized void setReminder(@Nullable Reminder reminder)
    {
        data.reminder = reminder;
    }

    public RepetitionList getOriginalEntries()
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

    public synchronized int getTargetType()
    {
        return data.targetType;
    }

    public synchronized void setTargetType(int targetType)
    {
        if (targetType != AT_LEAST && targetType != AT_MOST)
            throw new IllegalArgumentException(
                String.format("invalid targetType: %d", targetType));
        data.targetType = targetType;
    }

    public synchronized double getTargetValue()
    {
        return data.targetValue;
    }

    public synchronized void setTargetValue(double targetValue)
    {
        if (targetValue < 0) throw new IllegalArgumentException();
        data.targetValue = targetValue;
    }

    public synchronized int getType()
    {
        return data.type;
    }

    public synchronized void setType(int type)
    {
        if (type != YES_NO_HABIT && type != NUMBER_HABIT)
            throw new IllegalArgumentException();

        data.type = type;
    }

    @NonNull
    public synchronized String getUnit()
    {
        return data.unit;
    }

    public synchronized void setUnit(@NonNull String unit)
    {
        data.unit = unit;
    }

    /**
     * Returns the public URI that identifies this habit
     *
     * @return the URI
     */
    public String getUriString()
    {
        return String.format(Locale.US, HABIT_URI_FORMAT, getId());
    }

    public synchronized boolean hasId()
    {
        return getId() != null;
    }

    /**
     * Returns whether the habit has a reminder.
     *
     * @return true if habit has reminder, false otherwise
     */
    public synchronized boolean hasReminder()
    {
        return data.reminder != null;
    }

    public void invalidateNewerThan(Timestamp timestamp)
    {
        getScores().invalidateNewerThan(timestamp);
        getComputedEntries().invalidateNewerThan(timestamp);
        getStreaks().invalidateNewerThan(timestamp);
    }

    public synchronized boolean isArchived()
    {
        return data.archived;
    }

    public synchronized void setArchived(boolean archived)
    {
        data.archived = archived;
    }

    public synchronized boolean isCompletedToday()
    {
        int todayCheckmark = getComputedEntries().getTodayValue();
        if (isNumerical())
        {
            if(getTargetType() == AT_LEAST)
                return todayCheckmark / 1000.0 >= data.targetValue;
            else
                return todayCheckmark / 1000.0 <= data.targetValue;
        }
        else return (todayCheckmark != NO && todayCheckmark != UNKNOWN);
    }

    public synchronized boolean isNumerical()
    {
        return data.type == NUMBER_HABIT;
    }

    public HabitData getData()
    {
        return new HabitData(data);
    }

    public Integer getPosition()
    {
        return data.position;
    }

    public void setPosition(int newPosition)
    {
        data.position = newPosition;
    }

    @NonNull
    public String getQuestion()
    {
        return data.question;
    }

    public void setQuestion(@NonNull String question)
    {
        data.question = question;
    }

    @NonNull
    public String getUUID()
    {
        return data.uuid;
    }

    public void setUUID(@NonNull String uuid)
    {
        data.uuid = uuid;
    }

    public static final class HabitData
    {
        @NonNull
        public String name;

        @NonNull
        public String description;

        @NonNull
        public String question;

        @NonNull
        public Frequency frequency;

        public PaletteColor color;

        public boolean archived;

        public int targetType;

        public double targetValue;

        public int type;

        public String uuid;

        @NonNull
        public String unit;

        @Nullable
        public Reminder reminder;

        public int position;

        public HabitData()
        {
            this.color = new PaletteColor(8);
            this.archived = false;
            this.frequency = new Frequency(3, 7);
            this.type = YES_NO_HABIT;
            this.name = "";
            this.description = "";
            this.question = "";
            this.targetType = AT_LEAST;
            this.targetValue = 100;
            this.unit = "";
            this.position = 0;
            this.uuid = UUID.randomUUID().toString().replace("-", "");
        }

        public HabitData(@NonNull HabitData model)
        {
            this.name = model.name;
            this.description = model.description;
            this.question = model.question;
            this.frequency = model.frequency;
            this.color = model.color;
            this.archived = model.archived;
            this.targetType = model.targetType;
            this.targetValue = model.targetValue;
            this.type = model.type;
            this.unit = model.unit;
            this.reminder = model.reminder;
            this.position = model.position;
            this.uuid = model.uuid;
        }

        @Override
        public String toString()
        {
            return new ToStringBuilder(this, defaultToStringStyle())
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
                .append("position", position)
                .append("question", question)
                .append("uuid", uuid)
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
                .append(position, habitData.position)
                .append(question, habitData.question)
                .append(uuid, habitData.uuid)
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
                .append(position)
                .append(question)
                .append(uuid)
                .toHashCode();
        }
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, defaultToStringStyle())
            .append("id", id)
            .append("data", data)
            .toString();
    }
}
