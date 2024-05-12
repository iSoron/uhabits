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
package org.isoron.uhabits.core.database

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.IsEqual.equalTo
import org.junit.Test
import java.io.ByteArrayInputStream

class SQLParserTest {
    @Test
    fun testParseSimple() {
        val sql = "CREATE TABLE habits (id INTEGER); INSERT INTO habits VALUES (1);"
        val commands = SQLParser.parse(ByteArrayInputStream(sql.toByteArray()))
        assertThat(commands.size, equalTo(2))
        assertThat(commands[0], equalTo("CREATE TABLE habits (id INTEGER)"))
        assertThat(commands[1], equalTo("INSERT INTO habits VALUES (1)"))
    }

    @Test
    fun testParseWithComments() {
        val sql = """
            -- This is a comment
            CREATE TABLE habits (id INTEGER);
            /* This is a 
               block comment */
            INSERT INTO habits VALUES (1);
        """.trimIndent()
        val commands = SQLParser.parse(ByteArrayInputStream(sql.toByteArray()))
        assertThat(commands.size, equalTo(2))
        assertThat(commands[0], equalTo("CREATE TABLE habits (id INTEGER)"))
        assertThat(commands[1], equalTo("INSERT INTO habits VALUES (1)"))
    }

    @Test
    fun testParseWithStrings() {
        val sql = "INSERT INTO habits (name) VALUES ('Semicolon; inside');"
        val commands = SQLParser.parse(ByteArrayInputStream(sql.toByteArray()))
        assertThat(commands.size, equalTo(1))
        assertThat(commands[0], equalTo("INSERT INTO habits (name) VALUES ('Semicolon; inside')"))
    }
}
