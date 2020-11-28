/*
 * Copyright (C) 2016-2020 Álinson Santos Xavier <isoron@gmail.com>
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

import androidx.test.filters.*
import com.fasterxml.jackson.databind.*
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.features.json.*
import io.ktor.client.request.*
import io.ktor.http.*
import junit.framework.Assert.*
import kotlinx.coroutines.*
import org.junit.*

@MediumTest
class RemoteSyncServerTest {

    private val mapper = ObjectMapper()
    val data = SyncData(1, "Hello world")

    @Test
    fun when_register_succeeds_should_return_key() = runBlocking {
        val server = server("/register") {
            respondWithJson(RegisterReponse("ABCDEF"))
        }
        assertEquals("ABCDEF", server.register())
    }

    @Test(expected = ServiceUnavailable::class)
    fun when_register_fails_should_raise_correct_exception() = runBlocking {
        val server = server("/register") {
            respondError(HttpStatusCode.ServiceUnavailable)
        }
        server.register()
        return@runBlocking
    }

    @Test
    fun when_get_data_version_succeeds_should_return_version() = runBlocking {
        server("/db/ABC/version") {
            respondWithJson(GetDataVersionResponse(5))
        }.apply {
            assertEquals(5, getDataVersion("ABC"))
        }
        return@runBlocking
    }

    @Test(expected = ServiceUnavailable::class)
    fun when_get_data_version_with_server_error_should_raise_exception() = runBlocking {
        server("/db/ABC/version") {
            respondError(HttpStatusCode.InternalServerError)
        }.apply {
            getDataVersion("ABC")
        }
        return@runBlocking
    }

    @Test(expected = KeyNotFoundException::class)
    fun when_get_data_version_with_invalid_key_should_raise_exception() = runBlocking {
        server("/db/ABC/version") {
            respondError(HttpStatusCode.NotFound)
        }.apply {
            getDataVersion("ABC")
        }
        return@runBlocking
    }

    @Test
    fun when_get_data_succeeds_should_return_data() = runBlocking {
        server("/db/ABC") {
            respondWithJson(data)
        }.apply {
            assertEquals(data, getData("ABC"))
        }
        return@runBlocking
    }

    @Test(expected = KeyNotFoundException::class)
    fun when_get_data_with_invalid_key_should_raise_exception() = runBlocking {
        server("/db/ABC") {
            respondError(HttpStatusCode.NotFound)
        }.apply {
            getData("ABC")
        }
        return@runBlocking
    }

    @Test
    fun when_put_succeeds_should_not_raise_exceptions() = runBlocking {
        server("/db/ABC") {
            respondOk()
        }.apply {
            put("ABC", data)
        }
        return@runBlocking
    }

    private fun server(expectedPath: String,
                       action: MockRequestHandleScope.(HttpRequestData) -> HttpResponseData
    ): AbstractSyncServer {
        return RemoteSyncServer(httpClient = HttpClient(MockEngine) {
            install(JsonFeature)
            engine {
                addHandler { request ->
                    when (request.url.fullPath) {
                        expectedPath -> action(request)
                        else -> error("unexpected call: ${request.url.fullPath}")
                    }
                }
            }
        })
    }

    private fun MockRequestHandleScope.respondWithJson(content: Any) =
            respond(mapper.writeValueAsBytes(content),
                    headers = headersOf("Content-Type" to listOf("application/json")))
}