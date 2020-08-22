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

import androidx.test.uiautomator.*;

import static android.os.Build.VERSION.SDK_INT;
import static org.junit.Assert.*;
import static org.isoron.uhabits.BaseUserInterfaceTest.*;

public class WidgetSteps {
    public static void clickCheckmarkWidget() throws Exception {
        String view_id = "org.isoron.uhabits:id/imageView";
        device.findObject(new UiSelector().resourceId(view_id)).click();
    }

    public static void clickText(String s) throws Exception {
        UiObject object = device.findObject(new UiSelector().text(s));
        if (!object.waitForExists(1000)) {
            object = device.findObject(new UiSelector().text(s.toUpperCase()));
        }
        object.click();
    }

    public static void dragCheckmarkWidgetToHomeScreen() throws Exception {
        openWidgetScreen();
        dragWidgetToHomeScreen();
    }

    private static void dragWidgetToHomeScreen() throws Exception {
        int height = device.getDisplayHeight();
        int width = device.getDisplayWidth();
        device.findObject(new UiSelector().text("Checkmark"))
                .dragTo(width / 2, height / 2, 8);
    }

    private static void openWidgetScreen() throws Exception {
        int h = device.getDisplayHeight();
        int w = device.getDisplayWidth();
        if (SDK_INT <= 21) {
            device.pressHome();
            device.waitForIdle();
            device.findObject(new UiSelector().description("Apps")).click();
            device.findObject(new UiSelector().description("Apps")).click();
            device.findObject(new UiSelector().description("Widgets")).click();
        } else {
            String list_id = "com.android.launcher3:id/widgets_list_view";
            device.pressHome();
            device.waitForIdle();
            device.drag(w / 2, h / 2, w / 2, h / 2, 8);
            UiObject button = device.findObject(new UiSelector().text("WIDGETS"));
            if(!button.waitForExists(1000)) {
                button = device.findObject(new UiSelector().text("Widgets"));
            }
            button.click();
            if (SDK_INT >= 28) {
                new UiScrollable(new UiSelector().resourceId(list_id))
                        .scrollForward();
            }
            new UiScrollable(new UiSelector().resourceId(list_id))
                    .scrollIntoView(new UiSelector().text("Checkmark"));
        }
    }

    public static void verifyCheckmarkWidgetIsShown() throws Exception {
        String view_id = "org.isoron.uhabits:id/imageView";
        assertTrue(device.findObject(new UiSelector().resourceId(view_id)).exists());
        assertFalse(device.findObject(new UiSelector().textStartsWith("Habit deleted")).exists());
    }
}
