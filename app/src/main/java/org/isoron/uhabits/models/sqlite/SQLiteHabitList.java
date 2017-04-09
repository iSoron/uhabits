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

package org.isoron.uhabits.models.sqlite;

import android.support.annotation.*;

import com.activeandroid.query.*;
import com.activeandroid.util.*;

import org.apache.commons.lang3.*;
import org.isoron.uhabits.models.*;
import org.isoron.uhabits.models.sqlite.records.*;

import java.util.*;

/**
 * Implementation of a {@link HabitList} that is backed by SQLite.
 */
public class SQLiteHabitList extends HabitList
{
    private static HashMap<Long, Habit> cache;

    private static SQLiteHabitList instance;

    @NonNull
    private final SQLiteUtils<HabitRecord> sqlite;

    @NonNull
    private final ModelFactory modelFactory;

    @NonNull
    private Order order;

    public SQLiteHabitList(@NonNull ModelFactory modelFactory)
    {
        super();
        this.modelFactory = modelFactory;

        if (cache == null) cache = new HashMap<>();
        sqlite = new SQLiteUtils<>(HabitRecord.class);
        order = Order.BY_POSITION;
    }

    protected SQLiteHabitList(@NonNull ModelFactory modelFactory,
                              @NonNull HabitMatcher filter,
                              @NonNull Order order)
    {
        super(filter);
        this.modelFactory = modelFactory;

        if (cache == null) cache = new HashMap<>();
        sqlite = new SQLiteUtils<>(HabitRecord.class);
        this.order = order;
    }

    public static SQLiteHabitList getInstance(
        @NonNull ModelFactory modelFactory)
    {
        if (instance == null) instance = new SQLiteHabitList(modelFactory);
        return instance;
    }

    @Override
    public void add(@NonNull Habit habit)
    {
        if (cache.containsValue(habit))
            throw new IllegalArgumentException("habit already added");

        HabitRecord record = new HabitRecord();
        record.copyFrom(habit);
        record.position = size();

        Long id = habit.getId();
        if (id == null) id = record.save();
        else record.save(id);

        if (id < 0)
            throw new IllegalArgumentException("habit could not be saved");

        habit.setId(id);
        cache.put(id, habit);
    }

    @Override
    @Nullable
    public Habit getById(long id)
    {
        if (!cache.containsKey(id))
        {
            HabitRecord record = HabitRecord.get(id);
            if (record == null) return null;

            Habit habit = modelFactory.buildHabit();
            record.copyTo(habit);
            cache.put(id, habit);
        }

        return cache.get(id);
    }

    @Override
    @NonNull
    public Habit getByPosition(int position)
    {
        return toList().get(position);
    }

    @NonNull
    @Override
    public HabitList getFiltered(HabitMatcher filter)
    {
        return new SQLiteHabitList(modelFactory, filter, order);
    }

    @Override
    @NonNull
    public Order getOrder()
    {
        return order;
    }

    @Override
    public void setOrder(@NonNull Order order)
    {
        this.order = order;
    }

    @Override
    public int indexOf(@NonNull Habit h)
    {
        return toList().indexOf(h);
    }

    @Override
    public Iterator<Habit> iterator()
    {
        return Collections.unmodifiableCollection(toList()).iterator();
    }

    public void rebuildOrder()
    {
        List<Habit> habits = toList();

        int i = 0;
        for (Habit h : habits)
        {
            HabitRecord record = HabitRecord.get(h.getId());
            if (record == null)
                throw new RuntimeException("habit not in database");

            record.position = i++;
            record.save();
        }

        update(habits);
    }

    @Override
    public void remove(@NonNull Habit habit)
    {
        if (!cache.containsKey(habit.getId()))
            throw new RuntimeException("habit not in cache");

        cache.remove(habit.getId());
        HabitRecord record = HabitRecord.get(habit.getId());
        if (record == null) throw new RuntimeException("habit not in database");
        record.cascadeDelete();
        rebuildOrder();
    }

    @Override
    public void removeAll()
    {
        sqlite.query("delete from checkmarks", null);
        sqlite.query("delete from score", null);
        sqlite.query("delete from streak", null);
        sqlite.query("delete from repetitions", null);
        sqlite.query("delete from habits", null);
    }

    @Override
    public synchronized void reorder(Habit from, Habit to)
    {
        if (from == to) return;

        HabitRecord fromRecord = HabitRecord.get(from.getId());
        HabitRecord toRecord = HabitRecord.get(to.getId());

        if (fromRecord == null)
            throw new RuntimeException("habit not in database");
        if (toRecord == null)
            throw new RuntimeException("habit not in database");

        Integer fromPos = fromRecord.position;
        Integer toPos = toRecord.position;

        Log.d("SQLiteHabitList",
            String.format("reorder: %d %d", fromPos, toPos));

        if (toPos < fromPos)
        {
            new Update(HabitRecord.class)
                .set("position = position + 1")
                .where("position >= ? and position < ?", toPos, fromPos)
                .execute();
        }
        else
        {
            new Update(HabitRecord.class)
                .set("position = position - 1")
                .where("position > ? and position <= ?", fromPos, toPos)
                .execute();
        }

        fromRecord.position = toPos;
        fromRecord.save();
        update(from);
        getObservable().notifyListeners();
    }

    @Override
    public void repair()
    {
        super.repair();
        rebuildOrder();
    }

    @Override
    public int size()
    {
        return toList().size();
    }

    @Override
    public void update(List<Habit> habits)
    {
        for (Habit h : habits)
        {
            HabitRecord record = HabitRecord.get(h.getId());
            if (record == null)
                throw new RuntimeException("habit not in database");
            record.copyFrom(h);
            record.save();
        }
    }

    protected List<Habit> toList()
    {
        String query = buildSelectQuery();
        List<HabitRecord> recordList = sqlite.query(query, null);

        List<Habit> habits = new LinkedList<>();
        for (HabitRecord record : recordList)
        {
            Habit habit = getById(record.getId());
            if (habit == null)
                throw new RuntimeException("habit not in database");

            if (!filter.matches(habit)) continue;
            habits.add(habit);
        }

        if(order == Order.BY_SCORE)
        {
            Collections.sort(habits, (lhs, rhs) -> {
                int s1 = lhs.getScores().getTodayValue();
                int s2 = rhs.getScores().getTodayValue();
                return Integer.compare(s2, s1);
            });
        }

        return habits;
    }

    private void appendOrderBy(StringBuilder query)
    {
        switch (order)
        {
            case BY_POSITION:
                query.append("order by position ");
                break;

            case BY_NAME:
            case BY_SCORE:
                query.append("order by name ");
                break;

            case BY_COLOR:
                query.append("order by color, name ");
                break;

            default:
                throw new IllegalStateException();
        }
    }

    private void appendSelect(StringBuilder query)
    {
        query.append(HabitRecord.SELECT);
    }

    private void appendWhere(StringBuilder query)
    {
        ArrayList<Object> where = new ArrayList<>();
        if (filter.isReminderRequired()) where.add("reminder_hour is not null");
        if (!filter.isArchivedAllowed()) where.add("archived = 0");

        if (where.isEmpty()) return;
        query.append("where ");
        query.append(StringUtils.join(where, " and "));
        query.append(" ");
    }

    private String buildSelectQuery()
    {
        StringBuilder query = new StringBuilder();
        appendSelect(query);
        appendWhere(query);
        appendOrderBy(query);
        return query.toString();
    }
}
