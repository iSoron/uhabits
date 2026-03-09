package org.isoron.platform.io

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class MigrationTest {
    @Test
    fun testMigrateFromScratch() = runTest {
        val db = TestDatabaseHelper.createEmptyDatabase()
        assertEquals(26, db.getVersion())

        db.run(
            """
            insert into Habits(name, freq_num, freq_den, color, position, archived, type)
            values ('Test', 1, 1, 0, 0, 0, 0)
            """
        )
        db.run(
            """
            insert into Repetitions(habit, timestamp, value)
            values (1, 1000000, 2)
            """
        )

        val stmt = db.prepareStatement("select name from Habits where id = 1")
        assertEquals(StepResult.ROW, stmt.step())
        assertEquals("Test", stmt.getText(0))
        stmt.finalize()

        db.close()
    }

    @Test
    fun testMigrateIdempotent() = runTest {
        val db = TestDatabaseHelper.createEmptyDatabase()
        val version = db.getVersion()
        db.migrateTo(version) { v -> TestDatabaseHelper.loadMigrationSQL(v) }
        assertEquals(version, db.getVersion())
        db.close()
    }
}
