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

import com.nhaarman.mockitokotlin2.whenever
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.withTestApplication
import kotlinx.coroutines.runBlocking
import org.isoron.uhabits.sync.ServiceUnavailable
import org.junit.Test
import kotlin.test.assertEquals

class RegistrationModuleTest : BaseApplicationTest() {
    @Test
    fun `when register succeeds should return generated key`(): Unit = runBlocking {
        whenever(server.register()).thenReturn("ABCDEF")
        withTestApplication(app()) {
            val call = handleRequest(HttpMethod.Post, "/register")
            assertEquals(HttpStatusCode.OK, call.response.status())
            assertEquals("{\"key\":\"ABCDEF\"}", call.response.content)
        }
    }

    @Test
    fun `when registration is unavailable should return 503`(): Unit = runBlocking {
        whenever(server.register()).thenThrow(ServiceUnavailable())
        withTestApplication(app()) {
            val call = handleRequest(HttpMethod.Post, "/register")
            assertEquals(HttpStatusCode.ServiceUnavailable, call.response.status())
        }
    }
}
