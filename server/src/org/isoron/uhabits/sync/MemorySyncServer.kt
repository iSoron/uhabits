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

package org.isoron.uhabits.sync

import java.util.*
import kotlin.streams.*

/**
 * An AbstractSyncServer that stores all data in memory.
 */
class MemorySyncServer : AbstractSyncServer {
    private val db = mutableMapOf<String, SyncData>()

    override fun register(): String {
        synchronized(db) {
            val key = generateKey()
            db[key] = SyncData(0, "")
            return key
        }
    }

    override fun put(key: String, newData: SyncData) {
        synchronized(db) {
            if (!db.containsKey(key)) {
                throw KeyNotFoundException()
            }
            val prevData = db.getValue(key)
            if (newData.version != prevData.version + 1) {
                throw EditConflictException()
            }
            db[key] = newData
        }
    }

    override fun get(key: String): SyncData {
        synchronized(db) {
            if (!db.containsKey(key)) {
                throw KeyNotFoundException()
            }
            return db.getValue(key)
        }
    }

    private fun generateKey(): String {
        val chars = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
        while (true) {
            val key = Random().ints(64, 0, chars.length)
                .asSequence()
                .map(chars::get)
                .joinToString("")
            if (!db.containsKey(key))
                return key
        }

    }
}
