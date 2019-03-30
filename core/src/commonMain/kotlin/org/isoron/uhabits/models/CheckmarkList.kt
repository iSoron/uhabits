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

class CheckmarkList(private val frequency: Frequency,
                    private val dateCalculator: LocalDateCalculator) {

    private val manualCheckmarks = mutableListOf<Checkmark>()
    private val automaticCheckmarks = mutableListOf<Checkmark>()

    /**
     * Replaces the entire list of manual checkmarks by the ones provided. The
     * list of automatic checkmarks will be automatically updated.
     */
    fun setManualCheckmarks(checks: List<Checkmark>) {
        manualCheckmarks.clear()
        automaticCheckmarks.clear()
        manualCheckmarks.addAll(checks)
        automaticCheckmarks.addAll(computeAutomaticCheckmarks(checks,
                                                              frequency,
                                                              dateCalculator))
    }

    /**
     * Returns values of all checkmarks (manual and automatic) from the oldest
     * entry until the date provided.
     *
     * The interval is inclusive, and the list is sorted from newest to oldest.
     * That is, the first element of the returned list corresponds to the date
     * provided.
     */
    fun getValuesUntil(date: LocalDate): List<Int> {
        if (automaticCheckmarks.isEmpty()) return listOf()

        val result = mutableListOf<Int>()
        val newest = automaticCheckmarks.first().date
        val distToNewest = dateCalculator.distanceInDays(newest, date)

        var fromIndex = 0
        val toIndex = automaticCheckmarks.size
        if (newest.isOlderThan(date)) {
            repeat(distToNewest) { result.add(UNCHECKED) }
        } else {
            fromIndex = distToNewest
        }
        val subList = automaticCheckmarks.subList(fromIndex, toIndex)
        result.addAll(subList.map { it.value })
        return result
    }

    companion object {
        /**
         * Computes the list of automatic checkmarks a list of manual ones.
         */
        fun computeAutomaticCheckmarks(checks: List<Checkmark>,
                                       frequency: Frequency,
                                       calc: LocalDateCalculator
                                      ): MutableList<Checkmark> {

            val intervals = buildIntervals(checks, frequency, calc)
            snapIntervalsTogether(intervals, calc)
            return buildCheckmarksFromIntervals(checks, intervals, calc)
        }

        /**
         * Modifies the intervals so that gaps between intervals are eliminated.
         *
         * More specifically, this function shifts the beginning and the end of
         * intervals so that they overlap the least as possible. The center of
         * the interval, however, still falls within the interval. The length of
         * the intervals are also not modified.
         */
        fun snapIntervalsTogether(intervals: MutableList<Interval>,
                                  calc: LocalDateCalculator) {

            for (i in 1 until intervals.size) {
                val (begin, center, end) = intervals[i]
                val (_, _, prevEnd) = intervals[i - 1]

                val gap = calc.distanceInDays(prevEnd, begin) - 1
                val endMinusGap = calc.minusDays(end, gap)
                if (gap <= 0 || endMinusGap.isOlderThan(center)) continue
                intervals[i] = Interval(calc.minusDays(begin, gap),
                                        center,
                                        calc.minusDays(end, gap))
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
                                         intervals: List<Interval>,
                                         calc: LocalDateCalculator
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

            val distance = calc.distanceInDays(oldest, newest)
            val checkmarks = mutableListOf<Checkmark>()
            for (offset in 0..distance)
                checkmarks.add(Checkmark(calc.minusDays(newest, offset),
                                         UNCHECKED))

            for (interval in intervals) {
                val beginOffset = calc.distanceInDays(newest, interval.begin)
                val endOffset = calc.distanceInDays(newest, interval.end)

                for (offset in endOffset..beginOffset) {
                    checkmarks.set(offset,
                                   Checkmark(calc.minusDays(newest, offset),
                                             CHECKED_AUTOMATIC))
                }
            }

            for (check in checks) {
                val offset = calc.distanceInDays(newest, check.date)
                checkmarks.set(offset, Checkmark(check.date, CHECKED_MANUAL))
            }

            return checkmarks
        }

        /**
         * Constructs a list of intervals based on a list of (manual)
         * checkmarks.
         */
        fun buildIntervals(checks: List<Checkmark>,
                           frequency: Frequency,
                           calc: LocalDateCalculator): MutableList<Interval> {

            val num = frequency.numerator
            val den = frequency.denominator

            val intervals = mutableListOf<Interval>()
            for (i in 0..(checks.size - num)) {
                val first = checks[i]
                val last = checks[i + num - 1]

                val distance = calc.distanceInDays(first.date, last.date)
                if (distance >= den) continue

                val end = calc.plusDays(first.date, den - 1)
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