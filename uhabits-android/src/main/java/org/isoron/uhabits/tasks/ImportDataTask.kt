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
package org.isoron.uhabits.tasks

import android.util.Log
import org.isoron.uhabits.core.io.GenericImporter
import org.isoron.uhabits.core.models.ModelFactory
import org.isoron.uhabits.core.models.sqlite.SQLModelFactory
import org.isoron.uhabits.core.tasks.Task
import java.io.File

class ImportDataTask(
    private val importer: GenericImporter,
    modelFactory: ModelFactory,
    private val file: File,
    private val listener: Listener
) : Task {
    private var result = 0
    private val modelFactory: SQLModelFactory = modelFactory as SQLModelFactory
    override fun doInBackground() {
        modelFactory.database.beginTransaction()
        try {
            if (importer.canHandle(file)) {
                importer.importHabitsFromFile(file)
                result = SUCCESS
                modelFactory.database.setTransactionSuccessful()
            } else {
                result = NOT_RECOGNIZED
            }
        } catch (e: Exception) {
            result = FAILED
            Log.e("ImportDataTask", "Import failed", e)
        }
        modelFactory.database.endTransaction()
    }

    override fun onPostExecute() {
        listener.onImportDataFinished(result)
    }

    fun interface Listener {
        fun onImportDataFinished(result: Int)
    }

    companion object {
        const val FAILED = 3
        const val NOT_RECOGNIZED = 2
        const val SUCCESS = 1
    }
}
