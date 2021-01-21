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

package org.isoron.uhabits.sync.app

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.jackson.*
import io.ktor.routing.*
import org.isoron.uhabits.sync.*
import org.isoron.uhabits.sync.repository.*
import org.isoron.uhabits.sync.server.*
import java.nio.file.*

fun Application.main() = SyncApplication().apply { main() }

val REPOSITORY_PATH: Path = Paths.get(System.getenv("LOOP_REPO_PATH")!!)

class SyncApplication(
    val server: AbstractSyncServer = RepositorySyncServer(
        FileRepository(REPOSITORY_PATH),
    ),
) {
    fun Application.main() {
        install(DefaultHeaders)
        install(CallLogging)
        install(ContentNegotiation) {
            jackson { }
        }
        routing {
            registration(this@SyncApplication)
            storage(this@SyncApplication)
            links(this@SyncApplication)
            metrics(this@SyncApplication)
        }
    }
}
