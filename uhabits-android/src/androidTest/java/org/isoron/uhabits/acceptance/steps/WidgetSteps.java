/*
 * Copyright (C) 2017 Álinson Santos Xavier <isoron@gmail.com>
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

package org.isoron.uhabits.acceptance.steps;

import android.support.test.uiautomator.*;

import static junit.framework.Assert.*;
import static org.isoron.uhabits.BaseUserInterfaceTest.*;

public class WidgetSteps
{
    public static void clickCheckmarkWidget() throws Exception
    {
        device
            .findObject(
                new UiSelector().resourceId("org.isoron.uhabits:id/imageView"))
            .click();
    }

    public static void clickText(String s) throws Exception
    {
        device.findObject(new UiSelector().text(s)).click();
    }

    public static void clickWidgets() throws Exception
    {
        device.findObject(new UiSelector().text("WIDGETS")).click();
    }

    public static void dragWidgetToHomescreen() throws Exception
    {
        int height = device.getDisplayHeight();
        int width = device.getDisplayWidth();
        device
            .findObject(new UiSelector().text("Checkmark"))
            .dragTo(width / 2, height / 2, 8);
    }

    public static void longPressHomeScreen() throws Exception
    {
        device.pressHome();
        device.waitForIdle();
        device
            .findObject(new UiSelector().resourceId(
                "com.google.android.apps.nexuslauncher:id/workspace"))
            .longClick();
    }

    public static void scrollToHabits() throws Exception
    {
        new UiScrollable(new UiSelector().resourceId(
            "com.google.android.apps.nexuslauncher:id/widgets_list_view")).scrollIntoView(
            new UiSelector().text("Habits"));

    }

    public static void verifyCheckmarkWidgetIsShown() throws Exception
    {
        assertTrue(device
            .findObject(
                new UiSelector().resourceId("org.isoron.uhabits:id/imageView"))
            .exists());

        assertFalse(device
            .findObject(new UiSelector().textStartsWith("Habit deleted"))
            .exists());
    }
}
