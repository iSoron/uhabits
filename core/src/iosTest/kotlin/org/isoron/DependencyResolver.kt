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

package org.isoron

import org.isoron.platform.io.*
import org.isoron.platform.time.*

actual object DependencyResolver {
    actual suspend fun getFileOpener(): FileOpener = IosFileOpener()

    actual fun getDateFormatter(locale: Locale): LocalDateFormatter {
        return when(locale) {
            Locale.US -> IosLocalDateFormatter("en-US")
            Locale.JAPAN -> IosLocalDateFormatter("ja-JP")
        }
    }

    // IosDatabase and IosCanvas are currently implemented in Swift, so we
    // cannot test these classes here. The tests will be skipped.
    actual suspend fun getDatabase(): Database = TODO()
    actual fun getCanvasHelper(): CanvasHelper = TODO()
    actual val supportsDatabaseTests = false
    actual val supportsCanvasTests = false
}