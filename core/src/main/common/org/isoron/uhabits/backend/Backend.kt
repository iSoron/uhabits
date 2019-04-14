/*
 * Copyright (C) 2016-2019 Álinson Santos Xavier <isoron@gmail.com>
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

package org.isoron.uhabits.backend

import kotlinx.coroutines.*
import org.isoron.platform.concurrency.*
import org.isoron.platform.io.*
import org.isoron.uhabits.*
import org.isoron.uhabits.components.*
import org.isoron.uhabits.i18n.*
import org.isoron.uhabits.models.*
import kotlin.coroutines.*


open class BackendScope(private val ctx: CoroutineContext,
                        private val log: Log) : CoroutineScope {

    private val job = Job()

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        log.info("Coroutine", throwable.toString())
    }

    override val coroutineContext: CoroutineContext
        get() = ctx + job + exceptionHandler
}

class Backend(private val databaseName: String,
              private val databaseOpener: DatabaseOpener,
              private val fileOpener: FileOpener,
              private val localeHelper: LocaleHelper,
              private val log: Log,
              private val scope: CoroutineContext
             ) : CoroutineScope by BackendScope(scope, log) {


    private lateinit var database: Database
    private lateinit var habitsRepository: HabitRepository
    private lateinit var checkmarkRepository: CheckmarkRepository
    lateinit var preferences: Preferences

    lateinit var mainScreenDataSource: MainScreenDataSource

    private val habits = mutableMapOf<Int, Habit>()
    private val checkmarks = mutableMapOf<Habit, CheckmarkList>()
    private val scores = mutableMapOf<Habit, ScoreList>()

    var strings = localeHelper.getStringsForCurrentLocale()
    var theme: Theme = LightTheme()

    val observable = Observable<Listener>()

    fun init() {
        launch {
            initDatabase()
            initRepositories()
            initDataSources()
            observable.notifyListeners { it.onReady() }
        }
    }

    private fun initRepositories() {
        preferences = Preferences(PreferencesRepository(database))
        habitsRepository = HabitRepository(database)
        checkmarkRepository = CheckmarkRepository(database)
        habits.putAll(habitsRepository.findAll())
        log.info("Backend", "${habits.size} habits loaded")
        for ((key, habit) in habits) {
            val checks = checkmarkRepository.findAll(key)
            checkmarks[habit] = CheckmarkList(habit.frequency, habit.type)
            checkmarks[habit]?.setManualCheckmarks(checks)
            scores[habit] = ScoreList(checkmarks[habit]!!)
        }
    }

    private fun initDataSources() {
        mainScreenDataSource =
                MainScreenDataSource(preferences, habits, checkmarks, scores)
    }

    private suspend fun initDatabase() {
        val dbFile = fileOpener.openUserFile(databaseName)
        if (!dbFile.exists()) {
            val templateFile = fileOpener.openResourceFile("databases/template.db")
            templateFile.copyTo(dbFile)
        }
        database = databaseOpener.open(dbFile)
        database.migrateTo(LOOP_DATABASE_VERSION, fileOpener, log)
    }

    fun createHabit(habit: Habit) {
        val id = habitsRepository.nextId()
        habit.id = id
        habit.position = habits.size
        habits[id] = habit
        checkmarks[habit] = CheckmarkList(habit.frequency, habit.type)
        habitsRepository.insert(habit)
        mainScreenDataSource.requestData()
    }

    fun deleteHabit(id: Int) {
        habits[id]?.let { habit ->
            habitsRepository.delete(habit)
            habits.remove(id)
            mainScreenDataSource.requestData()
        }
    }

    fun updateHabit(modified: Habit) {
        habits[modified.id]?.let { existing ->
            modified.position = existing.position
            habitsRepository.update(modified)
        }
    }

    interface Listener {
        fun onReady()
    }
}
