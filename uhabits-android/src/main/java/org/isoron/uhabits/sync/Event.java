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

package org.isoron.uhabits.sync;

import android.support.annotation.NonNull;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

import java.util.List;

@Table(name = "Events")
public class Event extends Model
{
    @NonNull
    @Column(name = "timestamp")
    public Long timestamp;

    @NonNull
    @Column(name = "message")
    public String message;

    @NonNull
    @Column(name = "server_id")
    public String serverId;

    public Event()
    {
        timestamp = 0L;
        message = "";
        serverId = "";
    }

    public Event(@NonNull String serverId, long timestamp, @NonNull String message)
    {
        this.serverId = serverId;
        this.timestamp = timestamp;
        this.message = message;
    }

    @NonNull
    public static List<Event> getAll()
    {
        return new Select().from(Event.class).orderBy("timestamp").execute();
    }
}
