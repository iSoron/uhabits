package org.isoron.uhabits.core.database.migrations

import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.isoron.uhabits.core.BaseUnitTest
import org.isoron.uhabits.core.database.Database
import org.isoron.uhabits.core.database.MigrationHelper
import org.isoron.uhabits.core.models.sqlite.SQLModelFactory
import org.isoron.uhabits.core.test.HabitFixtures
import org.junit.Test

class Version23Test : BaseUnitTest() {

    private lateinit var db: Database

    private lateinit var helper: MigrationHelper

    override fun setUp() {
        super.setUp()
        db = openDatabaseResource("/databases/022.db")
        helper = MigrationHelper(db)
        modelFactory = SQLModelFactory(db)
        habitList = modelFactory.buildHabitList()
        fixtures = HabitFixtures(modelFactory, habitList)
    }

    private fun migrateTo23() = helper.migrateTo(23)

    @Test
    fun `test migrate to 23 creates question column`() {
        migrateTo23()
        val cursor = db.query("select question from Habits")
        cursor.moveToNext()
    }

    @Test
    fun `test migrate to 23 moves description to question column`() {
        var cursor = db.query("select description from Habits")

        val descriptions = mutableListOf<String?>()
        while (cursor.moveToNext()) {
            descriptions.add(cursor.getString(0))
        }

        migrateTo23()
        cursor = db.query("select question from Habits")

        for (i in 0 until descriptions.size) {
            cursor.moveToNext()
            MatcherAssert.assertThat(cursor.getString(0), Matchers.equalTo(descriptions[i]))
        }
    }

    @Test
    fun `test migrate to 23 sets description to null`() {
        migrateTo23()
        val cursor = db.query("select description from Habits")

        while (cursor.moveToNext()) {
            MatcherAssert.assertThat(cursor.getString(0), Matchers.equalTo(""))
        }
    }
}
