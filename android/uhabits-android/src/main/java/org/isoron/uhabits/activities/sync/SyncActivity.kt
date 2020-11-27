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
import android.view.*
import com.google.zxing.*
import com.google.zxing.qrcode.*
import kotlinx.coroutines.*
import org.isoron.androidbase.activities.*
import org.isoron.androidbase.utils.*
import org.isoron.androidbase.utils.InterfaceUtils.getFontAwesome
import org.isoron.uhabits.*
import org.isoron.uhabits.core.preferences.*
import org.isoron.uhabits.core.tasks.*
import org.isoron.uhabits.databinding.*
import org.isoron.uhabits.sync.*
import org.isoron.uhabits.utils.*


class SyncActivity : BaseActivity() {

    private lateinit var syncManager: SyncManager
    private lateinit var preferences: Preferences
    private lateinit var taskRunner: TaskRunner
    private lateinit var baseScreen: BaseScreen
    private lateinit var binding: ActivitySyncBinding

    private var styledResources = StyledResources(this)

    override fun onCreate(state: Bundle?) {
        super.onCreate(state)

        baseScreen = BaseScreen(this)

        val component = (application as HabitsApplication).component
        taskRunner = component.taskRunner
        preferences = component.preferences
        syncManager = component.syncManager

        binding = ActivitySyncBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.errorIcon.typeface = getFontAwesome(this)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.elevation = 10.0f

        binding.instructions.setText(Html.fromHtml(resources.getString(R.string.sync_instructions)))

        binding.syncLink.setOnClickListener {
            copyToClipboard()
        }
    }

    override fun onResume() {
        super.onResume()
        if(preferences.syncKey.isBlank()) {
            register()
        } else {
            displayCurrentKey()
        }
    }

    private fun displayCurrentKey() {
        displayLink("https://loophabits.org/sync/${preferences.syncKey}#${preferences.encryptionKey}")
        displayPassword("6B2W9F5X")
    }

    private fun register() {
        displayLoading()
        taskRunner.execute(object : Task {
            private lateinit var encKey: EncryptionKey
            private lateinit var syncKey: String
            private var error = false
            override fun doInBackground() {
                runBlocking {
                    val server = RemoteSyncServer(baseURL = preferences.syncBaseURL)
                    try {
                        syncKey = server.register()
                        encKey = EncryptionKey.generate()
                        preferences.enableSync(syncKey, encKey.base64)
                    } catch (e: ServiceUnavailable) {
                        error = true
                    }
                }
            }

            override fun onPostExecute() {
                if (error) {
                    displayError()
                    return;
                }
                displayCurrentKey()
            }
        })
    }

    private fun displayLoading() {
        binding.qrCode.visibility = View.GONE
        binding.progress.visibility = View.VISIBLE
        binding.errorPanel.visibility = View.GONE
    }

    private fun displayError() {
        binding.qrCode.visibility = View.GONE
        binding.progress.visibility = View.GONE
        binding.errorPanel.visibility = View.VISIBLE
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
        binding.qrCode.visibility = View.GONE
        binding.progress.visibility = View.VISIBLE
        binding.errorPanel.visibility = View.GONE
        binding.syncLink.text = link
        displayQR(link)
    }

    private fun displayQR(msg: String) {
        taskRunner.execute(object : Task {
            lateinit var bitmap: Bitmap
            override fun doInBackground() {
                val writer = QRCodeWriter()
                val matrix = writer.encode(msg, BarcodeFormat.QR_CODE, 1024, 1024)
                val height = matrix.height
                val width = matrix.width
                bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
                val bgColor = styledResources.getColor(R.attr.highContrastReverseTextColor)
                val fgColor = styledResources.getColor(R.attr.highContrastTextColor)
                for (x in 0 until width) {
                    for (y in 0 until height) {
                        val color = if (matrix.get(x, y)) fgColor else bgColor
                        bitmap.setPixel(x, y, color)
                    }
                }
            }
            override fun onPostExecute() {
                binding.progress.visibility = View.GONE
                binding.qrCode.visibility = View.VISIBLE
                binding.qrCode.setImageBitmap(bitmap)
            }
        })
    }
}