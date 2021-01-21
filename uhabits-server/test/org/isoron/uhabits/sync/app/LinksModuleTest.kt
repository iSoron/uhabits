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
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.TestApplicationCall
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.server.testing.withTestApplication
import kotlinx.coroutines.runBlocking
import org.isoron.uhabits.sync.KeyNotFoundException
import org.isoron.uhabits.sync.links.Link
import org.isoron.uhabits.sync.links.toJson
import org.junit.Test
import kotlin.test.assertEquals

class LinksModuleTest : BaseApplicationTest() {
    private val link = Link(
        id = "ABC123",
        syncKey = "SECRET",
        createdAt = System.currentTimeMillis(),
    )

    @Test
    fun `when POST is successful should return link`(): Unit = runBlocking {
        whenever(server.registerLink("SECRET")).thenReturn(link)
        withTestApplication(app()) {
            handlePost("/links", LinkRegisterRequestData(syncKey = "SECRET")).apply {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(link.toJson(), response.content)
            }
        }
    }

    @Test
    fun `when GET is successful should return link`(): Unit = runBlocking {
        whenever(server.getLink("ABC123")).thenReturn(link)
        withTestApplication(app()) {
            handleGet("/links/ABC123").apply {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(link.toJson(), response.content)
            }
        }
    }

    @Test
    fun `GET with invalid link id should return 404`(): Unit = runBlocking {
        whenever(server.getLink("ABC123")).thenThrow(KeyNotFoundException())
        withTestApplication(app()) {
            handleGet("/links/ABC123").apply {
                assertEquals(HttpStatusCode.NotFound, response.status())
            }
        }
    }

    private fun TestApplicationEngine.handlePost(
        url: String,
        data: LinkRegisterRequestData
    ): TestApplicationCall {
        return handleRequest(HttpMethod.Post, url) {
            addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(data.toJson())
        }
    }

    private fun TestApplicationEngine.handleGet(url: String): TestApplicationCall {
        return handleRequest(HttpMethod.Get, url)
    }
}
