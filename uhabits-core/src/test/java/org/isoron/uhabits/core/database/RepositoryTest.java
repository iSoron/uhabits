/*
 * Copyright (C) 2017 Álinson Santos Xavier <isoron@gmail.com>
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

package org.isoron.uhabits.core.database;

import org.apache.commons.lang3.builder.*;
import org.isoron.uhabits.core.*;
import org.junit.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;


public class RepositoryTest extends BaseUnitTest
{
    private Repository<ThingRecord> repository;

    private Database db;

    @Before
    @Override
    public void setUp() throws Exception
    {
        super.setUp();
        this.db = buildMemoryDatabase();
        repository = new Repository<>(ThingRecord.class, db);

        db.execute("drop table if exists tests");
        db.execute("create table tests(" +
                   "id integer not null primary key autoincrement, " +
                   "color_number integer not null, score float not null, " +
                   "name string)");
    }

    @Test
    public void testFind() throws Exception
    {
        db.execute("insert into tests(id, color_number, name, score) " +
                   "values (10, 20, 'hello', 8.0)");

        ThingRecord record = repository.find(10L);

        assertNotNull(record);
        assertThat(record.id, equalTo(10L));
        assertThat(record.color, equalTo(20));
        assertThat(record.name, equalTo("hello"));
        assertThat(record.score, equalTo(8.0));
    }

    @Test
    public void testSave_withId() throws Exception
    {
        ThingRecord record = new ThingRecord();
        record.id = 50L;
        record.color = 10;
        record.name = "hello";
        record.score = 5.0;
        repository.save(record);
        assertThat(record, equalTo(repository.find(50L)));

        record.name = "world";
        record.score = 128.0;
        repository.save(record);
        assertThat(record, equalTo(repository.find(50L)));
    }

    @Test
    public void testSave_withNull() throws Exception
    {
        ThingRecord record = new ThingRecord();
        record.color = 50;
        record.name = null;
        record.score = 12.0;
        repository.save(record);

        ThingRecord retrieved = repository.find(record.id);
        assertNotNull(retrieved);
        assertNull(retrieved.name);
        assertThat(record, equalTo(retrieved));
    }

    @Test
    public void testSave_withoutId() throws Exception
    {
        ThingRecord r1 = new ThingRecord();
        r1.color = 10;
        r1.name = "hello";
        r1.score = 16.0;
        repository.save(r1);

        ThingRecord r2 = new ThingRecord();
        r2.color = 20;
        r2.name = "world";
        r2.score = 2.0;
        repository.save(r2);

        assertThat(r1.id, equalTo(1L));
        assertThat(r2.id, equalTo(2L));
    }

    @Test
    public void testRemove() throws Exception
    {
        ThingRecord rec1 = new ThingRecord();
        rec1.color = 10;
        rec1.name = "hello";
        rec1.score = 16.0;
        repository.save(rec1);

        ThingRecord rec2 = new ThingRecord();
        rec2.color = 20;
        rec2.name = "world";
        rec2.score = 32.0;
        repository.save(rec2);

        long id = rec1.id;
        assertThat(rec1, equalTo(repository.find(id)));
        assertThat(rec2, equalTo(repository.find(rec2.id)));

        repository.remove(rec1);
        assertThat(rec1.id, equalTo(null));
        assertNull(repository.find(id));
        assertThat(rec2, equalTo(repository.find(rec2.id)));

        repository.remove(rec1); // should have no effect
        assertNull(repository.find(id));
    }
}

@Table(name = "tests")
class ThingRecord
{
    @Column
    public Long id;

    @Column
    public String name;

    @Column(name = "color_number")
    public Integer color;

    @Column
    public Double score;

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        ThingRecord record = (ThingRecord) o;

        return new EqualsBuilder()
            .append(id, record.id)
            .append(name, record.name)
            .append(color, record.color)
            .isEquals();
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder(17, 37)
            .append(id)
            .append(name)
            .append(color)
            .toHashCode();
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this)
            .append("id", id)
            .append("name", name)
            .append("color", color)
            .toString();
    }
}
