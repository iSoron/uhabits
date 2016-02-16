package org.isoron.uhabits.dialogs;


import android.os.Bundle;
import android.preference.PreferenceFragment;

import org.isoron.uhabits.R;

public class SettingsFragment extends PreferenceFragment
{
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }
}