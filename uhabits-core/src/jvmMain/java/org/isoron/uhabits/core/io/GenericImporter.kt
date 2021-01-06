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
package org.isoron.uhabits.core.io

import java.io.File
import javax.inject.Inject

/**
 * A GenericImporter decides which implementation of AbstractImporter is able to
 * handle a given file and delegates to it the task of importing the data.
 */
class GenericImporter
@Inject constructor(
    loopDBImporter: LoopDBImporter,
    rewireDBImporter: RewireDBImporter,
    tickmateDBImporter: TickmateDBImporter,
    habitBullCSVImporter: HabitBullCSVImporter,
) : AbstractImporter() {

    var importers: List<AbstractImporter> = listOf(
        loopDBImporter,
        rewireDBImporter,
        tickmateDBImporter,
        habitBullCSVImporter,
    )

    override fun canHandle(file: File): Boolean {
        for (importer in importers) {
            if (importer.canHandle(file)) {
                return true
            }
        }
        return false
    }

    override fun importHabitsFromFile(file: File) {
        for (importer in importers) {
            if (importer.canHandle(file)) {
                importer.importHabitsFromFile(file)
            }
        }
    }
}
