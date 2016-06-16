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
import org.isoron.uhabits.*;

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
    private Integer freqNum;

    @NonNull
    private Integer freqDen;

    @NonNull
    private Integer color;

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

    @Nullable
    private Reminder reminder;

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

        copyFrom(model);

        checkmarks = factory.buildCheckmarkList(this);
        streaks = factory.buildStreakList(this);
        scores = factory.buildScoreList(this);
        repetitions = factory.buildRepetitionList(this);
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

        checkmarks = factory.buildCheckmarkList(this);
        streaks = factory.buildStreakList(this);
        scores = factory.buildScoreList(this);
        repetitions = factory.buildRepetitionList(this);
    }

    /**
     * Clears the reminder for a habit. This sets all the related fields to
     * null.
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
        this.freqNum = model.getFreqNum();
        this.freqDen = model.getFreqDen();
        this.color = model.getColor();
        this.reminder = model.reminder;
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

    @NonNull
    public Reminder getReminder()
    {
        if(reminder == null) throw new IllegalStateException();
        return reminder;
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

    public void setFreqNum(@NonNull Integer freqNum)
    {
        this.freqNum = freqNum;
    }

    /**
     * Not currently used.
     */
    @NonNull
    public Integer getHighlight()
    {
        return highlight;
    }

    public void setHighlight(@NonNull Integer highlight)
    {
        this.highlight = highlight;
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

    /**
     * Name of the habit
     */
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
        return reminder != null;
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

    public void setArchived(@NonNull Integer archived)
    {
        this.archived = archived;
    }

    public void setReminder(@Nullable Reminder reminder)
    {
        this.reminder = reminder;
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
            .append("highlight", highlight)
            .append("archived", archived)
            .toString();
    }
}
