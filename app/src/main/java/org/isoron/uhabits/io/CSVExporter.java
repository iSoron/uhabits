package org.isoron.uhabits.io;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.activeandroid.Cache;

import org.isoron.helpers.DateHelper;
import org.isoron.uhabits.models.Habit;
import org.isoron.uhabits.models.Score;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class CSVExporter
{
    private List<Habit> habits;
    private Context context;
    private java.text.DateFormat dateFormat;

    private List<String> generateDirs;
    private List<String> generateFilenames;

    private String basePath;

    public CSVExporter(Context context, List<Habit> habits)
    {
        this.habits = habits;
        this.context = context;
        generateDirs = new LinkedList<>();
        generateFilenames = new LinkedList<>();

        basePath = String.format("%s/export/", context.getFilesDir());

        dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    public String formatDate(long timestamp)
    {
        return dateFormat.format(new Date(timestamp));
    }

    public String formatScore(int score)
    {
        return String.format("%.2f", ((float) score) / Score.MAX_SCORE);
    }

    private void writeScores(String dirPath, Habit habit) throws IOException
    {
        String path = dirPath + "scores.csv";
        FileWriter out = new FileWriter(basePath + path);
        generateFilenames.add(path);

        String query = "select timestamp, score from score where habit = ? order by timestamp";
        String params[] = { habit.getId().toString() };

        SQLiteDatabase db = Cache.openDatabase();
        Cursor cursor = db.rawQuery(query, params);

        if(!cursor.moveToFirst()) return;

        do
        {
            String timestamp = formatDate(cursor.getLong(0));
            String score = formatScore(cursor.getInt(1));
            out.write(String.format("%s,%s\n", timestamp, score));

        } while(cursor.moveToNext());

        out.close();
        cursor.close();
    }

    private void writeCheckmarks(String dirPath, Habit habit) throws IOException
    {
        String path = dirPath + "checkmarks.csv";
        FileWriter out = new FileWriter(basePath + path);
        generateFilenames.add(path);

        String query = "select timestamp, value from checkmarks where habit = ? order by timestamp";
        String params[] = { habit.getId().toString() };

        SQLiteDatabase db = Cache.openDatabase();
        Cursor cursor = db.rawQuery(query, params);

        if(!cursor.moveToFirst()) return;

        do
        {
            String timestamp = formatDate(cursor.getLong(0));
            Integer value = cursor.getInt(1);
            out.write(String.format("%s,%d\n", timestamp, value));

        } while(cursor.moveToNext());

        out.close();
        cursor.close();
    }

    private void writeFiles(Habit habit) throws IOException
    {
        String path = String.format("%s/", habit.name);
        new File(basePath + path).mkdirs();
        generateDirs.add(path);

        writeScores(path, habit);
        writeCheckmarks(path, habit);
    }

    private void writeZipFile(String zipFilename) throws IOException
    {
        FileOutputStream fos = new FileOutputStream(zipFilename);
        ZipOutputStream zos = new ZipOutputStream(fos);

        for(String filename : generateFilenames)
            addFileToZip(zos, filename);

        zos.close();
        fos.close();
    }

    private void addFileToZip(ZipOutputStream zos, String filename) throws IOException
    {
        FileInputStream fis = new FileInputStream(new File(basePath + filename));
        ZipEntry ze = new ZipEntry(filename);
        zos.putNextEntry(ze);

        int length;
        byte bytes[] = new byte[1024];
        while((length = fis.read(bytes)) >= 0)
            zos.write(bytes, 0, length);

        zos.closeEntry();
        fis.close();
    }

    private void cleanup()
    {
        for(String filename : generateFilenames)
            new File(basePath + filename).delete();

        for(String filename : generateDirs)
            new File(basePath + filename).delete();

        new File(basePath).delete();
    }

    public String writeArchive()
    {
        String date = formatDate(DateHelper.getStartOfToday());

        File dir = context.getExternalCacheDir();

        if(dir == null)
        {
            Log.e("CSVExporter", "No suitable directory found.");
            return null;
        }

        String zipFilename = String.format("%s/habits-%s.zip", dir, date);

        try
        {
            for (Habit h : habits)
                writeFiles(h);

            writeZipFile(zipFilename);
            cleanup();
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return null;
        }

        return zipFilename;
    }


}
