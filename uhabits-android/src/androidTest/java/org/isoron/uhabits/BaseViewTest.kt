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
package org.isoron.uhabits

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.view.View
import android.view.View.MeasureSpec
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.test.platform.app.InstrumentationRegistry
import org.isoron.uhabits.utils.FileUtils.getSDCardDir
import org.isoron.uhabits.utils.InterfaceUtils.dpToPixels
import org.isoron.uhabits.widgets.BaseWidget
import org.isoron.uhabits.widgets.WidgetDimensions
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.Random
import kotlin.math.abs

open class BaseViewTest : BaseAndroidTest() {
    var similarityCutoff = 0.00018

    @Throws(IOException::class)
    protected fun assertRenders(view: View, expectedImagePath: String) {
        val actual = renderView(view)
        assertRenders(actual, expectedImagePath)
    }

    @Throws(IOException::class)
    protected fun assertRenders(actual: Bitmap, expectedImagePath: String) {
        var expectedImagePath = expectedImagePath
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        expectedImagePath = "views/$expectedImagePath"
        try {
            val expected = getBitmapFromAssets(expectedImagePath)
            val distance = distance(actual, expected)
            if (distance > similarityCutoff) {
                saveBitmap(expectedImagePath, ".expected", expected)
                val path = saveBitmap(expectedImagePath, "", actual)
                fail(
                    String.format(
                        "Image differs from expected " +
                            "(distance=%f). Actual rendered " +
                            "image saved to %s",
                        distance,
                        path
                    )
                )
            }
            expected.recycle()
        } catch (e: IOException) {
            val path = saveBitmap(expectedImagePath, "", actual)
            fail(
                String.format(
                    "Could not open expected image. Actual " +
                        "rendered image saved to %s",
                    path
                )
            )
            throw e
        }
    }

    protected fun convertToView(
        widget: BaseWidget,
        width: Int,
        height: Int
    ): FrameLayout {
        widget.setDimensions(
            WidgetDimensions(width, height, width, height)
        )
        val view = FrameLayout(targetContext)
        val remoteViews = widget.portraitRemoteViews
        view.addView(remoteViews.apply(targetContext, view))
        measureView(view, width.toFloat(), height.toFloat())
        return view
    }

    protected fun dpToPixels(dp: Int): Float {
        return dpToPixels(targetContext, dp.toFloat())
    }

    protected fun measureView(view: View, width: Float, height: Float) {
        val specWidth = MeasureSpec.makeMeasureSpec(width.toInt(), MeasureSpec.EXACTLY)
        val specHeight = MeasureSpec.makeMeasureSpec(height.toInt(), MeasureSpec.EXACTLY)
        view.layoutParams = ViewGroup.LayoutParams(width.toInt(), height.toInt())
        view.measure(specWidth, specHeight)
        view.layout(0, 0, view.measuredWidth, view.measuredHeight)
    }

    protected fun skipAnimation(view: View) {
        val animator = view.animate()
        animator.duration = 0
        animator.start()
    }

    private fun colorToArgb(c1: Int): IntArray {
        return intArrayOf(
            c1 shr 24 and 0xff, // alpha
            c1 shr 16 and 0xff, // red
            c1 shr 8 and 0xff, // green
            c1 and 0xff // blue
        )
    }

    private fun distance(b1: Bitmap, b2: Bitmap): Double {
        if (b1.width != b2.width) return 1.0
        if (b1.height != b2.height) return 1.0
        val random = Random()
        var distance = 0.0
        for (x in 0 until b1.width) {
            for (y in 0 until b1.height) {
                if (random.nextInt(4) != 0) continue
                val argb1 = colorToArgb(b1.getPixel(x, y))
                val argb2 = colorToArgb(b2.getPixel(x, y))
                distance += abs(argb1[0] - argb2[0]).toDouble()
                distance += abs(argb1[1] - argb2[1]).toDouble()
                distance += abs(argb1[2] - argb2[2]).toDouble()
                distance += abs(argb1[3] - argb2[3]).toDouble()
            }
        }
        distance /= 255.0 * 16 * b1.width * b1.height
        return distance
    }

    @Throws(IOException::class)
    private fun getBitmapFromAssets(path: String): Bitmap {
        val stream = testContext.assets.open(path)
        return BitmapFactory.decodeStream(stream)
    }

    @Throws(IOException::class)
    private fun saveBitmap(filename: String, suffix: String, bitmap: Bitmap): String {
        var filename = filename
        var dir = getSDCardDir("test-screenshots")
        if (dir == null) dir = AndroidDirFinder(targetContext).getFilesDir("test-screenshots")
        if (dir == null) throw RuntimeException(
            "Could not find suitable dir for screenshots"
        )
        filename = filename.replace("\\.png$".toRegex(), "$suffix.png")
        val absolutePath = String.format("%s/%s", dir.absolutePath, filename)
        val parent = File(absolutePath).parentFile
        if (!parent.exists() && !parent.mkdirs()) throw RuntimeException(
            String.format(
                "Could not create dir: %s",
                parent.absolutePath
            )
        )
        val out = FileOutputStream(absolutePath)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        return absolutePath
    }

    fun renderView(view: View): Bitmap {
        val width = view.measuredWidth
        val height = view.measuredHeight
        if (view.isLayoutRequested) measureView(view, width.toFloat(), height.toFloat())
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.invalidate()
        view.draw(canvas)
        return bitmap
    }
}
