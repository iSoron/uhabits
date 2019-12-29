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

package org.isoron.platform.io

interface PreparedStatement {
    fun step(): StepResult
    fun finalize()
    fun getInt(index: Int): Int
    fun getLong(index: Int): Long
    fun getText(index: Int): String
    fun getReal(index: Int): Double
    fun bindInt(index: Int, value: Int)
    fun bindLong(index: Int, value: Long)
    fun bindText(index: Int, value: String)
    fun bindReal(index: Int, value: Double)
    fun reset()
}

enum class StepResult {
    ROW,
    DONE
}

interface DatabaseOpener {
    fun open(file: UserFile): Database
}

interface Database {
    fun prepareStatement(sql: String): PreparedStatement
    fun close()
}

fun Database.run(sql: String) {
    val stmt = prepareStatement(sql)
    stmt.step()
    stmt.finalize()
}

fun Database.queryInt(sql: String): Int {
    val stmt = prepareStatement(sql)
    stmt.step()
    val result = stmt.getInt(0)
    stmt.finalize()
    return result
}

fun Database.nextId(tableName: String): Int {
    val stmt = prepareStatement("select seq from sqlite_sequence where name='$tableName'")
    if (stmt.step() == StepResult.ROW) {
        val result = stmt.getInt(0)
        stmt.finalize()
        return result + 1
    } else {
        return 0
    }
}

fun Database.begin() = run("begin")

fun Database.commit() = run("commit")

fun Database.getVersion() = queryInt("pragma user_version")

fun Database.setVersion(v: Int) = run("pragma user_version = $v")

suspend fun Database.migrateTo(newVersion: Int,
                               fileOpener: FileOpener,
                               log: Log) {
    val currentVersion = getVersion()
    log.debug("Database", "Current database version: $currentVersion")

    if (currentVersion == newVersion) return
    log.debug("Database", "Upgrading to version: $newVersion")

    if (currentVersion > newVersion)
        throw RuntimeException("database produced by future version of the application")

    begin()
    for (v in (currentVersion + 1)..newVersion) {
        val sv = if (v < 10) "00$v" else if (v < 100) "0$v" else "$v"
        val filename = "migrations/$sv.sql"
        val migrationFile = fileOpener.openResourceFile(filename)
        for (line in migrationFile.lines()) {
            if (line.isEmpty()) continue
            run(line)
        }
        setVersion(v)
    }
    commit()
}
