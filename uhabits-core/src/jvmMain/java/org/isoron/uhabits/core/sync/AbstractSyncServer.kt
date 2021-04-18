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

package org.isoron.uhabits.core.sync

interface AbstractSyncServer {
    /**
     * Generates and returns a new sync key, which can be used to store and retrive
     * data.
     *
     * @throws ServiceUnavailable If key cannot be generated at this time, for example,
     *      due to insufficient server resources, temporary server maintenance or network problems.
     */
    suspend fun register(): String

    /**
     * Replaces data for a given sync key.
     *
     * @throws KeyNotFoundException If key is not found
     * @throws EditConflictException If the version of the data provided is not
     *      exactly the current data version plus one.
     * @throws ServiceUnavailable If data cannot be put at this time, for example, due
     *      to insufficient server resources or network problems.
     */
    suspend fun put(key: String, newData: SyncData)

    /**
     * Returns data for a given sync key.
     *
     * @throws KeyNotFoundException If key is not found
     * @throws ServiceUnavailable If data cannot be retrieved at this time, for example, due
     *      to insufficient server resources or network problems.
     */
    suspend fun getData(key: String): SyncData

    /**
     * Returns the current data version for the given key
     *
     * @throws KeyNotFoundException If key is not found
     * @throws ServiceUnavailable If data cannot be retrieved at this time, for example, due
     *      to insufficient server resources or network problems.
     */
    suspend fun getDataVersion(key: String): Long
}
