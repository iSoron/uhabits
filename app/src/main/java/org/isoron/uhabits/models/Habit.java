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

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.isoron.uhabits.HabitsApplication;
import org.isoron.uhabits.utils.DateUtils;

import java.util.Locale;

import javax.inject.Inject;

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
    private Integer freqNum;

    @NonNull
    private Integer freqDen;

    @NonNull
    private Integer color;

    @Nullable
    private Integer reminderHour;

    @Nullable
    private Integer reminderMin;

    @NonNull
    private Integer reminderDays;

    @NonNull
    private Integer highlight;

    @NonNull
    private Integer archived;

    @NonNull
    private StreakList streaks;

    @NonNull
    private ScoreList scores;

    @NonNull
    private RepetitionList repetitions;

    @NonNull
    private CheckmarkList checkmarks;

    private ModelObservable observable = new ModelObservable();

    @Inject
    ModelFactory factory;

    /**
     * Constructs a habit with the same attributes as the specified habit.
     *
     * @param model the model whose attributes should be copied from
     */
    public Habit(Habit model)
    {
        HabitsApplication.getComponent().inject(this);

        reminderDays = DateUtils.ALL_WEEK_DAYS;

        copyFrom(model);

        checkmarks = factory.buildCheckmarkList(this);
        streaks = factory.buildStreakList(this);
        scores = factory.buildScoreList(this);
        repetitions = factory.buidRepetitionList(this);
    }

    /**
     * Constructs a habit with default attributes.
     * <p>
     * The habit is not archived, not highlighted, has no reminders and is
     * placed in the last position of the list of habits.
     */
    public Habit()
    {
        HabitsApplication.getComponent().inject(this);

        this.color = 5;
        this.highlight = 0;
        this.archived = 0;
        this.freqDen = 7;
        this.freqNum = 3;
        this.reminderDays = DateUtils.ALL_WEEK_DAYS;

        checkmarks = factory.buildCheckmarkList(this);
        streaks = factory.buildStreakList(this);
        scores = factory.buildScoreList(this);
        repetitions = factory.buidRepetitionList(this);
    }

    /**
     * Clears the reminder for a habit. This sets all the related fields to
     * null.
     */
    public void clearReminder()
    {
        reminderHour = null;
        reminderMin = null;
        reminderDays = DateUtils.ALL_WEEK_DAYS;
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
        this.freqNum = model.getFreqNum();
        this.freqDen = model.getFreqDen();
        this.color = model.getColor();
        this.reminderHour = model.getReminderHour();
        this.reminderMin = model.getReminderMin();
        this.reminderDays = model.getReminderDays();
        this.highlight = model.getHighlight();
        this.archived = model.getArchived();
        observable.notifyListeners();
    }

    /**
     * Flag that indicates whether the habit is archived. Archived habits are
     * usually omitted from listings, unless explicitly included.
     */
    public Integer getArchived()
    {
        return archived;
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
    public Integer getColor()
    {
        return color;
    }

    public void setColor(Integer color)
    {
        this.color = color;
    }

    /**
     * Description of the habit
     */
    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    /**
     * Frequency denominator. If a habit is performed 3 times in 7 days, this
     * field equals 7.
     */
    public Integer getFreqDen()
    {
        return freqDen;
    }

    public void setFreqDen(Integer freqDen)
    {
        this.freqDen = freqDen;
    }

    /**
     * Frequency numerator. If a habit is performed 3 times in 7 days, this
     * field equals 3.
     */
    public Integer getFreqNum()
    {
        return freqNum;
    }

    public void setFreqNum(Integer freqNum)
    {
        this.freqNum = freqNum;
    }

    /**
     * Not currently used.
     */
    public Integer getHighlight()
    {
        return highlight;
    }

    public void setHighlight(Integer highlight)
    {
        this.highlight = highlight;
    }

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    /**
     * Name of the habit
     */
    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public ModelObservable getObservable()
    {
        return observable;
    }

    /**
     * Days of the week the reminder should be shown. This field can be
     * converted to a list of booleans using the method DateHelper.unpackWeekdayList
     * and converted back to an integer by using the method
     * DateHelper.packWeekdayList. If the habit has no reminders, this value
     * should be ignored.
     */
    @NonNull
    public Integer getReminderDays()
    {
        return reminderDays;
    }

    public void setReminderDays(@NonNull Integer reminderDays)
    {
        this.reminderDays = reminderDays;
    }

    /**
     * Hour of the day the reminder should be shown. If there is no reminder,
     * this equals to null.
     */
    @Nullable
    public Integer getReminderHour()
    {
        return reminderHour;
    }

    public void setReminderHour(@Nullable Integer reminderHour)
    {
        this.reminderHour = reminderHour;
    }

    /**
     * Minute the reminder should be shown. If there is no reminder, this equals
     * to null.
     */
    @Nullable
    public Integer getReminderMin()
    {
        return reminderMin;
    }

    public void setReminderMin(@Nullable Integer reminderMin)
    {
        this.reminderMin = reminderMin;
    }

    /**
     * List of repetitions belonging to this habit.
     */
    @NonNull
    public RepetitionList getRepetitions()
    {
        return repetitions;
    }

    /**
     * List of scores belonging to this habit.
     */
    @NonNull
    public ScoreList getScores()
    {
        return scores;
    }

    /**
     * List of streaks belonging to this habit.
     */
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
     * Checks whether the habit has a reminder set.
     *
     * @return true if habit has reminder, false otherwise
     */
    public boolean hasReminder()
    {
        return (reminderHour != null && reminderMin != null);
    }

    /**
     * Returns whether the habit is archived or not.
     *
     * @return true if archived
     */
    public boolean isArchived()
    {
        return archived != 0;
    }

    public void setArchived(Integer archived)
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
            .append("freqNum", freqNum)
            .append("freqDen", freqDen)
            .append("color", color)
            .append("reminderHour", reminderHour)
            .append("reminderMin", reminderMin)
            .append("reminderDays", reminderDays)
            .append("highlight", highlight)
            .append("archived", archived)
            .toString();
    }
}
