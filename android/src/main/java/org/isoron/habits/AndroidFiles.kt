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

package org.isoron.habits

import android.content.*
import org.isoron.uhabits.utils.*
import java.io.*
import java.util.*


class AndroidFileOpener(val context: Context) : FileOpener {
    override fun openUserFile(filename: String): UserFile {
        return AndroidUserFile(File(context.filesDir, filename))
    }

    override fun openResourceFile(filename: String): ResourceFile {
        return AndroidResourceFile(context, filename)
    }
}

class AndroidResourceFile(val context: Context,
                          val filename: String) : ResourceFile {

    override fun readLines(): List<String> {
        val asset = context.assets.open(filename)
        val reader = BufferedReader(InputStreamReader(asset))
        val lines = ArrayList<String>()
        while (true) {
            val line = reader.readLine() ?: break
            lines.add(line)
        }
        return lines
    }
}

class AndroidUserFile(val file: File) : UserFile {
    override fun delete() {
        file.delete()
    }

    override fun exists(): Boolean {
        return file.exists()
    }

}
