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

package org.isoron.uhabits;

import org.isoron.uhabits.models.Habit;
import org.isoron.uhabits.tasks.ExportCSVTask;
import org.isoron.uhabits.tasks.ExportDBTask;
import org.isoron.uhabits.tasks.ImportDataTask;
import org.isoron.uhabits.tasks.ProgressBar;
import org.isoron.uhabits.utils.DateUtils;

import java.io.File;
import java.io.IOException;

public class MainController implements ImportDataTask.Listener, ExportCSVTask.Listener,
        ExportDBTask.Listener
{
    public interface Screen
    {
        void showIntroScreen();

        void showMessage(Integer stringId);

        void refresh(Long refreshKey);

        ProgressBar getProgressBar();
    }

    public interface System
    {
        void sendFile(String filename);

        void sendEmail(String to, String subject, String content);

        void scheduleReminders();

        void updateWidgets();

        File dumpBugReportToFile() throws IOException;

        String getBugReport() throws IOException;
    }

    System sys;
    Screen screen;
    Preferences prefs;

    public MainController()
    {
        prefs = Preferences.getInstance();
    }

    public void setScreen(Screen screen)
    {
        this.screen = screen;
    }

    public void setSystem(System sys)
    {
        this.sys = sys;
    }

    public void onStartup()
    {
        prefs.initialize();
        prefs.incrementLaunchCount();
        prefs.updateLastAppVersion();
        if(prefs.isFirstRun()) onFirstRun();

        sys.updateWidgets();
        sys.scheduleReminders();
    }

    private void onFirstRun()
    {
        prefs.setFirstRun(false);
        prefs.setLastHintTimestamp(DateUtils.getStartOfToday());
        screen.showIntroScreen();
    }

    public void importData(File file)
    {
        ImportDataTask task = new ImportDataTask(file, screen.getProgressBar());
        task.setListener(this);
        task.execute();
    }

    @Override
    public void onImportDataFinished(int result)
    {
        switch (result)
        {
            case ImportDataTask.SUCCESS:
                screen.refresh(null);
                screen.showMessage(R.string.habits_imported);
                break;

            case ImportDataTask.NOT_RECOGNIZED:
                screen.showMessage(R.string.file_not_recognized);
                break;

            default:
                screen.showMessage(R.string.could_not_import);
                break;
        }
    }

    public void exportCSV()
    {
        ExportCSVTask task = new ExportCSVTask(Habit.getAll(true), screen.getProgressBar());
        task.setListener(this);
        task.execute();
    }

    @Override
    public void onExportCSVFinished(String filename)
    {
        if(filename != null) sys.sendFile(filename);
        else screen.showMessage(R.string.could_not_export);
    }

    public void exportDB()
    {
        ExportDBTask task = new ExportDBTask(screen.getProgressBar());
        task.setListener(this);
        task.execute();
    }

    @Override
    public void onExportDBFinished(String filename)
    {
        if(filename != null) sys.sendFile(filename);
        else screen.showMessage(R.string.could_not_export);
    }

    public void sendBugReport()
    {
        try
        {
            sys.dumpBugReportToFile();
        }
        catch (IOException e)
        {
            // ignored
        }

        try
        {
            String log = "---------- BUG REPORT BEGINS ----------\n";
            log += sys.getBugReport();
            log += "---------- BUG REPORT ENDS ------------\n";
            String to = "dev@loophabits.org";
            String subject = "Bug Report - Loop Habit Tracker";
            sys.sendEmail(log, to, subject);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            screen.showMessage(R.string.bug_report_failed);
        }
    }
}
