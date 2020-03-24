package org.isoron.uhabits.core.io;

import androidx.annotation.NonNull;

import com.opencsv.CSVReader;

import org.isoron.uhabits.core.models.Checkmark;
import org.isoron.uhabits.core.models.Frequency;
import org.isoron.uhabits.core.models.Habit;
import org.isoron.uhabits.core.models.HabitList;
import org.isoron.uhabits.core.models.ModelFactory;
import org.isoron.uhabits.core.models.Timestamp;
import org.isoron.uhabits.core.utils.ColorConstants;
import org.isoron.uhabits.core.utils.DateFormats;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.inject.Inject;

public class HabitsCSVImporter extends AbstractImporter {
    private ModelFactory modelFactory;

    @Inject
    public HabitsCSVImporter(@NonNull HabitList habits,
                             @NonNull ModelFactory modelFactory) {
        super(habits);
        this.modelFactory = modelFactory;
    }

    @Override
    public boolean canHandle(@NonNull File file) {
        try {
            ZipFile zipFile = new ZipFile(file);
            ZipEntry entry = zipFile.getEntry("Habits.csv");
            return entry != null;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void importHabitsFromFile(@NonNull File file) throws IOException {
        ZipFile zipFile = new ZipFile(file);
        CSVReader habitsCsv = new CSVReader(new InputStreamReader(zipFile.getInputStream(zipFile.getEntry("Habits.csv"))));
        try {
            boolean hasQuestion = false;
            for (String line[] : habitsCsv) {
                if ("Position".equals(line[0])) {
                    // older csv files did not have a question column
                    if ("Question".equals(line[2])) hasQuestion = true;
                    continue;
                }

                int idx = 0;
                String position = line[idx++];
                String name = line[idx++];
                String question = hasQuestion ? line[idx++] : null;
                String desc = line[idx++];
                int reps = Integer.parseInt(line[idx++]);
                int intv = Integer.parseInt(line[idx++]);
                String colr = line[idx++];

                Habit habit = findExisting(name);
                if (habit == null) {
                    habit = modelFactory.buildHabit();
                    habit.setName(name);
                    if (hasQuestion) {
                        habit.setDescription(desc);
                        habit.setQuestion(question);
                    } else {
                        habit.setDescription("");
                        habit.setQuestion(desc);
                    }
                    habit.setFrequency(new Frequency(reps, intv));
                    habit.setColor(findColor(colr));
                    habitList.add(habit);
                }

                parseCheckmarks(findCheckmarks(position, zipFile), habit);
            }
        } finally {
            try {
                zipFile.close();
                habitsCsv.close();
            } catch (IOException e) {
                // ignore
            }
        }
    }

    Timestamp parse(String ts) throws ParseException {
        Date date = DateFormats.getCSVDateFormat().parse(ts);
        Timestamp timestamp = new Timestamp(date.getTime());
        return timestamp;
    }

    void parseCheckmarks(CSVReader checkmarks, Habit habit) {
        if (checkmarks == null) return;
        try {
            for (String line[] : checkmarks) {
                try {
                    Timestamp ts = parse(line[0]);
                    int value = Integer.parseInt(line[1]);
                    // only add positive changes ;)
                    if (value > Checkmark.UNCHECKED) {
                        habit.getRepetitions().toggle(ts, value);
                    }
                } catch (Exception e) {
                    // skip
                }
            }
        } finally {
            try {
                checkmarks.close();
            } catch (IOException e) {
                // ignore
            }
        }
    }

    CSVReader findCheckmarks(String position, ZipFile zipFile) throws IOException {
        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            // look for 00x */Checkmarks.csv
            if (!entry.isDirectory() && entry.getName().startsWith(position) && entry.getName().endsWith("/Checkmarks.csv")) {
                return new CSVReader(new InputStreamReader(zipFile.getInputStream(entry)));
            }
        }
        return null;
    }

    private Habit findExisting(String name) {
        Iterator<Habit> iterator = habitList.iterator();
        while (iterator.hasNext()) {
            Habit habit = iterator.next();
            if (habit.getName().equals(name)) return habit;
        }
        return null;
    }

    private int findColor(String hex) {
        for (int i = 0; i < ColorConstants.CSV_PALETTE.length; i++) {
            if (ColorConstants.CSV_PALETTE[i].equalsIgnoreCase(hex)) {
                return i;
            }
        }
        return 0;
    }
}
