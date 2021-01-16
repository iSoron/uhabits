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

package org.isoron.uhabits.sync.repository

import org.isoron.uhabits.sync.*
import java.io.*
import java.nio.file.*

class FileRepository(
    private val basepath: Path,
) : Repository {

    override suspend fun put(key: String, data: SyncData) {
        // Create directory
        val dataPath = key.toDataPath()
        val dataDir = dataPath.toFile()
        dataDir.mkdirs()

        // Create metadata
        val metadataFile = dataPath.resolve("version").toFile()
        metadataFile.outputStream().use { outputStream ->
            PrintWriter(outputStream).use { printWriter ->
                printWriter.print(data.version)
            }
        }

        // Create data file
        val dataFile = dataPath.resolve("content").toFile()
        dataFile.outputStream().use { outputStream ->
            PrintWriter(outputStream).use { printWriter ->
                printWriter.print(data.content)
            }
        }
    }

    override suspend fun get(key: String): SyncData {
        val dataPath = key.toDataPath()
        val contentFile = dataPath.resolve("content").toFile()
        val versionFile = dataPath.resolve("version").toFile()
        if (!contentFile.exists() || !versionFile.exists()) {
            throw KeyNotFoundException()
        }
        val version = versionFile.readText().trim().toLong()
        return SyncData(version, contentFile.readText())
    }

    override suspend fun contains(key: String): Boolean {
        val dataPath = key.toDataPath()
        val versionFile = dataPath.resolve("version").toFile()
        return versionFile.exists()
    }

    private fun String.toDataPath(): Path {
        return basepath.resolve("${this[0]}/${this[1]}/${this[2]}/${this[3]}/$this")
    }
}