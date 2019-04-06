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

package org.isoron.uhabits.models

import org.isoron.platform.time.*
import org.isoron.uhabits.models.Checkmark.Companion.CHECKED_AUTOMATIC
import org.isoron.uhabits.models.Checkmark.Companion.CHECKED_MANUAL
import org.isoron.uhabits.models.Checkmark.Companion.UNCHECKED

class CheckmarkList(val frequency: Frequency,
                    val habitType: HabitType) {

    private val manualCheckmarks = mutableListOf<Checkmark>()
    private val computedCheckmarks = mutableListOf<Checkmark>()

    /**
     * Replaces the entire list of manual checkmarks by the ones provided. The
     * list of automatic checkmarks will be automatically updated.
     */
    fun setManualCheckmarks(checks: List<Checkmark>) {
        manualCheckmarks.clear()
        computedCheckmarks.clear()
        manualCheckmarks.addAll(checks)
        if (habitType == HabitType.NUMERICAL_HABIT) {
            computedCheckmarks.addAll(checks)
        } else {
            val computed = computeCheckmarks(checks, frequency)
            computedCheckmarks.addAll(computed)
        }
    }

    /**
     * Returns values of all checkmarks (manual and automatic) from the oldest
     * entry until the date provided.
     *
     * The interval is inclusive, and the list is sorted from newest to oldest.
     * That is, the first element of the returned list corresponds to the date
     * provided.
     */
    fun getUntil(date: LocalDate): List<Checkmark> {
        if (computedCheckmarks.isEmpty()) return listOf()

        val result = mutableListOf<Checkmark>()
        val newest = computedCheckmarks.first().date
        val distToNewest = newest.distanceTo(date)

        var k = 0
        var fromIndex = 0
        val toIndex = computedCheckmarks.size
        if (newest.isOlderThan(date)) {
            repeat(distToNewest) { result.add(Checkmark(date.minus(k++), UNCHECKED)) }
        } else {
            fromIndex = distToNewest
        }
        val subList = computedCheckmarks.subList(fromIndex, toIndex)
        result.addAll(subList.map { Checkmark(date.minus(k++), it.value) })
        return result
    }

    companion object {
        /**
         * Computes the list of automatic checkmarks a list of manual ones.
         */
        fun computeCheckmarks(checks: List<Checkmark>,
                              frequency: Frequency
                             ): MutableList<Checkmark> {

            val intervals = buildIntervals(checks, frequency)
            snapIntervalsTogether(intervals)
            return buildCheckmarksFromIntervals(checks, intervals)
        }

        /**
         * Modifies the intervals so that gaps between intervals are eliminated.
         *
         * More specifically, this function shifts the beginning and the end of
         * intervals so that they overlap the least as possible. The center of
         * the interval, however, still falls within the interval. The length of
         * the intervals are also not modified.
         */
        fun snapIntervalsTogether(intervals: MutableList<Interval>) {

            for (i in 1 until intervals.size) {
                val (begin, center, end) = intervals[i]
                val (_, _, prevEnd) = intervals[i - 1]

                val gap = prevEnd.distanceTo(begin) - 1
                if (gap <= 0 || end.minus(gap).isOlderThan(center)) continue
                intervals[i] = Interval(begin.minus(gap),
                                        center,
                                        end.minus(gap))
            }
        }

        /**
         * Converts a list of (manually checked) checkmarks and computed
         * intervals into a list of unchecked, manually checked and
         * automatically checked checkmarks.
         *
         * Manual checkmarks are simply copied over to the output list. Days
         * that are an interval, but which do not have manual checkmarks receive
         * automatic checkmarks. Days that fall in the gaps between intervals
         * receive unchecked checkmarks.
         */
        fun buildCheckmarksFromIntervals(checks: List<Checkmark>,
                                         intervals: List<Interval>
                                        ): MutableList<Checkmark> {

            if (checks.isEmpty()) throw IllegalArgumentException()
            if (intervals.isEmpty()) throw IllegalArgumentException()

            var oldest = intervals[0].begin
            var newest = intervals[0].end
            for (interval in intervals) {
                if (interval.begin.isOlderThan(oldest)) oldest = interval.begin
                if (interval.end.isNewerThan(newest)) newest = interval.end
            }
            for (check in checks) {
                if (check.date.isOlderThan(oldest)) oldest = check.date
                if (check.date.isNewerThan(newest)) newest = check.date
            }

            val distance = oldest.distanceTo(newest)
            val checkmarks = mutableListOf<Checkmark>()
            for (offset in 0..distance)
                checkmarks.add(Checkmark(newest.minus(offset),
                                         UNCHECKED))

            for (interval in intervals) {
                val beginOffset = newest.distanceTo(interval.begin)
                val endOffset = newest.distanceTo(interval.end)

                for (offset in endOffset..beginOffset) {
                    checkmarks.set(offset,
                                   Checkmark(newest.minus(offset),
                                             CHECKED_AUTOMATIC))
                }
            }

            for (check in checks) {
                val offset = newest.distanceTo(check.date)
                checkmarks.set(offset, Checkmark(check.date, CHECKED_MANUAL))
            }

            return checkmarks
        }

        /**
         * Constructs a list of intervals based on a list of (manual)
         * checkmarks.
         */
        fun buildIntervals(checks: List<Checkmark>,
                           frequency: Frequency): MutableList<Interval> {

            val num = frequency.numerator
            val den = frequency.denominator

            val intervals = mutableListOf<Interval>()
            for (i in 0..(checks.size - num)) {
                val first = checks[i]
                val last = checks[i + num - 1]

                if (first.date.distanceTo(last.date) >= den) continue

                val end = first.date.plus(den - 1)
                intervals.add(Interval(first.date, last.date, end))
            }

            return intervals
        }
    }

    /*
     * For non-daily habits, some groups of repetitions generate many
     * automatic checkmarks. For weekly habits, each repetition generates
     * seven checkmarks. For twice-a-week habits, two repetitions that are close
     * enough together also generate seven checkmarks. This group of generated
     * checkmarks is represented by an interval.
     *
     * The fields `begin` and `end` indicate the length of the interval, and are
     * inclusive. The field `center` indicates the newest day within the interval
     * that has a manual checkmark.
     */
    data class Interval(val begin: LocalDate,
                        val center: LocalDate,
                        val end: LocalDate)
}