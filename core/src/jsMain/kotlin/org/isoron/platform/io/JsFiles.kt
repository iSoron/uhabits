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

import kotlinx.coroutines.*
import org.w3c.dom.events.*
import org.w3c.xhr.*
import kotlin.js.*

class JsFileOpener : FileOpener {
    override fun openUserFile(filename: String): UserFile {
        return JsUserFile(filename)
    }

    override fun openResourceFile(filename: String): ResourceFile {
        return JsResourceFile(filename)
    }
}

class JsUserFile(filename: String) : UserFile {
    override fun delete() {
        TODO()
    }

    override fun exists(): Boolean {
        TODO()
    }
}

class JsResourceFile(val filename: String) : ResourceFile {
    override suspend fun lines(): List<String> {
        return Promise<List<String>> { resolve, reject ->
            val xhr = XMLHttpRequest()
            xhr.open("GET", "/assets/$filename", true)
            xhr.onload = { resolve(xhr.responseText.lines()) }
            xhr.onerror = { reject(Exception()) }
            xhr.send()
        }.await()
    }

    override fun copyTo(dest: UserFile) {
        TODO()
    }
}
