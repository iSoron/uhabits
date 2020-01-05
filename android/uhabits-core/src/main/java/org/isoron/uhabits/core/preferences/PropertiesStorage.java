/*
 * Copyright (C) 2015-2017 Álinson Santos Xavier <isoron@gmail.com>
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

package org.isoron.uhabits.core.preferences;

import androidx.annotation.*;

import java.io.*;
import java.util.*;

public class PropertiesStorage implements Preferences.Storage
{
    @NonNull
    private final Properties props;

    @NonNull
    private File file;

    public PropertiesStorage(@NonNull File file)
    {
        try
        {
            this.file = file;
            props = new Properties();
            props.load(new FileInputStream(file));
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void clear()
    {
        for(String key : props.stringPropertyNames()) props.remove(key);
        flush();
    }

    @Override
    public boolean getBoolean(String key, boolean defValue)
    {
        String value = props.getProperty(key, Boolean.toString(defValue));
        return Boolean.parseBoolean(value);
    }

    @Override
    public int getInt(String key, int defValue)
    {
        String value = props.getProperty(key, Integer.toString(defValue));
        return Integer.parseInt(value);
    }

    @Override
    public long getLong(String key, long defValue)
    {
        String value = props.getProperty(key, Long.toString(defValue));
        return Long.parseLong(value);
    }

    @Override
    public String getString(String key, String defValue)
    {
        return props.getProperty(key, defValue);
    }

    @Override
    public void onAttached(Preferences preferences)
    {
        // nop
    }

    @Override
    public void putBoolean(String key, boolean value)
    {
        props.setProperty(key, Boolean.toString(value));
    }

    @Override
    public void putInt(String key, int value)
    {
        props.setProperty(key, Integer.toString(value));
        flush();
    }

    private void flush()
    {
        try
        {
            props.store(new FileOutputStream(file), "");
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void putLong(String key, long value)
    {
        props.setProperty(key, Long.toString(value));
        flush();
    }

    @Override
    public void putString(String key, String value)
    {
        props.setProperty(key, value);
        flush();
    }

    @Override
    public void remove(String key)
    {
        props.remove(key);
        flush();
    }
}
