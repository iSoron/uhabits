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
package org.isoron.uhabits.activities.about

import android.annotation.*
import android.content.*
import android.view.*
import android.widget.*
import org.isoron.uhabits.*
import org.isoron.uhabits.core.models.*
import org.isoron.uhabits.databinding.*
import org.isoron.uhabits.utils.*

@SuppressLint("ViewConstructor")
class AboutView(
        context: Context,
        private val screen: AboutScreen,
) : FrameLayout(context) {

    private var binding = AboutBinding.inflate(LayoutInflater.from(context))

    init {
        addView(binding.root)
        setupToolbar(
                toolbar = binding.toolbar,
                color = PaletteColor(11),
                title = resources.getString(R.string.about)
        )
        val version = resources.getString(R.string.version_n)
        binding.tvContributors.setOnClickListener { screen.showCodeContributorsWebsite() }
        binding.tvFeedback.setOnClickListener { screen.showSendFeedbackScreen() }
        binding.tvPrivacy.setOnClickListener { screen.showPrivacyPolicyWebsite() }
        binding.tvRate.setOnClickListener { screen.showRateAppWebsite() }
        binding.tvSource.setOnClickListener { screen.showSourceCodeWebsite() }
        binding.tvTranslate.setOnClickListener { screen.showTranslationWebsite() }
        binding.tvVersion.setOnClickListener { screen.onPressDeveloperCountdown() }
        binding.tvVersion.text = String.format(version, BuildConfig.VERSION_NAME)
    }
}