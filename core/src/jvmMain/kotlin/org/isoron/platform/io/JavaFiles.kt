/*
 * Copyright (C) 2016-2019 Álinson Santos Xavier <isoron@gmail.com>
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

package org.isoron.platform.io

import java.io.*
import java.nio.file.*

class JavaResourceFile(private val path: Path) : ResourceFile {
    override fun copyTo(dest: UserFile) {
        Files.copy(path, (dest as JavaUserFile).path)
    }

    override fun readLines(): List<String> {
        return Files.readAllLines(path)
    }

    fun stream(): InputStream {
        return Files.newInputStream(path)
    }
}

class JavaUserFile(val path: Path) : UserFile {
    override fun exists(): Boolean {
        return Files.exists(path)
    }

    override fun delete() {
        Files.delete(path)
    }
}

class JavaFileOpener : FileOpener {
    override fun openUserFile(filename: String): UserFile {
        val path = Paths.get("/tmp/$filename")
        return JavaUserFile(path)
    }

    override fun openResourceFile(filename: String): ResourceFile {
        val rootFolders = listOf("assets/main",
                                 "assets/test")
        for (root in rootFolders) {
            val path = Paths.get("$root/$filename")
            if (Files.exists(path) && Files.isReadable(path)) {
                return JavaResourceFile(path)
            }
        }
        throw RuntimeException("file not found")
    }
}
