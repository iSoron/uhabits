/*
 * Copyright (C) 2016-2021 Álinson Santos Xavier <git@axavier.org>
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
package org.isoron.uhabits.core.database

import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.tuple.ImmutablePair
import org.apache.commons.lang3.tuple.Pair
import java.lang.reflect.Field
import java.util.ArrayList
import java.util.HashMap
import java.util.LinkedList

class Repository<T>(
    private val klass: Class<T>,
    private val db: Database,
) {
    /**
     * Returns the record that has the id provided. If no record is found, returns null.
     */
    fun find(id: Long): T? {
        return findFirst(String.format("where %s=?", getIdName()), id.toString())
    }

    /**
     * Returns all records matching the given SQL query.
     *
     * The query should only contain the "where" part of the SQL query, and optionally the "order
     * by" part. "Group by" is not allowed. If no matching records are found, returns an empty list.
     */
    fun findAll(query: String, vararg params: String): List<T> {
        db.query(buildSelectQuery() + query, *params).use { c -> return cursorToMultipleRecords(c) }
    }

    /**
     * Returns the first record matching the given SQL query. See findAll for more details about
     * the parameters.
     */
    fun findFirst(query: String, vararg params: String): T? {
        db.query(buildSelectQuery() + query, *params).use { c ->
            return if (!c.moveToNext()) null else cursorToSingleRecord(c)
        }
    }

    /**
     * Executes the given SQL query on the repository.
     *
     * The query can be of any kind. For example, complex deletes and updates are allowed. The
     * repository does not perform any checks to guarantee that the query is valid, however the
     * underlying database might.
     */
    fun execSQL(query: String, vararg params: Any) {
        db.execute(query, *params)
    }

    /**
     * Executes the given callback inside a database transaction.
     *
     * If the callback terminates without throwing any exceptions, the transaction is considered
     * successful. If any exceptions are thrown, the transaction is aborted. Nesting transactions
     * is not allowed.
     */
    fun executeAsTransaction(callback: Runnable) {
        db.beginTransaction()
        try {
            callback.run()
            db.setTransactionSuccessful()
        } catch (e: Exception) {
            throw RuntimeException(e)
        } finally {
            db.endTransaction()
        }
    }

    /**
     * Saves the record on the database.
     *
     * If the id of the given record is null, it is assumed that the record has not been inserted
     * in the repository yet. The record will be inserted, a new id will be automatically generated,
     * and the id of the given record will be updated.
     *
     * If the given record has a non-null id, then an update will be performed instead. That is,
     * the previous record will be overwritten by the one provided.
     */
    fun save(record: T) {
        try {
            val fields = getFields()
            val columns = getColumnNames()
            val values: MutableMap<String, Any?> = HashMap()
            for (i in fields.indices) values[columns[i]] = fields[i][record]
            var id = getIdField()[record] as Long?
            var affectedRows = 0
            if (id != null) {
                affectedRows = db.update(getTableName(), values, "${getIdName()}=?", id.toString())
            }
            if (id == null || affectedRows == 0) {
                id = db.insert(getTableName(), values)
                getIdField()[record] = id
            }
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    /**
     * Removes the given record from the repository. The id of the given record is also set to null.
     */
    fun remove(record: T) {
        try {
            val id = getIdField()[record] as Long?
            db.delete(getTableName(), "${getIdName()}=?", id.toString())
            getIdField()[record] = null
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    private fun cursorToMultipleRecords(c: Cursor): List<T> {
        val records: MutableList<T> = LinkedList()
        while (c.moveToNext()) records.add(cursorToSingleRecord(c))
        return records
    }

    @Suppress("UNCHECKED_CAST")
    private fun cursorToSingleRecord(cursor: Cursor): T {
        return try {
            val constructor = klass.declaredConstructors[0]
            constructor.isAccessible = true
            val record = constructor.newInstance() as T
            var index = 0
            for (field in getFields()) copyFieldFromCursor(record, field, cursor, index++)
            record
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    private fun copyFieldFromCursor(record: T, field: Field, c: Cursor, index: Int) {
        when {
            field.type.isAssignableFrom(java.lang.Integer::class.java) -> field[record] = c.getInt(index)
            field.type.isAssignableFrom(java.lang.Long::class.java) -> field[record] = c.getLong(index)
            field.type.isAssignableFrom(java.lang.Double::class.java) -> field[record] = c.getDouble(index)
            field.type.isAssignableFrom(java.lang.String::class.java) -> field[record] = c.getString(index)
            else -> throw RuntimeException("Type not supported: ${field.type.name} ${field.name}")
        }
    }

    private fun buildSelectQuery(): String {
        return String.format("select %s from %s ", StringUtils.join(getColumnNames(), ", "), getTableName())
    }

    private val fieldColumnPairs: List<Pair<Field, Column>>
        get() {
            val fields: MutableList<Pair<Field, Column>> = ArrayList()
            for (f in klass.declaredFields) {
                f.isAccessible = true
                for (annotation in f.annotations) {
                    if (annotation !is Column) continue
                    fields.add(ImmutablePair(f, annotation))
                }
            }
            return fields
        }

    private var cacheFields: Array<Field>? = null

    private fun getFields(): Array<Field> {
        if (cacheFields == null) {
            val fields: MutableList<Field> = ArrayList()
            val columns = fieldColumnPairs
            for (pair in columns) fields.add(pair.left)
            cacheFields = fields.toTypedArray()
        }
        return cacheFields!!
    }

    private var cacheColumnNames: Array<String>? = null

    private fun getColumnNames(): Array<String> {
        if (cacheColumnNames == null) {
            val names: MutableList<String> = ArrayList()
            val columns = fieldColumnPairs
            for (pair in columns) {
                var cname = pair.right.name
                if (cname.isEmpty()) cname = pair.left.name
                if (names.contains(cname)) throw RuntimeException("duplicated column : $cname")
                names.add(cname)
            }
            cacheColumnNames = names.toTypedArray()
        }
        return cacheColumnNames!!
    }

    private var cacheTableName: String? = null

    private fun getTableName(): String {
        if (cacheTableName == null) {
            val name = getTableAnnotation().name
            if (name.isEmpty()) throw RuntimeException("Table name is empty")
            cacheTableName = name
        }
        return cacheTableName!!
    }

    private var cacheIdName: String? = null

    private fun getIdName(): String {
        if (cacheIdName == null) {
            val id = getTableAnnotation().id
            if (id.isEmpty()) throw RuntimeException("Table id is empty")
            cacheIdName = id
        }
        return cacheIdName!!
    }

    private var cacheIdField: Field? = null

    private fun getIdField(): Field {
        if (cacheIdField == null) {
            val fields = getFields()
            val idName = getIdName()
            for (f in fields) if (f.name == idName) {
                cacheIdField = f
                break
            }
            if (cacheIdField == null) throw RuntimeException("Field not found: $idName")
        }
        return cacheIdField!!
    }

    private fun getTableAnnotation(): Table {
        var t: Table? = null
        for (annotation in klass.annotations) {
            if (annotation !is Table) continue
            t = annotation
            break
        }
        if (t == null) throw RuntimeException("Table annotation not found")
        return t
    }
}
