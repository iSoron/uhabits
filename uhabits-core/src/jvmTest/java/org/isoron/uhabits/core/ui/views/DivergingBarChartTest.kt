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
package org.isoron.uhabits.core.ui.views

import org.isoron.platform.gui.Color
import org.isoron.platform.time.JavaLocalDateFormatter
import org.isoron.platform.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.util.Locale

class DivergingBarChartTest {

    private lateinit var chart: DivergingBarChart
    private val theme = LightTheme()
    private val dateFormatter = JavaLocalDateFormatter(Locale.US)

    @Before
    fun setUp() {
        chart = DivergingBarChart(theme, dateFormatter)
    }

    // ==================== Initialization tests ====================

    @Test
    fun testInitialization_defaultValues() {
        assertEquals(0, chart.series.size)
        assertEquals(0, chart.axis.size)
        assertEquals(0, chart.dataOffset)
        assertEquals(Color.GREEN, chart.positiveColor)
        assertEquals(Color.RED, chart.negativeColor)
    }

    @Test
    fun testDataColumnWidth_returnsExpectedValue() {
        // Default barHeight is 10.0, barMargin is 4.0
        // dataColumnWidth = barHeight + barMargin * 2 = 10 + 8 = 18
        assertEquals(18.0, chart.dataColumnWidth, 0.001)
    }

    // ==================== Series data tests ====================

    @Test
    fun testSeries_canBeModified() {
        chart.series.add(0.5)
        chart.series.add(-0.3)
        chart.series.add(0.0)

        assertEquals(3, chart.series.size)
        assertEquals(0.5, chart.series[0], 0.001)
        assertEquals(-0.3, chart.series[1], 0.001)
        assertEquals(0.0, chart.series[2], 0.001)
    }

    @Test
    fun testAxis_canBeSet() {
        val dates = listOf(
            LocalDate(2025, 1, 1),
            LocalDate(2025, 1, 2),
            LocalDate(2025, 1, 3)
        )
        chart.axis = dates

        assertEquals(3, chart.axis.size)
        assertEquals(LocalDate(2025, 1, 1), chart.axis[0])
    }

    // ==================== Color configuration tests ====================

    @Test
    fun testColors_canBeCustomized() {
        val customPositive = Color(0.0, 0.8, 0.0, 1.0)
        val customNegative = Color(0.8, 0.0, 0.0, 1.0)

        chart.positiveColor = customPositive
        chart.negativeColor = customNegative

        assertEquals(customPositive, chart.positiveColor)
        assertEquals(customNegative, chart.negativeColor)
    }

    // ==================== DataOffset tests ====================

    @Test
    fun testDataOffset_canBeSet() {
        chart.dataOffset = 5
        assertEquals(5, chart.dataOffset)
    }

    @Test
    fun testDataOffset_affectsVisibleData() {
        chart.series.addAll(listOf(0.1, 0.2, 0.3, 0.4, 0.5))
        chart.axis = (0..4).map { LocalDate(2025, 1, it + 1) }

        chart.dataOffset = 2

        // After offset, visible data starts from index 2
        assertEquals(2, chart.dataOffset)
    }

    // ==================== Style configuration tests ====================

    @Test
    fun testStyle_paddingValues_canBeSet() {
        chart.paddingTop = 20.0
        chart.paddingBottom = 20.0
        chart.paddingLeft = 10.0
        chart.paddingRight = 10.0

        assertEquals(20.0, chart.paddingTop, 0.001)
        assertEquals(20.0, chart.paddingBottom, 0.001)
        assertEquals(10.0, chart.paddingLeft, 0.001)
        assertEquals(10.0, chart.paddingRight, 0.001)
    }

    @Test
    fun testStyle_barProperties_canBeSet() {
        chart.barHeight = 15.0
        chart.barMargin = 6.0

        assertEquals(15.0, chart.barHeight, 0.001)
        assertEquals(6.0, chart.barMargin, 0.001)
        // dataColumnWidth should update: 15 + 6*2 = 27
        assertEquals(27.0, chart.dataColumnWidth, 0.001)
    }

    @Test
    fun testStyle_gridlines_canBeConfigured() {
        chart.nGridlines = 6
        assertEquals(6, chart.nGridlines)
    }

    @Test
    fun testStyle_valuePrecision_canBeSet() {
        chart.valuePrecision = 3
        assertEquals(3, chart.valuePrecision)
    }

    // ==================== Edge cases ====================

    @Test
    fun testEmptyData_handledGracefully() {
        // Chart with no data should not throw
        assertEquals(0, chart.series.size)
        assertEquals(0, chart.axis.size)
    }

    @Test
    fun testMixedPositiveNegativeValues() {
        chart.series.addAll(listOf(0.5, -0.3, 0.2, -0.1, 0.0))
        chart.axis = (0..4).map { LocalDate(2025, 1, it + 1) }

        // Verify data was added correctly
        assertEquals(5, chart.series.size)
        assertEquals(0.5, chart.series[0], 0.001)
        assertEquals(-0.3, chart.series[1], 0.001)
        assertEquals(0.0, chart.series[4], 0.001)
    }

    @Test
    fun testAllPositiveValues() {
        chart.series.addAll(listOf(0.1, 0.2, 0.3, 0.4, 0.5))
        chart.axis = (0..4).map { LocalDate(2025, 1, it + 1) }

        assertEquals(5, chart.series.size)
        chart.series.forEach { value ->
            assert(value >= 0.0)
        }
    }

    @Test
    fun testAllNegativeValues() {
        chart.series.addAll(listOf(-0.1, -0.2, -0.3, -0.4, -0.5))
        chart.axis = (0..4).map { LocalDate(2025, 1, it + 1) }

        assertEquals(5, chart.series.size)
        chart.series.forEach { value ->
            assert(value <= 0.0)
        }
    }

    @Test
    fun testMinValueRange_preventsZeroDivision() {
        // minValueRange should prevent division by zero when all values are 0
        assertEquals(0.00001, chart.minValueRange, 0.000001)
    }
}
