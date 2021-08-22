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

package org.isoron.uhabits.activities.sync

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.text.Html
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.invoke
import kotlinx.coroutines.launch
import org.isoron.uhabits.HabitsApplication
import org.isoron.uhabits.R
import org.isoron.uhabits.activities.AndroidThemeSwitcher
import org.isoron.uhabits.core.models.PaletteColor
import org.isoron.uhabits.core.ui.screens.sync.SyncBehavior
import org.isoron.uhabits.databinding.ActivitySyncBinding
import org.isoron.uhabits.sync.RemoteSyncServer
import org.isoron.uhabits.utils.InterfaceUtils.getFontAwesome
import org.isoron.uhabits.utils.setupToolbar
import org.isoron.uhabits.utils.showMessage

class SyncActivity : AppCompatActivity(), SyncBehavior.Screen {

    private lateinit var binding: ActivitySyncBinding
    private lateinit var behavior: SyncBehavior

    private val scope = CoroutineScope(Dispatchers.Main)

    override fun onCreate(savedInstance: Bundle?) {
        super.onCreate(savedInstance)
        val component = (application as HabitsApplication).component
        val preferences = component.preferences
        val server = RemoteSyncServer(preferences = preferences)
        val themeSwitcher = AndroidThemeSwitcher(this, component.preferences)
        themeSwitcher.apply()

        behavior = SyncBehavior(this, preferences, server, component.logging)
        binding = ActivitySyncBinding.inflate(layoutInflater)
        binding.errorIcon.typeface = getFontAwesome(this)
        binding.root.setupToolbar(
            toolbar = binding.toolbar,
            color = PaletteColor(11),
            title = resources.getString(R.string.device_sync),
            theme = themeSwitcher.currentTheme,
        )
        binding.syncLink.setOnClickListener { copyToClipboard() }
        binding.instructions.text = Html.fromHtml(resources.getString(R.string.sync_instructions))
        setContentView(binding.root)
    }

    override fun onResume() {
        super.onResume()
        scope.launch {
            behavior.onResume()
        }
    }

    private fun copyToClipboard() {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("Loop Sync Link", binding.syncLink.text))
        showMessage(resources.getString(R.string.copied_to_the_clipboard))
    }

    suspend fun generateQR(msg: String): Bitmap = Dispatchers.IO {
        val writer = QRCodeWriter()
        val matrix = writer.encode(msg, BarcodeFormat.QR_CODE, 1024, 1024)
        val height = matrix.height
        val width = matrix.width
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        val bgColor = Color.WHITE
        val fgColor = Color.BLACK
        for (x in 0 until width) {
            for (y in 0 until height) {
                val color = if (matrix.get(x, y)) fgColor else bgColor
                bitmap.setPixel(x, y, color)
            }
        }
        return@IO bitmap
    }

    suspend fun showQR(msg: String) {
        binding.progress.visibility = View.GONE
        binding.qrCode.visibility = View.VISIBLE
        binding.qrCode.setImageBitmap(generateQR(msg))
    }

    override suspend fun showLoadingScreen() {
        binding.qrCode.visibility = View.GONE
        binding.progress.visibility = View.VISIBLE
        binding.errorPanel.visibility = View.GONE
    }

    override suspend fun showErrorScreen() {
        binding.qrCode.visibility = View.GONE
        binding.progress.visibility = View.GONE
        binding.errorPanel.visibility = View.VISIBLE
    }

    override suspend fun showLink(link: String) {
        binding.qrCode.visibility = View.GONE
        binding.progress.visibility = View.VISIBLE
        binding.errorPanel.visibility = View.GONE
        binding.syncLink.text = link
        showQR(link)
    }
}
