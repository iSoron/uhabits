/*
 * Copyright (C) 2016-2020 Alinson Santos Xavier <git@axavier.org>
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

package org.isoron.uhabits.sync.server

import org.isoron.uhabits.sync.*
import org.isoron.uhabits.sync.repository.*
import java.util.*
import kotlin.streams.*

/**
 * An AbstractSyncServer that stores all data in a [Repository].
 */
class RepositorySyncServer(
    private val repo: Repository,
) : AbstractSyncServer {

    override suspend fun register(): String {
        val key = generateKey()
        repo.put(key, SyncData(0, ""))
        return key
    }

    override suspend fun put(key: String, newData: SyncData) {
        if (!repo.contains(key)) {
            throw KeyNotFoundException()
        }
        val prevData = repo.get(key)
        if (newData.version != prevData.version + 1) {
            throw EditConflictException()
        }
        repo.put(key, newData)
    }

    override suspend fun getData(key: String): SyncData {
        if (!repo.contains(key)) {
            throw KeyNotFoundException()
        }
        return repo.get(key)
    }

    override suspend fun getDataVersion(key: String): Long {
        if (!repo.contains(key)) {
            throw KeyNotFoundException()
        }
        return repo.get(key).version
    }

    private suspend fun generateKey(): String {
        val chars = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
        while (true) {
            val key = Random().ints(64, 0, chars.length)
                .asSequence()
                .map(chars::get)
                .joinToString("")
            if (!repo.contains(key))
                return key
        }

    }
}
