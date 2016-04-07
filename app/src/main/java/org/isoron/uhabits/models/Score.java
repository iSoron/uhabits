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

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

@Table(name = "Score")
public class Score extends Model
{
    /**
     * Minimum score value required to earn half a star.
     */
    public static final int HALF_STAR_CUTOFF =  9629750;

    /**
     * Minimum score value required to earn a full star.
     */
    public static final int FULL_STAR_CUTOFF = 15407600;

    /**
     * Maximum score value attainable by any habit.
     */
    public static final int MAX_VALUE = 19259478;

    /**
     * Status indicating that the habit has not earned any star.
     */
    public static final int EMPTY_STAR = 0;

    /**
     * Status indicating that the habit has earned half a star.
     */
    public static final int HALF_STAR = 1;

    /**
     * Status indicating that the habit has earned a full star.
     */
    public static final int FULL_STAR = 2;

    /**
     * Habit to which this score belongs to.
     */
    @Column(name = "habit")
    public Habit habit;

    /**
     * Timestamp of the day to which this score applies. Time of day should be midnight (UTC).
     */
    @Column(name = "timestamp")
    public Long timestamp;

    /**
     * Value of the score.
     */
    @Column(name = "score")
    public Integer score;

    /**
     * Given the frequency of the habit, the previous score, and the value of the current checkmark,
     * computes the current score for the habit.
     *
     * The frequency of the habit is the number of repetitions divided by the length of the
     * interval. For example, a habit that should be repeated 3 times in 8 days has frequency 3.0 /
     * 8.0 = 0.375.
     *
     * The checkmarkValue should be UNCHECKED, CHECKED_IMPLICITLY or CHECK_EXPLICITLY.
     *
     * @param frequency the frequency of the habit
     * @param previousScore the previous score of the habit
     * @param checkmarkValue the value of the current checkmark
     *
     * @return the current score
     */
    public static int compute(double frequency, int previousScore, int checkmarkValue)
    {
        double multiplier = Math.pow(0.5, 1.0 / (14.0 / frequency - 1));
        int score = (int) (previousScore * multiplier);

        if (checkmarkValue == Checkmark.CHECKED_EXPLICITLY)
        {
            score += 1000000;
            score = Math.min(score, Score.MAX_VALUE);
        }

        return score;
    }

    /**
     * Return the current star status for the habit, which can one of EMPTY_STAR, HALF_STAR or
     * FULL_STAR.
     *
     * @return current star status
     */
    public int getStarStatus()
    {
        if(score >= Score.FULL_STAR_CUTOFF) return Score.FULL_STAR;
        if(score >= Score.HALF_STAR_CUTOFF) return Score.HALF_STAR;
        return Score.EMPTY_STAR;
    }
}
