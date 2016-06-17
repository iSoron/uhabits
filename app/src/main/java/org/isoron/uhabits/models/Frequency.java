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

import org.apache.commons.lang3.builder.*;

/**
 * Represents how often is the habit repeated.
 */
public class Frequency
{
    public static final Frequency DAILY = new Frequency(1, 1);

    public static final Frequency FIVE_TIMES_PER_WEEK = new Frequency(5, 7);

    public static final Frequency THREE_TIMES_PER_WEEK = new Frequency(3, 7);

    public static final Frequency TWO_TIMES_PER_WEEK = new Frequency(2, 7);

    public static final Frequency WEEKLY = new Frequency(1, 7);

    private final int numerator;

    private final int denominator;

    public Frequency(int numerator, int denominator)
    {
        if (numerator == denominator) numerator = denominator = 1;

        this.numerator = numerator;
        this.denominator = denominator;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Frequency frequency = (Frequency) o;

        return new EqualsBuilder()
            .append(numerator, frequency.numerator)
            .append(denominator, frequency.denominator)
            .isEquals();
    }

    public int getDenominator()
    {
        return denominator;
    }

    public int getNumerator()
    {
        return numerator;
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder(17, 37)
            .append(numerator)
            .append(denominator)
            .toHashCode();
    }

    public double toDouble()
    {
        return (double) numerator / denominator;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this)
            .append("numerator", numerator)
            .append("denominator", denominator)
            .toString();
    }
}
