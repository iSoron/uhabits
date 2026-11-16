/*
 * Copyright (C) 2016-2025 Álinson Santos Xavier <git@axavier.org>
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

package org.isoron.uhabits.provider

import android.content.ContentProvider
import android.content.ContentUris
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import org.isoron.platform.time.LocalDate
import org.isoron.platform.time.getToday
import org.isoron.uhabits.HabitsApplication
import org.isoron.uhabits.core.models.Entry
import org.isoron.uhabits.core.models.Habit

/**
 * Read-only ContentProvider exposing habits, scores, streaks, and recent entries
 * to external apps holding [org.isoron.uhabits.permission.READ_HABITS].
 */
class HabitsContentProvider : ContentProvider() {

    override fun onCreate(): Boolean = true

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? {
        val app = context?.applicationContext as? HabitsApplication ?: return null
        val habitList = app.component.habitList
        val today = getToday()

        return when (uriMatcher.match(uri)) {
            CODE_HABITS -> {
                val includeArchived = uri.getBooleanQueryParameter("include_archived", false)
                val cursor = MatrixCursor(HABIT_COLUMNS)
                for (habit in habitList) {
                    if (!includeArchived && habit.isArchived) continue
                    cursor.addRow(habitRow(habit, today))
                }
                cursor
            }

            CODE_HABIT_ID -> {
                val id = ContentUris.parseId(uri)
                val habit = habitList.getById(id) ?: return MatrixCursor(HABIT_COLUMNS)
                MatrixCursor(HABIT_COLUMNS).apply {
                    addRow(habitRow(habit, today))
                }
            }

            CODE_HABIT_ENTRIES -> {
                val segments = uri.pathSegments
                val id = segments.getOrNull(1)?.toLongOrNull() ?: return null
                val habit = habitList.getById(id) ?: return MatrixCursor(ENTRY_COLUMNS)
                val days = uri.getQueryParameter("days")?.toIntOrNull()?.coerceIn(1, 365) ?: 30
                val from = today.minus(days - 1)
                val entries = habit.computedEntries.getByInterval(from, today)
                val cursor = MatrixCursor(ENTRY_COLUMNS)
                for (entry in entries) {
                    cursor.addRow(
                        arrayOf<Any?>(
                            entry.date.unixTime,
                            entry.value,
                            if (entry.completed) 1 else 0,
                            entry.notes
                        )
                    )
                }
                cursor
            }

            else -> throw IllegalArgumentException("Unknown URI: $uri")
        }
    }

    override fun getType(uri: Uri): String? = when (uriMatcher.match(uri)) {
        CODE_HABITS -> "vnd.android.cursor.dir/vnd.$AUTHORITY.habits"
        CODE_HABIT_ID -> "vnd.android.cursor.item/vnd.$AUTHORITY.habits"
        CODE_HABIT_ENTRIES -> "vnd.android.cursor.dir/vnd.$AUTHORITY.entries"
        else -> null
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? = null
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0
    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int = 0

    private fun habitRow(habit: Habit, today: LocalDate): Array<Any?> {
        val todayEntry = habit.computedEntries.get(today)
        val score = habit.scores[today].value
        val bestStreak = habit.streaks.getBest(1).firstOrNull()?.length ?: 0
        val currentStreak = computeCurrentStreak(habit, today)
        val todayStatus = resolveTodayStatus(habit, todayEntry)
        return arrayOf(
            habit.id ?: -1L,
            habit.name,
            habit.question,
            habit.description,
            if (habit.isNumerical) "NUMERICAL" else "BOOLEAN",
            habit.targetValue,
            habit.unit,
            habit.color.paletteIndex,
            if (habit.isArchived) 1 else 0,
            score,
            currentStreak,
            bestStreak,
            todayEntry.value,
            if (todayEntry.completed) 1 else 0,
            todayStatus
        )
    }

    private fun resolveTodayStatus(habit: Habit, entry: Entry): String {
        if (entry.completed) return "COMPLETED"
        if (habit.isNumerical) {
            return if (entry.value == Entry.UNKNOWN) "UNKNOWN" else "MISSED"
        }
        return when (entry.value) {
            Entry.YES_MANUAL, Entry.YES_AUTO -> "COMPLETED"
            Entry.SKIP -> "SKIPPED"
            Entry.NO -> "MISSED"
            Entry.UNKNOWN -> "UNKNOWN"
            else -> "UNKNOWN"
        }
    }

    private fun computeCurrentStreak(habit: Habit, today: LocalDate): Int {
        var streak = 0
        var day = if (habit.isCompletedOn(today)) today else today.minus(1)
        while (streak < 3650 && habit.isCompletedOn(day)) {
            streak += 1
            day = day.minus(1)
        }
        return streak
    }

    companion object {
        const val AUTHORITY = "org.isoron.uhabits.provider"

        private const val CODE_HABITS = 1
        private const val CODE_HABIT_ID = 2
        private const val CODE_HABIT_ENTRIES = 3

        private val uriMatcher = UriMatcher(UriMatcher.NO_MATCH).apply {
            addURI(AUTHORITY, "habits", CODE_HABITS)
            addURI(AUTHORITY, "habits/#", CODE_HABIT_ID)
            addURI(AUTHORITY, "habits/#/entries", CODE_HABIT_ENTRIES)
        }

        private val HABIT_COLUMNS = arrayOf(
            "_id",
            "name",
            "question",
            "description",
            "type",
            "target_value",
            "unit",
            "color",
            "archived",
            "score",
            "current_streak",
            "best_streak",
            "today_value",
            "today_completed",
            "today_status"
        )

        private val ENTRY_COLUMNS = arrayOf(
            "timestamp",
            "value",
            "completed",
            "notes"
        )
    }
}
