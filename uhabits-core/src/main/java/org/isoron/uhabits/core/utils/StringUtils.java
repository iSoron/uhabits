/*
 * Copyright (C) 2017 Álinson Santos Xavier <isoron@gmail.com>
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

package org.isoron.uhabits.core.utils;

import org.apache.commons.lang3.builder.*;

import java.math.*;
import java.util.*;

public class StringUtils
{
    private static StandardToStringStyle toStringStyle = null;

    public static String getRandomId()
    {
        return new BigInteger(260, new Random()).toString(32).substring(0, 32);
    }

    public static ToStringStyle defaultToStringStyle()
    {
        if (toStringStyle == null)
        {
            toStringStyle = new StandardToStringStyle();
            toStringStyle.setFieldSeparator(", ");
            toStringStyle.setUseClassName(false);
            toStringStyle.setUseIdentityHashCode(false);
            toStringStyle.setContentStart("{");
            toStringStyle.setContentEnd("}");
            toStringStyle.setFieldNameValueSeparator(": ");
            toStringStyle.setArrayStart("[");
            toStringStyle.setArrayEnd("]");
        }

        return toStringStyle;
    }

    public static String joinLongs(long values[])
    {
        return org.apache.commons.lang3.StringUtils.join(values, ',');
    }

    public static long[] splitLongs(String str)
    {
        String parts[] = org.apache.commons.lang3.StringUtils.split(str, ',');

        long numbers[] = new long[parts.length];
        for (int i = 0; i < parts.length; i++) numbers[i] = Long.valueOf(parts[i]);
        return numbers;
    }
}
