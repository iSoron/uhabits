/*
 * Copyright (C) 2016-2021 Álinson Santos Xavier <git@axavier.org>
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
package org.isoron.uhabits.acceptance.steps

import android.os.Build.VERSION.SDK_INT
import androidx.test.uiautomator.UiScrollable
import androidx.test.uiautomator.UiSelector
import junit.framework.Assert.assertFalse
import junit.framework.Assert.assertTrue
import org.isoron.uhabits.BaseUserInterfaceTest

object WidgetSteps {
    @Throws(Exception::class)
    fun clickCheckmarkWidget() {
        val viewId = "org.isoron.uhabits:id/imageView"
        BaseUserInterfaceTest.device.findObject(UiSelector().resourceId(viewId)).click()
    }

    @Throws(Exception::class)
    fun dragCheckmarkWidgetToHomeScreen() {
        openWidgetScreen()
        dragWidgetToHomeScreen()
    }

    @Throws(Exception::class)
    private fun dragWidgetToHomeScreen() {
        val height = BaseUserInterfaceTest.device.displayHeight
        val width = BaseUserInterfaceTest.device.displayWidth
        BaseUserInterfaceTest.device.findObject(UiSelector().text("Checkmark"))
            .dragTo(width / 2, height / 2, 40)
    }

    @Throws(Exception::class)
    private fun openWidgetScreen() {
        val h = BaseUserInterfaceTest.device.displayHeight
        val w = BaseUserInterfaceTest.device.displayWidth
        val listId = "com.android.launcher3:id/widgets_list_view"
        BaseUserInterfaceTest.device.pressHome()
        BaseUserInterfaceTest.device.waitForIdle()
        BaseUserInterfaceTest.device.drag(w / 2, h / 2, w / 2, h / 2, 8)
        var button = BaseUserInterfaceTest.device.findObject(UiSelector().text("WIDGETS"))
        if (!button.waitForExists(1000)) {
            button = BaseUserInterfaceTest.device.findObject(UiSelector().text("Widgets"))
        }
        button.click()
        if (SDK_INT >= 28) {
            UiScrollable(UiSelector().resourceId(listId))
                .scrollForward()
        }
        UiScrollable(UiSelector().resourceId(listId))
            .scrollIntoView(UiSelector().text("Checkmark"))
    }

    @Throws(Exception::class)
    fun verifyCheckmarkWidgetIsShown() {
        val viewId = "org.isoron.uhabits:id/imageView"
        assertTrue(
            BaseUserInterfaceTest.device.findObject(UiSelector().resourceId(viewId)).exists()
        )
        assertFalse(
            BaseUserInterfaceTest.device.findObject(UiSelector().textStartsWith("Habit deleted"))
                .exists()
        )
    }
}
