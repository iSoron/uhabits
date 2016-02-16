package org.isoron.uhabits;

import android.app.Activity;
import android.os.Bundle;

import org.isoron.uhabits.dialogs.SettingsFragment;

public class SettingsActivity extends Activity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content,
                new SettingsFragment()).commit();
    }
}
