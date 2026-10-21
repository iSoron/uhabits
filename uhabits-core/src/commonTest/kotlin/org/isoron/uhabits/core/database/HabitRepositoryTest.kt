package org.isoron.uhabits.core.database

import kotlinx.coroutines.test.runTest
import org.isoron.platform.io.TestDatabaseHelper
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class HabitRepositoryTest {
    @Test
    fun testInsertAndFindAll() = runTest {
        val db = TestDatabaseHelper.createEmptyDatabase()
        val repo = HabitRepository(db)

        assertEquals(0, repo.findAll().size)

        val data = HabitData(
            name = "Wake up early",
            description = "Before 6am",
            question = "Did you wake up early?",
            freqNum = 1,
            freqDen = 1,
            color = 3,
            position = 0,
            archived = 0,
            type = 0,
            unit = "",
            targetValue = 0.0,
            targetType = 0,
            uuid = "abc-123"
        )
        val id = repo.insert(data)
        assertTrue(id > 0)

        val all = repo.findAll()
        assertEquals(1, all.size)
        assertEquals("Wake up early", all[0].name)
        assertEquals("Did you wake up early?", all[0].question)
        assertEquals("abc-123", all[0].uuid)
        assertEquals(id, all[0].id)

        db.close()
    }

    @Test
    fun testUpdate() = runTest {
        val db = TestDatabaseHelper.createEmptyDatabase()
        val repo = HabitRepository(db)

        val data = HabitData(name = "Exercise", freqNum = 1, freqDen = 2, position = 0)
        data.id = repo.insert(data)

        data.name = "Exercise daily"
        data.freqNum = 1
        data.freqDen = 1
        repo.update(data)

        val updated = repo.findAll()
        assertEquals(1, updated.size)
        assertEquals("Exercise daily", updated[0].name)
        assertEquals(1, updated[0].freqDen)

        db.close()
    }

    @Test
    fun testDelete() = runTest {
        val db = TestDatabaseHelper.createEmptyDatabase()
        val repo = HabitRepository(db)

        val id1 = repo.insert(HabitData(name = "A", position = 0))
        repo.insert(HabitData(name = "B", position = 1))
        repo.insert(HabitData(name = "C", position = 2))
        assertEquals(3, repo.findAll().size)

        repo.delete(id1 + 1) // delete "B"
        val remaining = repo.findAll()
        assertEquals(2, remaining.size)
        assertEquals("A", remaining[0].name)
        assertEquals("C", remaining[1].name)

        db.close()
    }

    @Test
    fun testNullableFields() = runTest {
        val db = TestDatabaseHelper.createEmptyDatabase()
        val repo = HabitRepository(db)

        val data = HabitData(
            name = "No reminder",
            position = 0,
            reminderHour = null,
            reminderMin = null,
            reminderDays = 0
        )
        data.id = repo.insert(data)

        val loaded = repo.findAll()
        assertEquals(1, loaded.size)
        assertNull(loaded[0].reminderHour)
        assertNull(loaded[0].reminderMin)
        assertEquals(0, loaded[0].reminderDays)

        data.reminderHour = 8
        data.reminderMin = 30
        data.reminderDays = 127
        repo.update(data)

        val updated = repo.findAll()
        assertEquals(8, updated[0].reminderHour)
        assertEquals(30, updated[0].reminderMin)
        assertEquals(127, updated[0].reminderDays)

        db.close()
    }

    @Test
    fun testFindAllOrderedByPosition() = runTest {
        val db = TestDatabaseHelper.createEmptyDatabase()
        val repo = HabitRepository(db)

        repo.insert(HabitData(name = "Third", position = 2))
        repo.insert(HabitData(name = "First", position = 0))
        repo.insert(HabitData(name = "Second", position = 1))

        val all = repo.findAll()
        assertEquals("First", all[0].name)
        assertEquals("Second", all[1].name)
        assertEquals("Third", all[2].name)

        db.close()
    }

    @Test
    fun testExecSQL() = runTest {
        val db = TestDatabaseHelper.createEmptyDatabase()
        val repo = HabitRepository(db)

        repo.insert(HabitData(name = "A", position = 0))
        repo.insert(HabitData(name = "B", position = 1))
        repo.insert(HabitData(name = "C", position = 2))

        repo.execSQL("update Habits set position = position + 1 where position >= 0 and position < 2")

        val all = repo.findAll()
        assertEquals(1, all[0].position)
        assertEquals(2, all[1].position)
        assertEquals(2, all[2].position)

        db.close()
    }

    @Test
    fun testAllFieldsSurviveRoundTrip() = runTest {
        val db = TestDatabaseHelper.createEmptyDatabase()
        val repo = HabitRepository(db)

        val original = HabitData(
            name = "Meditate",
            description = "10 minutes of mindfulness",
            question = "Did you meditate today?",
            freqNum = 3,
            freqDen = 7,
            color = 5,
            position = 42,
            reminderHour = 7,
            reminderMin = 30,
            reminderDays = 127,
            highlight = 0,
            archived = 1,
            type = 1,
            targetValue = 10.0,
            targetType = 1,
            unit = "minutes",
            uuid = "550e8400-e29b-41d4-a716-446655440000",
            category = "Health"
        )
        original.id = repo.insert(original)

        val loaded = repo.findAll().single()
        assertEquals(original.name, loaded.name)
        assertEquals(original.description, loaded.description)
        assertEquals(original.question, loaded.question)
        assertEquals(original.freqNum, loaded.freqNum)
        assertEquals(original.freqDen, loaded.freqDen)
        assertEquals(original.color, loaded.color)
        assertEquals(original.position, loaded.position)
        assertEquals(original.reminderHour, loaded.reminderHour)
        assertEquals(original.reminderMin, loaded.reminderMin)
        assertEquals(original.reminderDays, loaded.reminderDays)
        assertEquals(original.highlight, loaded.highlight)
        assertEquals(original.archived, loaded.archived)
        assertEquals(original.type, loaded.type)
        assertEquals(original.targetValue, loaded.targetValue)
        assertEquals(original.targetType, loaded.targetType)
        assertEquals(original.unit, loaded.unit)
        assertEquals(original.uuid, loaded.uuid)
        assertEquals(original.category, loaded.category)
        assertNotNull(loaded.id)
        assertEquals(original.id, loaded.id)

        db.close()
    }
}
