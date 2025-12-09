/*
 * Copyright (C) 2016-2025 Álinson Santos Xavier <git@axavier.org>
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

package org.isoron.uhabits.activities.habits.show.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import org.isoron.uhabits.core.ui.screens.habits.show.views.NotesCardState
import org.isoron.uhabits.databinding.ShowHabitNotesBinding
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.text.util.Linkify
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.*
import java.util.regex.Pattern

class NotesCardView(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {
    private val binding = ShowHabitNotesBinding.inflate(LayoutInflater.from(context), this)
    fun setState(state: NotesCardState) {
        if (state.description.isEmpty()) {
            visibility = GONE
            return
        } 
        
        visibility = VISIBLE
        
        // 1. Set text immediately (AutoLink in XML handles web/phone/email instantly)
        binding.habitNotes.text = state.description

        // 2. Launch background work to find "weird" links
        findViewTreeLifecycleOwner()?.lifecycleScope?.launch(Dispatchers.Default) {
            val description = state.description
            
            // Create a Spannable to work on
            val spannable = SpannableString(description)
            
            // RE-APPLY standard links to the spannable (since we created a new one)
            Linkify.addLinks(spannable, Linkify.WEB_URLS or Linkify.PHONE_NUMBERS or Linkify.EMAIL_ADDRESSES)

            // Custom permissive regex, matches scheme:anything_until_whitespace
            val customPattern = Pattern.compile("\\b[a-z][a-z0-9+.-]*:[^\\s]+", Pattern.CASE_INSENSITIVE)

            // Define the Validator
            val matchFilter = Linkify.MatchFilter { s, start, end ->
                val url = s.subSequence(start, end).toString()
                
                // OPTIMIZATION: Skip common schemes we already handled to save IPC calls
                if (url.startsWith("http") || url.startsWith("mailto") || url.startsWith("tel")) {
                    return@MatchFilter false
                }

                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                val flags = PackageManager.MATCH_DEFAULT_ONLY
                
                // The expensive IPC call
                val activities = context.packageManager.queryIntentActivities(intent, flags)
                activities.isNotEmpty()
            }

            // Apply the custom links
            val foundLinks = Linkify.addLinks(spannable, customPattern, "", matchFilter, null)

            // 3. Post back to UI only if we actually found something worth updating
            if (foundLinks) {
                withContext(Dispatchers.Main) {
                    // Check if the text is still the same (user didn't scroll away/update)
                    if (binding.habitNotes.text.toString() == description) {
                        binding.habitNotes.movementMethod = LinkMovementMethod.getInstance()
                        binding.habitNotes.text = spannable
                    }
                }
            }
        }
    }
}
