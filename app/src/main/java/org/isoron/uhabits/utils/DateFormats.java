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

package org.isoron.uhabits.utils;

import android.support.annotation.*;

import java.text.*;
import java.util.*;

import static android.os.Build.VERSION.*;
import static android.os.Build.VERSION_CODES.*;
import static android.text.format.DateFormat.*;

public class DateFormats
{
    @NonNull
    private static SimpleDateFormat fromSkeleton(@NonNull String skeleton,
                                                 @NonNull Locale locale)
    {
        SimpleDateFormat df = new SimpleDateFormat(skeleton, locale);
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        return df;
    }

    @NonNull
    public static SimpleDateFormat fromSkeleton(@NonNull String skeleton)
    {
        Locale locale = Locale.getDefault();

        if (SDK_INT >= JELLY_BEAN_MR2)
            skeleton = getBestDateTimePattern(locale, skeleton);

        return fromSkeleton(skeleton, locale);
    }

    public static SimpleDateFormat getBackupDateFormat()
    {
        return fromSkeleton("yyyy-MM-dd HHmmss", Locale.US);
    }

    public static SimpleDateFormat getCSVDateFormat()
    {
        return fromSkeleton("yyyy-MM-dd", Locale.US);
    }
}
