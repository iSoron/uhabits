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

import org.isoron.platform.concurrency.*
import org.isoron.platform.io.*
import org.isoron.uhabits.*
import org.isoron.uhabits.components.*
import org.isoron.uhabits.i18n.*
import org.isoron.uhabits.models.*

//class Backend(databaseName: String,
//              databaseOpener: DatabaseOpener,
//              fileOpener: FileOpener,
//              localeHelper: LocaleHelper,
//              val log: Log,
//              val taskRunner: TaskRunner) {
//
//    val database: Database
//
//    val habitsRepository: HabitRepository
//
//    val checkmarkRepository: CheckmarkRepository
//
//    val habits = mutableMapOf<Int, Habit>()
//
//    val checkmarks = mutableMapOf<Habit, CheckmarkList>()
//
//    val scores = mutableMapOf<Habit, ScoreList>()
//
//    val mainScreenDataSource: MainScreenDataSource
//
//    val strings = localeHelper.getStringsForCurrentLocale()
//
//    val preferences: Preferences
//
//    var theme: Theme = LightTheme()
//
//    init {
//        val dbFile = fileOpener.openUserFile(databaseName)
//        if (!dbFile.exists()) {
//            val templateFile = fileOpener.openResourceFile("databases/template.db")
//            templateFile.copyTo(dbFile)
//        }
//        database = databaseOpener.open(dbFile)
//        database.migrateTo(LOOP_DATABASE_VERSION, fileOpener, log)
//        preferences = Preferences(PreferencesRepository(database))
//        habitsRepository = HabitRepository(database)
//        checkmarkRepository = CheckmarkRepository(database)
//        taskRunner.runInBackground {
//            habits.putAll(habitsRepository.findAll())
//            for ((key, habit) in habits) {
//                val checks = checkmarkRepository.findAll(key)
//                checkmarks[habit] = CheckmarkList(habit.frequency, habit.type)
//                checkmarks[habit]?.setManualCheckmarks(checks)
//                scores[habit] = ScoreList(checkmarks[habit]!!)
//            }
//        }
//        mainScreenDataSource = MainScreenDataSource(preferences,
//                                                    habits,
//                                                    checkmarks,
//                                                    scores,
//                                                    taskRunner)
//    }
//
//    fun createHabit(habit: Habit) {
//        val id = habitsRepository.nextId()
//        habit.id = id
//        habit.position = habits.size
//        habits[id] = habit
//        checkmarks[habit] = CheckmarkList(habit.frequency, habit.type)
//        habitsRepository.insert(habit)
//        mainScreenDataSource.requestData()
//    }
//
//    fun deleteHabit(id: Int) {
//        habits[id]?.let { habit ->
//            habitsRepository.delete(habit)
//            habits.remove(id)
//            mainScreenDataSource.requestData()
//        }
//    }
//
//    fun updateHabit(modified: Habit) {
//        habits[modified.id]?.let { existing ->
//            modified.position = existing.position
//            habitsRepository.update(modified)
//        }
//    }
//}
