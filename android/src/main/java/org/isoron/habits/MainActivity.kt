/*
 * Copyright (C) 2016-2019 Álinson Santos Xavier <isoron@gmail.com>
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

package org.isoron.habits

import android.content.*
import android.net.*
import android.os.*
import android.os.Build.VERSION.*
import android.os.Build.VERSION_CODES.*
import android.provider.*
import android.support.v7.app.*
import android.view.*
import com.facebook.react.*
import com.facebook.react.common.*
import com.facebook.react.shell.*
import com.horcrux.svg.*

class MainActivity : AppCompatActivity() {

    private lateinit var reactInstanceManager: ReactInstanceManager
    private lateinit var reactRootView: ReactRootView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (SDK_INT >= M && !Settings.canDrawOverlays(this)) {
            startActivityForResult(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")), 0)
        }

        reactRootView = ReactRootView(this)
        reactInstanceManager = ReactInstanceManager.builder()
                .setApplication(application)
                .setBundleAssetName("index.android.bundle")
                .setJSMainModulePath("index.android")
                .addPackage(MainReactPackage())
                .addPackage(CorePackage())
                .addPackage(SvgPackage())
                .setUseDeveloperSupport(BuildConfig.DEBUG)
                .setInitialLifecycleState(LifecycleState.RESUMED)
                .build()

        reactRootView.startReactApplication(reactInstanceManager, "LoopHabitTracker", null)
        setContentView(reactRootView)
    }

    override fun onPause() {
        reactInstanceManager.onHostPause(this)
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        reactInstanceManager.onHostResume(this)
    }

    override fun onDestroy() {
        reactInstanceManager.onHostDestroy(this)
        reactRootView.unmountReactApplication()
        super.onDestroy()
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            reactInstanceManager.showDevOptionsDialog()
            return true
        }
        return super.onKeyUp(keyCode, event)
    }
}
