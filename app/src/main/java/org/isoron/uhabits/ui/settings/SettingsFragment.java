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

package org.isoron.uhabits.ui.settings;

import android.app.backup.BackupManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceFragmentCompat;

import org.isoron.uhabits.HabitsApplication;
import org.isoron.uhabits.R;
import org.isoron.uhabits.utils.InterfaceUtils;
import org.isoron.uhabits.utils.ReminderUtils;

public class SettingsFragment extends PreferenceFragmentCompat
    implements SharedPreferences.OnSharedPreferenceChangeListener
{
    private static int RINGTONE_REQUEST_CODE = 1;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        setResultOnPreferenceClick("importData",
            HabitsApplication.RESULT_IMPORT_DATA);
        setResultOnPreferenceClick("exportCSV",
            HabitsApplication.RESULT_EXPORT_CSV);
        setResultOnPreferenceClick("exportDB",
            HabitsApplication.RESULT_EXPORT_DB);
        setResultOnPreferenceClick("bugReport",
            HabitsApplication.RESULT_BUG_REPORT);

        updateRingtoneDescription();

        if (InterfaceUtils.isLocaleFullyTranslated())
            removePreference("translate", "linksCategory");
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String s)
    {

    }

    private void removePreference(String preferenceKey, String categoryKey)
    {
        PreferenceCategory cat =
            (PreferenceCategory) findPreference(categoryKey);
        Preference pref = findPreference(preferenceKey);
        cat.removePreference(pref);
    }

    private void setResultOnPreferenceClick(String key, final int result)
    {
        Preference pref = findPreference(key);
        pref.setOnPreferenceClickListener(preference -> {
            getActivity().setResult(result);
            getActivity().finish();
            return true;
        });
    }

    @Override
    public void onResume()
    {
        super.onResume();
        getPreferenceManager().getSharedPreferences().
            registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause()
    {
        getPreferenceManager().getSharedPreferences().
            unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                          String key)
    {
        BackupManager.dataChanged("org.isoron.uhabits");
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference)
    {
        if (preference.getKey() == null) return false;

        if (preference.getKey().equals("reminderSound"))
        {
            ReminderUtils.startRingtonePickerActivity(this,
                RINGTONE_REQUEST_CODE);
            return true;
        }

        return super.onPreferenceTreeClick(preference);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == RINGTONE_REQUEST_CODE)
        {
            ReminderUtils.parseRingtoneData(getContext(), data);
            updateRingtoneDescription();
            return;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void updateRingtoneDescription()
    {
        String ringtoneName = ReminderUtils.getRingtoneName(getContext());
        if(ringtoneName == null) return;
        Preference ringtonePreference = findPreference("reminderSound");
        ringtonePreference.setSummary(ringtoneName);
    }
}