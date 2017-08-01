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

package org.isoron.uhabits.activities.settings;

import android.app.backup.*;
import android.content.*;
import android.net.*;
import android.os.*;
import android.provider.*;
import android.support.annotation.*;
import android.support.v7.preference.*;

import org.isoron.uhabits.*;
import org.isoron.uhabits.R;
import org.isoron.uhabits.core.preferences.*;
import org.isoron.uhabits.notifications.*;

import static android.media.RingtoneManager.ACTION_RINGTONE_PICKER;
import static android.media.RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI;
import static android.media.RingtoneManager.EXTRA_RINGTONE_EXISTING_URI;
import static android.media.RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT;
import static android.media.RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT;
import static android.media.RingtoneManager.EXTRA_RINGTONE_TYPE;
import static android.media.RingtoneManager.TYPE_NOTIFICATION;
import static org.isoron.uhabits.activities.habits.list.ListHabitsScreenKt.RESULT_BUG_REPORT;
import static org.isoron.uhabits.activities.habits.list.ListHabitsScreenKt.RESULT_EXPORT_CSV;
import static org.isoron.uhabits.activities.habits.list.ListHabitsScreenKt.RESULT_EXPORT_DB;
import static org.isoron.uhabits.activities.habits.list.ListHabitsScreenKt.RESULT_IMPORT_DATA;
import static org.isoron.uhabits.activities.habits.list.ListHabitsScreenKt.RESULT_REPAIR_DB;

public class SettingsFragment extends PreferenceFragmentCompat
    implements SharedPreferences.OnSharedPreferenceChangeListener
{
    private static int RINGTONE_REQUEST_CODE = 1;

    private SharedPreferences sharedPrefs;

    private RingtoneManager ringtoneManager;

    @Nullable
    private Preferences prefs;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == RINGTONE_REQUEST_CODE)
        {
            ringtoneManager.update(data);
            updateRingtoneDescription();
            return;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        Context appContext = getContext().getApplicationContext();
        if (appContext instanceof HabitsApplication)
        {
            HabitsApplication app = (HabitsApplication) appContext;
            prefs = app.getComponent().getPreferences();
        }

        setResultOnPreferenceClick("importData", RESULT_IMPORT_DATA);
        setResultOnPreferenceClick("exportCSV", RESULT_EXPORT_CSV);
        setResultOnPreferenceClick("exportDB", RESULT_EXPORT_DB);
        setResultOnPreferenceClick("repairDB", RESULT_REPAIR_DB);
        setResultOnPreferenceClick("bugReport", RESULT_BUG_REPORT);
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String s)
    {
        // NOP
    }

    @Override
    public void onPause()
    {
        sharedPrefs.unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference)
    {
        String key = preference.getKey();
        if (key == null) return false;

        if (key.equals("reminderSound"))
        {
            showRingtonePicker();
            return true;
        }

        return super.onPreferenceTreeClick(preference);
    }

    @Override
    public void onResume()
    {
        super.onResume();
        this.ringtoneManager = new RingtoneManager(getActivity());

        sharedPrefs = getPreferenceManager().getSharedPreferences();
        sharedPrefs.registerOnSharedPreferenceChangeListener(this);

        if (prefs != null && !prefs.isDeveloper())
        {
            PreferenceCategory devCategory =
                (PreferenceCategory) findPreference("devCategory");
            devCategory.removeAll();
            devCategory.setVisible(false);
        }

        updateRingtoneDescription();
        updateSync();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                          String key)
    {
        BackupManager.dataChanged("org.isoron.uhabits");
        updateSync();
    }

    private void setResultOnPreferenceClick(String key, final int result)
    {
        Preference pref = findPreference(key);
        pref.setOnPreferenceClickListener(preference ->
        {
            getActivity().setResult(result);
            getActivity().finish();
            return true;
        });
    }

    private void showRingtonePicker()
    {
        Uri existingRingtoneUri = ringtoneManager.getURI();
        Uri defaultRingtoneUri = Settings.System.DEFAULT_NOTIFICATION_URI;

        Intent intent = new Intent(ACTION_RINGTONE_PICKER);
        intent.putExtra(EXTRA_RINGTONE_TYPE, TYPE_NOTIFICATION);
        intent.putExtra(EXTRA_RINGTONE_SHOW_DEFAULT, true);
        intent.putExtra(EXTRA_RINGTONE_SHOW_SILENT, true);
        intent.putExtra(EXTRA_RINGTONE_DEFAULT_URI, defaultRingtoneUri);
        intent.putExtra(EXTRA_RINGTONE_EXISTING_URI, existingRingtoneUri);
        startActivityForResult(intent, RINGTONE_REQUEST_CODE);
    }

    private void updateRingtoneDescription()
    {
        String ringtoneName = ringtoneManager.getName();
        if (ringtoneName == null) return;
        Preference ringtonePreference = findPreference("reminderSound");
        ringtonePreference.setSummary(ringtoneName);
    }

    private void updateSync()
    {
        if (prefs == null) return;
        boolean enabled = prefs.isSyncEnabled();

        Preference syncKey = findPreference("pref_sync_key");
        if (syncKey != null)
        {
            syncKey.setSummary(prefs.getSyncKey());
            syncKey.setVisible(enabled);
        }

        Preference syncAddress = findPreference("pref_sync_address");
        if (syncAddress != null)
        {
            syncAddress.setSummary(prefs.getSyncAddress());
            syncAddress.setVisible(enabled);
        }
    }
}