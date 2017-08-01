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
 *
 *
 */

package org.isoron.uhabits.core.database;

import java.sql.*;

public class JdbcCursor implements Cursor
{
    private ResultSet resultSet;

    public JdbcCursor(ResultSet resultSet)
    {
        this.resultSet = resultSet;
    }

    @Override
    public void close()
    {
        try
        {
            resultSet.close();
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean moveToNext()
    {
        try
        {
            return resultSet.next();
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Integer getInt(int index)
    {
        try
        {
            Integer value = resultSet.getInt(index + 1);
            if(resultSet.wasNull()) return null;
            else return value;
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Long getLong(int index)
    {
        try
        {
            Long value = resultSet.getLong(index + 1);
            if(resultSet.wasNull()) return null;
            else return value;
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Double getDouble(int index)
    {
        try
        {
            Double value = resultSet.getDouble(index + 1);
            if(resultSet.wasNull()) return null;
            else return value;
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getString(int index)
    {
        try
        {
            String value = resultSet.getString(index + 1);
            if(resultSet.wasNull()) return null;
            else return value;
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
    }
}
