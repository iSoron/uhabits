/*
 * Copyright (C) 2016-2020 Álinson Santos Xavier <isoron@gmail.com>
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

package org.isoron.uhabits.activities.sync

import android.content.*
import android.content.ClipboardManager
import android.graphics.*
import android.os.*
import android.text.*
import com.google.zxing.*
import com.google.zxing.qrcode.*
import org.isoron.androidbase.activities.*
import org.isoron.androidbase.utils.*
import org.isoron.uhabits.*
import org.isoron.uhabits.activities.*
import org.isoron.uhabits.databinding.*


class SyncActivity : BaseActivity() {

    private lateinit var baseScreen: BaseScreen
    private lateinit var themeSwitcher: AndroidThemeSwitcher
    private lateinit var binding: ActivitySyncBinding

    override fun onCreate(state: Bundle?) {
        super.onCreate(state)

        baseScreen = BaseScreen(this)

        val component = (application as HabitsApplication).component
        themeSwitcher = AndroidThemeSwitcher(this, component.preferences)
        themeSwitcher.apply()

        binding = ActivitySyncBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.elevation = 10.0f

        binding.instructions.setText(Html.fromHtml(resources.getString(R.string.sync_instructions)))

        displayLink("https://loophabits.org/sync/KA9GvblSWrcLk9iwJrplHvWiWdE6opAokdf2qqRl6n6ECX8IUhvcksqlfkQACoMM")
        displayPassword("6B2W9F5X")

        binding.syncLink.setOnClickListener {
            copyToClipboard()
        }
    }

    private fun copyToClipboard() {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("Loop Sync Link", binding.syncLink.text))
        baseScreen.showMessage(R.string.copied_to_the_clipboard, binding.root)
    }

    private fun displayPassword(pin: String) {
        binding.password.text = pin
    }

    private fun displayLink(link: String) {
        binding.syncLink.text = link
        displayQR(link)
    }

    private fun displayQR(msg: String) {
        val writer = QRCodeWriter()
        val matrix = writer.encode(msg, BarcodeFormat.QR_CODE, 1024, 1024)
        val height = matrix.height
        val width = matrix.width
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        val bgColor = StyledResources(this).getColor(R.attr.highContrastReverseTextColor)
        val fgColor = StyledResources(this).getColor(R.attr.highContrastTextColor)
        for (x in 0 until width) {
            for (y in 0 until height) {
                val color = if (matrix.get(x, y)) fgColor else bgColor
                bitmap.setPixel(x, y, color)
            }
        }
        binding.qrCode.setImageBitmap(bitmap)
    }
}