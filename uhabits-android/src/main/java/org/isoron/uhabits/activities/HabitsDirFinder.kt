/*
 * Copyright (C) 2016-2021 Álinson Santos Xavier <git@axavier.org>
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
package org.isoron.uhabits.activities

import org.isoron.uhabits.AndroidDirFinder
import org.isoron.uhabits.core.ui.screens.habits.list.ListHabitsBehavior
import org.isoron.uhabits.core.ui.screens.habits.show.ShowHabitMenuPresenter
import java.io.File
import javax.inject.Inject

class HabitsDirFinder @Inject
constructor(
    private val androidDirFinder: AndroidDirFinder
) : ShowHabitMenuPresenter.System, ListHabitsBehavior.DirFinder {

    override fun getCSVOutputDir(): File {
        return androidDirFinder.getFilesDir("CSV")!!
    }
}
