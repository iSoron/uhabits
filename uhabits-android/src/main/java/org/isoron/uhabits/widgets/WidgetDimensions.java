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

package org.isoron.uhabits.widgets;

public class WidgetDimensions
{
    private final int portraitWidth;

    private final int portraitHeight;

    private final int landscapeWidth;

    private final int landscapeHeight;

    public WidgetDimensions(int portraitWidth,
                            int portraitHeight,
                            int landscapeWidth,
                            int landscapeHeight)
    {
        this.portraitWidth = portraitWidth;
        this.portraitHeight = portraitHeight;
        this.landscapeWidth = landscapeWidth;
        this.landscapeHeight = landscapeHeight;
    }

    public int getLandscapeHeight()
    {
        return landscapeHeight;
    }

    public int getLandscapeWidth()
    {
        return landscapeWidth;
    }

    public int getPortraitHeight()
    {
        return portraitHeight;
    }

    public int getPortraitWidth()
    {
        return portraitWidth;
    }
}