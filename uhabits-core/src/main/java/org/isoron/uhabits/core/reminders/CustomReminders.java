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

package org.isoron.uhabits.core.reminders;

import android.support.annotation.*;

import java.util.*;

public class CustomReminders
{
    private Map< Long, Long > map; // Habit id, Timestamp

    private Saver saver;

    CustomReminders()
    {
        map = new HashMap< Long, Long >();
    }

    public void setSaver( Saver saver )
    {
        this.saver = saver;
        if( saver != null )
            map = saver.load();
    }

    public void set(@NonNull Long habitId, @NonNull Long reminderTime)
    {
        map.put( habitId, reminderTime );
        save();
    }

    public void remove(@NonNull Long habitId )
    {
        if( map.remove( habitId ) != null )
            save();
    }

    public Long get( @NonNull Long habitId )
    {
        return map.get( habitId );
    }

    private void save()
    {
        if( saver != null )
            saver.save( map );
    }

    public interface Saver
    {
        void save( Map< Long, Long > map);
        Map< Long, Long > load();
    }
}
