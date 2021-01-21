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

import junit.framework.Assert.assertNull
import org.apache.commons.lang3.builder.EqualsBuilder
import org.apache.commons.lang3.builder.HashCodeBuilder
import org.apache.commons.lang3.builder.ToStringBuilder
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.IsEqual.equalTo
import org.isoron.uhabits.core.BaseUnitTest
import org.junit.Before
import org.junit.Test

class RepositoryTest : BaseUnitTest() {
    private lateinit var repository: Repository<ThingRecord>
    private lateinit var db: Database

    @Before
    @Throws(Exception::class)
    override fun setUp() {
        super.setUp()
        db = buildMemoryDatabase()
        repository = Repository(ThingRecord::class.java, db)
        db.execute("drop table if exists tests")
        db.execute(
            "create table tests(" +
                "id integer not null primary key autoincrement, " +
                "color_number integer not null, score float not null, " +
                "name string)"
        )
    }

    @Test
    @Throws(Exception::class)
    fun testFind() {
        db.execute(
            "insert into tests(id, color_number, name, score) " +
                "values (10, 20, 'hello', 8.0)"
        )
        val record = repository.find(10L)
        assertThat(record!!.id, equalTo(10L))
        assertThat(record.color, equalTo(20))
        assertThat(record.name, equalTo("hello"))
        assertThat(record.score, equalTo(8.0))
    }

    @Test
    @Throws(Exception::class)
    fun testSave_withId() {
        val record = ThingRecord().apply {
            id = 50L
            color = 10
            name = "hello"
            score = 5.0
        }
        repository.save(record)
        assertThat(record, equalTo(repository.find(50L)))
        record.name = "world"
        record.score = 128.0
        repository.save(record)
        assertThat(record, equalTo(repository.find(50L)))
    }

    @Test
    @Throws(Exception::class)
    fun testSave_withNull() {
        val record = ThingRecord().apply {
            color = 50
            name = null
            score = 12.0
        }
        repository.save(record)
        val retrieved = repository.find(record.id!!)
        assertNull(retrieved!!.name)
        assertThat(record, equalTo(retrieved))
    }

    @Test
    @Throws(Exception::class)
    fun testSave_withoutId() {
        val r1 = ThingRecord().apply {
            color = 10
            name = "hello"
            score = 16.0
        }
        repository.save(r1)
        val r2 = ThingRecord().apply {
            color = 20
            name = "world"
            score = 2.0
        }
        repository.save(r2)
        assertThat(r1.id, equalTo(1L))
        assertThat(r2.id, equalTo(2L))
    }

    @Test
    @Throws(Exception::class)
    fun testRemove() {
        val rec1 = ThingRecord().apply {
            color = 10
            name = "hello"
            score = 16.0
        }
        repository.save(rec1)
        val rec2 = ThingRecord().apply {
            color = 20
            name = "world"
            score = 32.0
        }
        repository.save(rec2)
        val id = rec1.id!!
        assertThat(rec1, equalTo(repository.find(id)))
        assertThat(rec2, equalTo(repository.find(rec2.id!!)))
        repository.remove(rec1)
        assertThat(rec1.id, equalTo(null))
        assertNull(repository.find(id))
        assertThat(rec2, equalTo(repository.find(rec2.id!!)))
        repository.remove(rec1) // should have no effect
        assertNull(repository.find(id))
    }

    @Table(name = "tests")
    class ThingRecord {
        @field:Column
        var id: Long? = null

        @field:Column
        var name: String? = null

        @field:Column(name = "color_number")
        var color: Int? = null

        @field:Column
        var score: Double? = null
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || javaClass != other.javaClass) return false
            val record = other as ThingRecord
            return EqualsBuilder()
                .append(id, record.id)
                .append(name, record.name)
                .append(color, record.color)
                .isEquals
        }

        override fun hashCode(): Int {
            return HashCodeBuilder(17, 37)
                .append(id)
                .append(name)
                .append(color)
                .toHashCode()
        }

        override fun toString(): String {
            return ToStringBuilder(this)
                .append("id", id)
                .append("name", name)
                .append("color", color)
                .toString()
        }
    }
}
