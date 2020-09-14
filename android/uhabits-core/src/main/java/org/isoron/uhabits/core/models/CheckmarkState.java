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


import javax.annotation.concurrent.ThreadSafe;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import static org.isoron.uhabits.core.utils.StringUtils.defaultToStringStyle;




/**
 * A CheckmarkState represents the completion status of the habit.
 */
@ThreadSafe
public final class CheckmarkState
{

    /**
     * The value of the checkmark.
     * <p>
     * For boolean habits, this equals either NO, YES or SKIP.
     * <p>
     * For numerical habits, this number is stored in thousandths. That
     * is, if the user enters value 1.50 on the app, it is stored as 1500.
     */
    private final int value;

    /**
     * The indicator whether the checkmark value was added manually or computed by the algorithm
     */
    private final boolean manualInput;

    public CheckmarkState(int value, boolean manualInput)
    {
        this.value = value;
        this.manualInput = manualInput;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        CheckmarkState checkmark = (CheckmarkState) o;

        return new EqualsBuilder()
            .append(value, checkmark.value)
            .append(manualInput, checkmark.manualInput)
            .isEquals();
    }

    public int getValue()
    {
        return value;
    }

    public boolean isManualInput()
    {
        return manualInput;
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder(17, 37)
            .append(value)
            .append(manualInput)
            .toHashCode();
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, defaultToStringStyle())
            .append("value", value)
            .append("manualInput", manualInput)
            .toString();
    }
}
