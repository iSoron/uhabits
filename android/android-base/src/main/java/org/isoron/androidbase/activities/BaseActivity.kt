/*
 * Copyright (C) 2016 Álinson Santos Xavier <isoron@gmail.com>
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
package org.isoron.androidbase.activities

import android.R.anim
import android.content.*
import android.os.*
import android.view.*
import androidx.appcompat.app.*
import org.isoron.androidbase.*

/**
 * Base class for all activities in the application.
 *
 * This class delegates the responsibilities of an Android activity to other classes. For example,
 * callbacks related to menus are forwarded to a []BaseMenu], while callbacks related to activity
 * results are forwarded to a [BaseScreen].
 *
 *
 * A BaseActivity also installs an [java.lang.Thread.UncaughtExceptionHandler] to the main thread.
 * By default, this handler is an instance of BaseExceptionHandler, which logs the exception to the
 * disk before the application crashes. To the default handler, you should override the method
 * getExceptionHandler.
 */
abstract class BaseActivity : AppCompatActivity() {
    private var baseMenu: BaseMenu? = null
    private var screen: BaseScreen? = null

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if (menu != null) baseMenu?.onCreate(menuInflater, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item == null) return false
        return baseMenu?.onItemSelected(item) ?: false
    }

    fun restartWithFade(cls: Class<*>?) {
        Handler().postDelayed({
            finish()
            overridePendingTransition(anim.fade_in, anim.fade_out)
            startActivity(Intent(this, cls))
        }, 500) // HACK: Let the menu disappear first
    }

    fun setBaseMenu(baseMenu: BaseMenu?) {
        this.baseMenu = baseMenu
    }

    fun setScreen(screen: BaseScreen?) {
        this.screen = screen
    }

    fun showDialog(dialog: AppCompatDialogFragment, tag: String?) {
        dialog.show(supportFragmentManager, tag)
    }

    fun showDialog(dialog: AppCompatDialog) {
        dialog.show()
    }

    override fun onActivityResult(request: Int, result: Int, data: Intent?) {
        val screen = screen
        if(screen == null) super.onActivityResult(request, result, data)
        else screen.onResult(request, result, data)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Thread.setDefaultUncaughtExceptionHandler(getExceptionHandler())
    }

    private fun getExceptionHandler() = BaseExceptionHandler(this)

    override fun onResume() {
        super.onResume()
        screen?.reattachDialogs()
    }

    override fun startActivity(intent: Intent?) {
        try {
            super.startActivity(intent)
        } catch(e: ActivityNotFoundException) {
            this.screen?.showMessage(R.string.activity_not_found)
        }
    }
}