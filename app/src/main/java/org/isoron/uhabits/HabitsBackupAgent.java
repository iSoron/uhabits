package org.isoron.uhabits;

import android.app.backup.BackupAgentHelper;
import android.app.backup.FileBackupHelper;
import android.app.backup.SharedPreferencesBackupHelper;

public class HabitsBackupAgent extends BackupAgentHelper
{
    @Override
    public void onCreate()
    {
        addHelper("preferences", new SharedPreferencesBackupHelper(this, "preferences"));
        addHelper("database", new FileBackupHelper(this, "../databases/uhabits.db"));
    }
}
