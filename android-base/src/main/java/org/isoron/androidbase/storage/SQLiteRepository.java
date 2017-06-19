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

package org.isoron.androidbase.storage;

import android.content.*;
import android.database.*;
import android.database.sqlite.*;
import android.support.annotation.*;
import android.util.*;

import org.apache.commons.lang3.*;

import java.lang.annotation.*;
import java.lang.reflect.*;
import java.util.*;

public class SQLiteRepository<T>
{
    @NonNull
    private final Class klass;

    @NonNull
    private final SQLiteDatabase db;

    public SQLiteRepository(@NonNull Class klass, @NonNull SQLiteDatabase db)
    {
        this.klass = klass;
        this.db = db;
    }

    @Nullable
    public T find(@NonNull Long id)
    {
        return findFirst(String.format("where %s=?", getIdName()),
            id.toString());
    }

    @NonNull
    public List<T> findAll(String query, String... params)
    {
        try (Cursor c = db.rawQuery(buildSelectQuery() + query, params))
        {
            return cursorToMultipleRecords(c);
        }
    }

    @Nullable
    public T findFirst(String query, String... params)
    {
        try (Cursor c = db.rawQuery(buildSelectQuery() + query, params))
        {
            if (!c.moveToNext()) return null;
            return cursorToSingleRecord(c);
        }
    }

    public void execSQL(String query, Object... params)
    {
        db.execSQL(query, params);
    }

    public void save(T record)
    {
        try
        {
            Field fields[] = getFields();
            String columns[] = getColumnNames();

            ContentValues values = new ContentValues();
            for (int i = 0; i < fields.length; i++)
                fieldToContentValue(values, columns[i], fields[i], record);

            Long id = (Long) getIdField().get(record);
            int affectedRows = 0;

            if (id != null) affectedRows =
                db.update(getTableName(), values, getIdName() + "=?",
                    new String[]{ id.toString() });

            if (id == null || affectedRows == 0)
            {
                id = db.insertOrThrow(getTableName(), null, values);
                getIdField().set(record, id);
            }

        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    public void remove(T record)
    {
        try
        {
            Long id = (Long) getIdField().get(record);
            if (id == null) return;

            db.delete(getTableName(), getIdName() + "=?",
                new String[]{ id.toString() });
            getIdField().set(record, null);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    @NonNull
    private List<T> cursorToMultipleRecords(Cursor c)
    {
        List<T> records = new LinkedList<>();
        while (c.moveToNext()) records.add(cursorToSingleRecord(c));
        return records;
    }

    @NonNull
    private T cursorToSingleRecord(Cursor cursor)
    {
        try
        {
            Constructor constructor = klass.getDeclaredConstructors()[0];
            constructor.setAccessible(true);
            T record = (T) constructor.newInstance();

            int index = 0;
            for (Field field : getFields())
                copyFieldFromCursor(record, field, cursor, index++);
            return record;
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    private void copyFieldFromCursor(T record, Field field, Cursor c, int index)
        throws IllegalAccessException
    {
        if (field.getType().isAssignableFrom(Integer.class))
            field.set(record, c.getInt(index));
        else if (field.getType().isAssignableFrom(Long.class))
            field.set(record, c.getLong(index));
        else if (field.getType().isAssignableFrom(Double.class))
            field.set(record, c.getDouble(index));
        else if (field.getType().isAssignableFrom(String.class))
            field.set(record, c.getString(index));
        else throw new RuntimeException(
                "Type not supported: " + field.getType().getName() + " " +
                field.getName());
    }

    private void fieldToContentValue(ContentValues values,
                                     String columnName,
                                     Field field,
                                     T record)
    {
        try
        {
            if (field.getType().isAssignableFrom(Integer.class))
                values.put(columnName, (Integer) field.get(record));
            else if (field.getType().isAssignableFrom(Long.class))
                values.put(columnName, (Long) field.get(record));
            else if (field.getType().isAssignableFrom(Double.class))
                values.put(columnName, (Double) field.get(record));
            else if (field.getType().isAssignableFrom(String.class))
                values.put(columnName, (String) field.get(record));
            else throw new RuntimeException(
                    "Type not supported: " + field.getName());
        }
        catch (IllegalAccessException e)
        {
            throw new RuntimeException(e);
        }
    }

    private String buildSelectQuery()
    {
        return String.format("select %s from %s ",
            StringUtils.join(getColumnNames(), ", "), getTableName());
    }

    private List<Pair<Field, Column>> getFieldColumnPairs()
    {
        List<Pair<Field, Column>> fields = new ArrayList<>();
        for (Field field : klass.getDeclaredFields())
            for (Annotation annotation : field.getAnnotations())
            {
                if (!(annotation instanceof Column)) continue;
                Column column = (Column) annotation;
                fields.add(new Pair<>(field, column));
            }
        return fields;
    }

    @NonNull
    private Field[] getFields()
    {
        List<Field> fields = new ArrayList<>();
        List<Pair<Field, Column>> columns = getFieldColumnPairs();
        for (Pair<Field, Column> pair : columns) fields.add(pair.first);
        return fields.toArray(new Field[]{});
    }

    @NonNull
    private String[] getColumnNames()
    {
        List<String> names = new ArrayList<>();
        List<Pair<Field, Column>> columns = getFieldColumnPairs();
        for (Pair<Field, Column> pair : columns)
        {
            String cname = pair.second.name();
            if (cname.isEmpty()) cname = pair.first.getName();
            if (names.contains(cname))
                throw new RuntimeException("duplicated column : " + cname);
            names.add(cname);
        }

        return names.toArray(new String[]{});
    }

    @NonNull
    private String getTableName()
    {
        String name = getTableAnnotation().name();
        if (name.isEmpty()) throw new RuntimeException("Table name is empty");
        return name;
    }

    @NonNull
    private String getIdName()
    {
        String id = getTableAnnotation().id();
        if (id.isEmpty()) throw new RuntimeException("Table id is empty");
        return id;
    }

    @NonNull
    private Field getIdField()
    {
        Field fields[] = getFields();
        String idName = getIdName();
        for (Field f : fields)
            if (f.getName().equals(idName)) return f;
        throw new RuntimeException("Field not found: " + idName);
    }

    @NonNull
    private Table getTableAnnotation()
    {
        Table t = null;
        for (Annotation annotation : klass.getAnnotations())
        {
            if (!(annotation instanceof Table)) continue;
            t = (Table) annotation;
            break;
        }

        if (t == null) throw new RuntimeException("Table annotation not found");
        return t;
    }
}
