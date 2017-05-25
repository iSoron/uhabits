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
import android.os.*;
import android.support.annotation.*;
import android.support.v7.preference.*;

import org.isoron.androidbase.activities.*;
import org.isoron.uhabits.*;
import org.isoron.uhabits.R;
import org.isoron.uhabits.notifications.*;
import org.isoron.uhabits.preferences.*;

import static org.isoron.uhabits.activities.habits.list.ListHabitsScreen.*;

public class SettingsFragment extends PreferenceFragmentCompat
    implements SharedPreferences.OnSharedPreferenceChangeListener
{
    private static int RINGTONE_REQUEST_CODE = 1;

    private SharedPreferences sharedPrefs;

    @Nullable
    private Preferences prefs;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == RINGTONE_REQUEST_CODE)
        {
            RingtoneManager.parseRingtoneData(getContext(), data);
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
        if(appContext instanceof HabitsApplication)
        {
            HabitsApplication app = (HabitsApplication) appContext;
            prefs = app.getComponent().getPreferences();
        }

        setResultOnPreferenceClick("importData", RESULT_IMPORT_DATA);
        setResultOnPreferenceClick("exportCSV", RESULT_EXPORT_CSV);
        setResultOnPreferenceClick("exportDB", RESULT_EXPORT_DB);
        setResultOnPreferenceClick("repairDB", RESULT_REPAIR_DB);
        setResultOnPreferenceClick("bugReport", RESULT_BUG_REPORT);

        updateRingtoneDescription();
        updateSync();
    }

    private void updateSync()
    {
        if(prefs == null) return;
        boolean enabled = prefs.isSyncFeatureEnabled();

        Preference syncKey = findPreference("pref_sync_key");
        syncKey.setSummary(prefs.getSyncKey());
        syncKey.setVisible(enabled);

        Preference syncAddress = findPreference("pref_sync_address");
        syncAddress.setSummary(prefs.getSyncAddress());
        syncAddress.setVisible(enabled);
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
            BaseScreen.showRingtonePicker(this,
                RINGTONE_REQUEST_CODE);
            return true;
        }

        return super.onPreferenceTreeClick(preference);
    }

    @Override
    public void onResume()
    {
        super.onResume();
        sharedPrefs = getPreferenceManager().getSharedPreferences();
        sharedPrefs.registerOnSharedPreferenceChangeListener(this);

        if(prefs != null && !prefs.isDeveloper())
        {
            PreferenceCategory devCategory =
                (PreferenceCategory) findPreference("devCategory");
            devCategory.removeAll();
            devCategory.setVisible(false);
        }
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

    private void updateRingtoneDescription()
    {
        String ringtoneName = RingtoneManager.getRingtoneName(getContext());
        if (ringtoneName == null) return;
        Preference ringtonePreference = findPreference("reminderSound");
        ringtonePreference.setSummary(ringtoneName);
    }
}