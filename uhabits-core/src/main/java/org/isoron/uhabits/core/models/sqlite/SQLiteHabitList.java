/*
 * Copyright (C) 2017 Álinson Santos Xavier <isoron@gmail.com>
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
 *
 *
 */

package org.isoron.uhabits.core.models.sqlite;

import android.support.annotation.*;

import org.isoron.uhabits.core.database.*;
import org.isoron.uhabits.core.models.*;
import org.isoron.uhabits.core.models.memory.*;
import org.isoron.uhabits.core.models.sqlite.records.*;

import java.util.*;

import javax.inject.*;

/**
 * Implementation of a {@link HabitList} that is backed by SQLite.
 */
public class SQLiteHabitList extends HabitList
{
    private static SQLiteHabitList instance;

    @NonNull
    private final Repository<HabitRecord> repository;

    @NonNull
    private final ModelFactory modelFactory;

    @NonNull
    private final MemoryHabitList list;

    private boolean loaded = false;

    @Inject
    public SQLiteHabitList(@NonNull ModelFactory modelFactory)
    {
        super();
        this.modelFactory = modelFactory;
        this.list = new MemoryHabitList();
        this.repository = modelFactory.buildHabitListRepository();
    }

    private void loadRecords()
    {
        if (loaded) return;
        loaded = true;

        list.removeAll();
        List<HabitRecord> records = repository.findAll("order by position");
        for (HabitRecord rec : records)
        {
            Habit h = modelFactory.buildHabit();
            rec.copyTo(h);
            list.add(h);
        }
    }

    public static SQLiteHabitList getInstance(
        @NonNull ModelFactory modelFactory)
    {
        if (instance == null) instance = new SQLiteHabitList(modelFactory);
        return instance;
    }

    @Override
    public synchronized void add(@NonNull Habit habit)
    {
        loadRecords();
        habit.setPosition(size());
        list.add(habit);

        HabitRecord record = new HabitRecord();
        record.copyFrom(habit);
        repository.save(record);
        rebuildOrder();

        getObservable().notifyListeners();
    }

    @Override
    @Nullable
    public Habit getById(long id)
    {
        loadRecords();
        return list.getById(id);
    }

    @Override
    @NonNull
    public Habit getByPosition(int position)
    {
        loadRecords();
        return list.getByPosition(position);
    }

    @NonNull
    @Override
    public HabitList getFiltered(HabitMatcher filter)
    {
        loadRecords();
        return list.getFiltered(filter);
    }

    @Override
    @NonNull
    public Order getOrder()
    {
        return list.getOrder();
    }

    @Override
    public void setOrder(@NonNull Order order)
    {
        list.setOrder(order);
        getObservable().notifyListeners();
    }

    @Override
    public int indexOf(@NonNull Habit h)
    {
        loadRecords();
        return list.indexOf(h);
    }

    @Override
    public Iterator<Habit> iterator()
    {
        loadRecords();
        return list.iterator();
    }

    private synchronized void rebuildOrder()
    {
        List<HabitRecord> records = repository.findAll("order by position");
        repository.executeAsTransaction(() -> {
            int pos = 0;
            for (HabitRecord r : records) {
                r.position = pos++;
                repository.save(r);
            }
        });
    }

    @Override
    public synchronized void remove(@NonNull Habit habit)
    {
        loadRecords();
        list.remove(habit);

        HabitRecord record = repository.find(habit.getId());
        if (record == null) throw new RuntimeException("habit not in database");
        repository.executeAsTransaction(() ->
        {
            ((SQLiteRepetitionList) habit.getRepetitions()).removeAll();
            repository.remove(record);
        });

        rebuildOrder();
        getObservable().notifyListeners();
    }

    @Override
    public synchronized void removeAll()
    {
        list.removeAll();
        repository.execSQL("delete from habits");
        repository.execSQL("delete from repetitions");
        getObservable().notifyListeners();
    }

    @Override
    public synchronized void reorder(@NonNull Habit from, @NonNull Habit to)
    {
        loadRecords();
        list.reorder(from, to);

        HabitRecord fromRecord = repository.find(from.getId());
        HabitRecord toRecord = repository.find(to.getId());

        if (fromRecord == null)
            throw new RuntimeException("habit not in database");
        if (toRecord == null)
            throw new RuntimeException("habit not in database");

        if (toRecord.position < fromRecord.position)
        {
            repository.execSQL("update habits set position = position + 1 " +
                               "where position >= ? and position < ?",
                                toRecord.position, fromRecord.position);
        }
        else
        {
            repository.execSQL("update habits set position = position - 1 " +
                               "where position > ? and position <= ?",
                               fromRecord.position, toRecord.position);
        }

        fromRecord.position = toRecord.position;
        repository.save(fromRecord);

        getObservable().notifyListeners();
    }

    @Override
    public void repair()
    {
        loadRecords();
        rebuildOrder();
        getObservable().notifyListeners();
    }

    @Override
    public int size()
    {
        loadRecords();
        return list.size();
    }

    @Override
    public synchronized void update(List<Habit> habits)
    {
        loadRecords();
        list.update(habits);

        for (Habit h : habits)
        {
            HabitRecord record = repository.find(h.getId());
            if (record == null) continue;
            record.copyFrom(h);
            repository.save(record);
        }

        getObservable().notifyListeners();
    }

    public void reload()
    {
        loaded = false;
    }
}
