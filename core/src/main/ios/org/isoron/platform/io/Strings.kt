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

import kotlinx.cinterop.*

// Although the three following methods have exactly the same implementation,
// replacing them all by a single format(format: String, arg: Any) breaks
// everything, as of Kotlin/Native 1.3.72. Apparently, Kotlin/Native is not
// able to do proper type conversions for variables of type Any when calling
// C functions.

actual fun format(format: String, arg: String): String {
    val buffer = ByteArray(1000)
    buffer.usePinned { p -> platform.posix.sprintf(p.addressOf(0), format, arg) }
    return buffer.toKString()
}

actual fun format(format: String, arg: Int): String {
    val buffer = ByteArray(1000)
    buffer.usePinned { p -> platform.posix.sprintf(p.addressOf(0), format, arg) }
    return buffer.toKString()
}

actual fun format(format: String, arg: Double): String {
    val buffer = ByteArray(1000)
    buffer.usePinned { p -> platform.posix.sprintf(p.addressOf(0), format, arg) }
    return buffer.toKString()
}
