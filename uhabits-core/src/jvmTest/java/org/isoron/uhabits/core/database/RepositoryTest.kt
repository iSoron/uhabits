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

import org.apache.commons.lang3.builder.EqualsBuilder
import org.apache.commons.lang3.builder.HashCodeBuilder
import org.apache.commons.lang3.builder.ToStringBuilder
import org.hamcrest.MatcherAssert
import org.hamcrest.core.IsEqual
import org.isoron.uhabits.core.BaseUnitTest
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class RepositoryTest : BaseUnitTest() {
    private var repository: Repository<ThingRecord>? = null
    private var db: Database? = null
    @Before
    @Throws(Exception::class)
    override fun setUp() {
        super.setUp()
        db = buildMemoryDatabase()
        repository = Repository(ThingRecord::class.java, db!!)
        db!!.execute("drop table if exists tests")
        db!!.execute(
            "create table tests(" +
                "id integer not null primary key autoincrement, " +
                "color_number integer not null, score float not null, " +
                "name string)"
        )
    }

    @Test
    @Throws(Exception::class)
    fun testFind() {
        db!!.execute(
            "insert into tests(id, color_number, name, score) " +
                "values (10, 20, 'hello', 8.0)"
        )
        val record = repository!!.find(10L)
        Assert.assertNotNull(record)
        MatcherAssert.assertThat(record!!.id, IsEqual.equalTo(10L))
        MatcherAssert.assertThat(record.color, IsEqual.equalTo(20))
        MatcherAssert.assertThat(record.name, IsEqual.equalTo("hello"))
        MatcherAssert.assertThat(record.score, IsEqual.equalTo(8.0))
    }

    @Test
    @Throws(Exception::class)
    fun testSave_withId() {
        val record = ThingRecord()
        record.id = 50L
        record.color = 10
        record.name = "hello"
        record.score = 5.0
        repository!!.save(record)
        MatcherAssert.assertThat(
            record,
            IsEqual.equalTo(
                repository!!.find(50L)
            )
        )
        record.name = "world"
        record.score = 128.0
        repository!!.save(record)
        MatcherAssert.assertThat(
            record,
            IsEqual.equalTo(
                repository!!.find(50L)
            )
        )
    }

    @Test
    @Throws(Exception::class)
    fun testSave_withNull() {
        val record = ThingRecord()
        record.color = 50
        record.name = null
        record.score = 12.0
        repository!!.save(record)
        val retrieved = repository!!.find(record.id!!)
        Assert.assertNotNull(retrieved)
        Assert.assertNull(retrieved!!.name)
        MatcherAssert.assertThat(record, IsEqual.equalTo(retrieved))
    }

    @Test
    @Throws(Exception::class)
    fun testSave_withoutId() {
        val r1 = ThingRecord()
        r1.color = 10
        r1.name = "hello"
        r1.score = 16.0
        repository!!.save(r1)
        val r2 = ThingRecord()
        r2.color = 20
        r2.name = "world"
        r2.score = 2.0
        repository!!.save(r2)
        MatcherAssert.assertThat(r1.id, IsEqual.equalTo(1L))
        MatcherAssert.assertThat(r2.id, IsEqual.equalTo(2L))
    }

    @Test
    @Throws(Exception::class)
    fun testRemove() {
        val rec1 = ThingRecord()
        rec1.color = 10
        rec1.name = "hello"
        rec1.score = 16.0
        repository!!.save(rec1)
        val rec2 = ThingRecord()
        rec2.color = 20
        rec2.name = "world"
        rec2.score = 32.0
        repository!!.save(rec2)
        val id = rec1.id!!
        MatcherAssert.assertThat(
            rec1,
            IsEqual.equalTo(
                repository!!.find(id)
            )
        )
        MatcherAssert.assertThat(
            rec2,
            IsEqual.equalTo(
                repository!!.find(rec2.id!!)
            )
        )
        repository!!.remove(rec1)
        MatcherAssert.assertThat(rec1.id, IsEqual.equalTo(null))
        Assert.assertNull(repository!!.find(id))
        MatcherAssert.assertThat(
            rec2,
            IsEqual.equalTo(
                repository!!.find(rec2.id!!)
            )
        )
        repository!!.remove(rec1) // should have no effect
        Assert.assertNull(repository!!.find(id))
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
