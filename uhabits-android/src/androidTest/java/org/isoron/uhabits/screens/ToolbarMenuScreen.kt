package org.isoron.uhabits.screens

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import com.kaspersky.kaspresso.screens.KScreen
import io.github.kakaocup.kakao.text.KButton
import io.github.kakaocup.kakao.text.KTextView
import org.isoron.uhabits.R

class ToolbarMenuScreen : KScreen<ToolbarMenuScreen>() {

    override val layoutId: Int? = null
    override val viewClass: Class<*>? = null

    private val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

    val moreOptionsButton = KButton { withContentDescription("More options") }
    val editButton = KButton { withId(R.id.action_edit_habit) }
    val filterButton = KButton { withId(R.id.action_filter) }

    val hideArchivedItem = KTextView { withText(R.string.hide_archived) }

    fun openMenu() {
        moreOptionsButton.click()
        Thread.sleep(500)
    }

    private fun clickMenuItem(text: String) {
        openMenu()
        device.wait(Until.findObject(By.text(text)), 3000)
        val item = device.findObject(By.text(text))
        item?.click()
        Thread.sleep(300)
    }

    fun clickDelete() {
        clickMenuItem("Delete")
    }

    fun clickArchive() {
        clickMenuItem("Archive")
    }

    fun clickUnarchive() {
        clickMenuItem("Unarchive")
    }

    fun toggleShowArchived() {
        // Close any existing popup first
        device.pressBack()
        Thread.sleep(300)

        filterButton.click()
        Thread.sleep(500)
        device.wait(Until.findObject(By.text("Show archived")), 3000)
        val item = device.findObject(By.text("Show archived"))
        item?.click()
        Thread.sleep(500)

        // Close popup by pressing back
        device.pressBack()
        Thread.sleep(300)
    }

    fun confirmYes() {
        device.wait(Until.findObject(By.text("Yes")), 3000)
        val btn = device.findObject(By.text("Yes"))
        btn?.click()
        Thread.sleep(300)
    }

    fun confirmNo() {
        device.wait(Until.findObject(By.text("No")), 3000)
        val btn = device.findObject(By.text("No"))
        btn?.click()
        Thread.sleep(300)
    }
}
