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

import org.isoron.platform.gui.*
import java.io.*
import java.nio.file.*
import javax.imageio.*

class JavaResourceFile(val path: String) : ResourceFile {
    private val javaPath: Path
        get() {
            val mainPath = Paths.get("assets/main/$path")
            val testPath = Paths.get("assets/test/$path")
            if (Files.exists(mainPath)) return mainPath
            else return testPath
        }

    override suspend fun exists(): Boolean {
        return Files.exists(javaPath)
    }

    override suspend fun lines(): List<String> {
        return Files.readAllLines(javaPath)
    }

    override suspend fun copyTo(dest: UserFile) {
        if (dest.exists()) dest.delete()
        val destPath = (dest as JavaUserFile).path
        destPath.toFile().parentFile?.mkdirs()
        Files.copy(javaPath, destPath)
    }

    fun stream(): InputStream {
        return Files.newInputStream(javaPath)
    }

    override suspend fun toImage(): Image {
        return JavaImage(ImageIO.read(stream()))
    }
}

class JavaUserFile(val path: Path) : UserFile {
    override suspend fun lines(): List<String> {
        return Files.readAllLines(path)
    }

    override suspend fun exists(): Boolean {
        return Files.exists(path)
    }

    override suspend fun delete() {
        Files.delete(path)
    }
}

class JavaFileOpener : FileOpener {
    override fun openUserFile(path: String): UserFile {
        val path = Paths.get("/tmp/$path")
        return JavaUserFile(path)
    }

    override fun openResourceFile(path: String): ResourceFile {
        return JavaResourceFile(path)
    }
}
