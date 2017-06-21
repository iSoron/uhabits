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
 */

package org.isoron.uhabits.core.database;

import android.support.annotation.*;

import org.apache.commons.lang3.*;
import org.apache.commons.lang3.tuple.*;

import java.lang.annotation.*;
import java.lang.reflect.*;
import java.util.*;

public class Repository<T>
{
    @NonNull
    private final Class klass;

    @NonNull
    private final Database db;

    public Repository(@NonNull Class<T> klass, @NonNull Database db)
    {
        this.klass = klass;
        this.db = db;
    }

    /**
     * Returns the record that has the id provided.
     * If no record is found, returns null.
     */
    @Nullable
    public T find(@NonNull Long id)
    {
        return findFirst(String.format("where %s=?", getIdName()),
            id.toString());
    }

    /**
     * Returns all records matching the given SQL query.
     * <p>
     * The query should only contain the "where" part of the SQL query, and
     * optinally the "order by" part. "Group by" is not allowed. If no matching
     * records are found, returns an empty list.
     */
    @NonNull
    public List<T> findAll(@NonNull String query, @NonNull String... params)
    {
        try (Cursor c = db.query(buildSelectQuery() + query, params))
        {
            return cursorToMultipleRecords(c);
        }
    }

    /**
     * Returns the first record matching the given SQL query.
     * See findAll for more details about the parameters.
     */
    @Nullable
    public T findFirst(String query, String... params)
    {
        try (Cursor c = db.query(buildSelectQuery() + query, params))
        {
            if (!c.moveToNext()) return null;
            return cursorToSingleRecord(c);
        }
    }

    /**
     * Executes the given SQL query on the repository.
     * <p>
     * The query can be of any kind. For example, complex deletes and updates
     * are allowed. The repository does not perform any checks to guarantee
     * that the query is valid, however the underlying database might.
     */
    public void execSQL(String query, Object... params)
    {
        db.execute(query, params);
    }

    /**
     * Executes the given callback inside a database transaction.
     * <p>
     * If the callback terminates without throwing any exceptions, the
     * transaction is considered successful. If any exceptions are thrown,
     * the transaction is aborted. Nesting transactions is not allowed.
     */
    public void executeAsTransaction(Runnable callback)
    {
        db.beginTransaction();
        try
        {
            callback.run();
            db.setTransactionSuccessful();
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
        finally
        {
            db.endTransaction();
        }
    }

    /**
     * Saves the record on the database.
     * <p>
     * If the id of the given record is null, it is assumed that the record has
     * not been inserted in the repository yet. The record will be inserted, a
     * new id will be automatically generated, and the id of the given record
     * will be updated.
     * <p>
     * If the given record has a non-null id, then an update will be performed
     * instead. That is, the previous record will be overwritten by the one
     * provided.
     */
    public void save(T record)
    {
        try
        {
            Field fields[] = getFields();
            String columns[] = getColumnNames();

            Map<String, Object> values = new HashMap<>();
            for (int i = 0; i < fields.length; i++)
                values.put(columns[i], fields[i].get(record));

            Long id = (Long) getIdField().get(record);
            int affectedRows = 0;

            if (id != null) affectedRows =
                db.update(getTableName(), values, getIdName() + "=?",
                    id.toString());

            if (id == null || affectedRows == 0)
            {
                id = db.insert(getTableName(), values);
                getIdField().set(record, id);
            }

        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * Removes the given record from the repository.
     * The id of the given record is also set to null.
     */
    public void remove(T record)
    {
        try
        {
            Long id = (Long) getIdField().get(record);
            if (id == null) return;

            db.delete(getTableName(), getIdName() + "=?", id.toString());
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
                fields.add(new ImmutablePair<>(field, column));
            }
        return fields;
    }

    @NonNull
    private Field[] getFields()
    {
        List<Field> fields = new ArrayList<>();
        List<Pair<Field, Column>> columns = getFieldColumnPairs();
        for (Pair<Field, Column> pair : columns) fields.add(pair.getLeft());
        return fields.toArray(new Field[]{});
    }

    @NonNull
    private String[] getColumnNames()
    {
        List<String> names = new ArrayList<>();
        List<Pair<Field, Column>> columns = getFieldColumnPairs();
        for (Pair<Field, Column> pair : columns)
        {
            String cname = pair.getRight().name();
            if (cname.isEmpty()) cname = pair.getLeft().getName();
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
