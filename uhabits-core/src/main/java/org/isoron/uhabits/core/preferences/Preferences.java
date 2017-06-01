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

package org.isoron.uhabits.core.preferences;

public interface Preferences
{
    int getLastHintNumber();

    long getLastHintTimestamp();

    boolean getShowArchived();

    boolean getShowCompleted();

    int getTheme();

    void incrementLaunchCount();

    boolean isFirstRun();

    boolean isPureBlackEnabled();

    void setDeveloper(boolean isDeveloper);

    void setFirstRun(boolean b);

    void setShowArchived(boolean showArchived);

    void setShowCompleted(boolean showCompleted);

    void setTheme(int theme);

    void updateLastHint(int i, long startOfToday);
}
