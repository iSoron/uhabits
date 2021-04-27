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
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiSelector
import org.isoron.uhabits.BaseUserInterfaceTest.Companion.device
import org.isoron.uhabits.acceptance.steps.CommonSteps.clickText
import org.isoron.uhabits.acceptance.steps.CommonSteps.pressBack
import org.isoron.uhabits.acceptance.steps.ListHabitsSteps.MenuItem.SETTINGS
import org.isoron.uhabits.acceptance.steps.ListHabitsSteps.clickMenu

const val BACKUP_FOLDER = "/sdcard/Android/data/org.isoron.uhabits/files/Backups/"
const val DOWNLOAD_FOLDER = "/sdcard/Download/"

fun exportFullBackup() {
    clickMenu(SETTINGS)
    clickText("Export full backup")
    if (SDK_INT < 28) return
    pressBack()
}

fun clearDownloadFolder() {
    device.executeShellCommand("rm -rf /sdcard/Download/")
}

fun clearBackupFolder() {
    device.executeShellCommand("rm -rf $BACKUP_FOLDER")
}

fun copyBackupToDownloadFolder() {
    device.executeShellCommand("mv $BACKUP_FOLDER $DOWNLOAD_FOLDER")
    device.executeShellCommand("chown root $DOWNLOAD_FOLDER")
}

fun importBackupFromDownloadFolder() {
    clickMenu(SETTINGS)
    clickText("Import data")
    if (SDK_INT <= 23) {
        while (!device.hasObject(By.textContains("Show file size"))) {
            device.click(720, 100) // Click overflow menu
            Thread.sleep(1000)
        }
        if (device.hasObject(By.textContains("Show SD card"))) {
            device.findObject(UiSelector().textContains("Show SD card")).click()
            Thread.sleep(1000)
        } else {
            device.pressBack()
        }
        device.click(50, 90) // Click menu button
        device.findObject(UiSelector().textContains("Internal storage")).click()
        device.findObject(UiSelector().textContains("Download")).click()
        device.findObject(UiSelector().textContains("Loop")).click()
    } else if (SDK_INT <= 25) {
        while (!device.hasObject(By.textContains("Show file size"))) {
            device.click(720, 100) // Click overflow menu
            Thread.sleep(1000)
        }
        if (device.hasObject(By.textContains("Show internal"))) {
            device.findObject(UiSelector().textContains("Show internal")).click()
            Thread.sleep(1000)
        } else {
            device.pressBack()
        }
        device.click(50, 90) // Click menu button
        device.findObject(UiSelector().textContains("Android")).click()
        device.findObject(UiSelector().textContains("Download")).click()
        device.findObject(UiSelector().textContains("Loop")).click()
    } else {
        device.click(50, 90) // Click menu button
        device.findObject(UiSelector().textContains("Download")).click()
        device.findObject(UiSelector().textContains("Loop")).click()
    }
}

fun openLauncher() {
    device.pressHome()
    device.waitForIdle()
    val h = device.displayHeight
    val w = device.displayWidth
    device.swipe(w / 2, h / 2, w / 2, 0, 8)
}
