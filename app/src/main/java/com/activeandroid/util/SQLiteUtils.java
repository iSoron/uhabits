package com.activeandroid.util;

/*
 * Copyright (C) 2010 Michael Pardo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.database.Cursor;
import android.os.Build;
import android.text.TextUtils;

import com.activeandroid.Cache;
import com.activeandroid.Model;
import com.activeandroid.TableInfo;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Column.ConflictAction;
import com.activeandroid.serializer.TypeSerializer;

import java.lang.Long;
import java.lang.String;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class SQLiteUtils {
	//////////////////////////////////////////////////////////////////////////////////////
	// ENUMERATIONS
	//////////////////////////////////////////////////////////////////////////////////////

	public enum SQLiteType {
		INTEGER, REAL, TEXT, BLOB
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// PUBLIC CONSTANTS
	//////////////////////////////////////////////////////////////////////////////////////

	public static final boolean FOREIGN_KEYS_SUPPORTED = Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO;

	//////////////////////////////////////////////////////////////////////////////////////
	// PRIVATE CONTSANTS
	//////////////////////////////////////////////////////////////////////////////////////

	@SuppressWarnings("serial")
	private static final HashMap<Class<?>, SQLiteType> TYPE_MAP = new HashMap<Class<?>, SQLiteType>() {
		{
			put(byte.class, SQLiteType.INTEGER);
			put(short.class, SQLiteType.INTEGER);
			put(int.class, SQLiteType.INTEGER);
			put(long.class, SQLiteType.INTEGER);
			put(float.class, SQLiteType.REAL);
			put(double.class, SQLiteType.REAL);
			put(boolean.class, SQLiteType.INTEGER);
			put(char.class, SQLiteType.TEXT);
			put(byte[].class, SQLiteType.BLOB);
			put(Byte.class, SQLiteType.INTEGER);
			put(Short.class, SQLiteType.INTEGER);
			put(Integer.class, SQLiteType.INTEGER);
			put(Long.class, SQLiteType.INTEGER);
			put(Float.class, SQLiteType.REAL);
			put(Double.class, SQLiteType.REAL);
			put(Boolean.class, SQLiteType.INTEGER);
			put(Character.class, SQLiteType.TEXT);
			put(String.class, SQLiteType.TEXT);
			put(Byte[].class, SQLiteType.BLOB);
		}
	};

	//////////////////////////////////////////////////////////////////////////////////////
	// PRIVATE MEMBERS
	//////////////////////////////////////////////////////////////////////////////////////

	private static HashMap<String, List<String>> sIndexGroupMap;
	private static HashMap<String, List<String>> sUniqueGroupMap;
	private static HashMap<String, ConflictAction> sOnUniqueConflictsMap;

	//////////////////////////////////////////////////////////////////////////////////////
	// PUBLIC METHODS
	//////////////////////////////////////////////////////////////////////////////////////

	public static void execSql(String sql) {
		Cache.openDatabase().execSQL(sql);
	}

	public static void execSql(String sql, Object[] bindArgs) {
		Cache.openDatabase().execSQL(sql, bindArgs);
	}

	public static <T extends Model> List<T> rawQuery(Class<? extends Model> type, String sql, String[] selectionArgs) {
		Cursor cursor = Cache.openDatabase().rawQuery(sql, selectionArgs);
		List<T> entities = processCursor(type, cursor);
		cursor.close();

		return entities;
	}
	  
	public static int intQuery(final String sql, final String[] selectionArgs) {
        final Cursor cursor = Cache.openDatabase().rawQuery(sql, selectionArgs);
        final int number = processIntCursor(cursor);
        cursor.close();

        return number;
	}

	public static <T extends Model> T rawQuerySingle(Class<? extends Model> type, String sql, String[] selectionArgs) {
		List<T> entities = rawQuery(type, sql, selectionArgs);

		if (entities.size() > 0) {
			return entities.get(0);
		}

		return null;
	}

	// Database creation

	public static ArrayList<String> createUniqueDefinition(TableInfo tableInfo) {
		final ArrayList<String> definitions = new ArrayList<String>();
		sUniqueGroupMap = new HashMap<String, List<String>>();
		sOnUniqueConflictsMap = new HashMap<String, ConflictAction>();

		for (Field field : tableInfo.getFields()) {
			createUniqueColumnDefinition(tableInfo, field);
		}

		if (sUniqueGroupMap.isEmpty()) {
			return definitions;
		}

		Set<String> keySet = sUniqueGroupMap.keySet();
		for (String key : keySet) {
			List<String> group = sUniqueGroupMap.get(key);
			ConflictAction conflictAction = sOnUniqueConflictsMap.get(key);

			definitions.add(String.format("UNIQUE (%s) ON CONFLICT %s",
					TextUtils.join(", ", group), conflictAction.toString()));
		}

		return definitions;
	}

	public static void createUniqueColumnDefinition(TableInfo tableInfo, Field field) {
		final String name = tableInfo.getColumnName(field);
		final Column column = field.getAnnotation(Column.class);

        if (field.getName().equals("mId")) {
            return;
        }

		String[] groups = column.uniqueGroups();
		ConflictAction[] conflictActions = column.onUniqueConflicts();
		if (groups.length != conflictActions.length)
			return;

		for (int i = 0; i < groups.length; i++) {
			String group = groups[i];
			ConflictAction conflictAction = conflictActions[i];

			if (TextUtils.isEmpty(group))
				continue;

			List<String> list = sUniqueGroupMap.get(group);
			if (list == null) {
				list = new ArrayList<String>();
			}
			list.add(name);

			sUniqueGroupMap.put(group, list);
			sOnUniqueConflictsMap.put(group, conflictAction);
		}
	}

	public static String[] createIndexDefinition(TableInfo tableInfo) {
		final ArrayList<String> definitions = new ArrayList<String>();
		sIndexGroupMap = new HashMap<String, List<String>>();

		for (Field field : tableInfo.getFields()) {
			createIndexColumnDefinition(tableInfo, field);
		}

		if (sIndexGroupMap.isEmpty()) {
			return new String[0];
		}

		for (Map.Entry<String, List<String>> entry : sIndexGroupMap.entrySet()) {
			definitions.add(String.format("CREATE INDEX IF NOT EXISTS %s on %s(%s);",
					"index_" + tableInfo.getTableName() + "_" + entry.getKey(),
					tableInfo.getTableName(), TextUtils.join(", ", entry.getValue())));
		}

		return definitions.toArray(new String[definitions.size()]);
	}

	public static void createIndexColumnDefinition(TableInfo tableInfo, Field field) {
		final String name = tableInfo.getColumnName(field);
		final Column column = field.getAnnotation(Column.class);

        if (field.getName().equals("mId")) {
            return;
        }

		if (column.index()) {
			List<String> list = new ArrayList<String>();
			list.add(name);
			sIndexGroupMap.put(name, list);
		}

		String[] groups = column.indexGroups();
		for (String group : groups) {
			if (TextUtils.isEmpty(group))
				continue;

			List<String> list = sIndexGroupMap.get(group);
			if (list == null) {
				list = new ArrayList<String>();
			}

			list.add(name);
			sIndexGroupMap.put(group, list);
		}
	}

	public static String createTableDefinition(TableInfo tableInfo) {
		final ArrayList<String> definitions = new ArrayList<String>();

		for (Field field : tableInfo.getFields()) {
			String definition = createColumnDefinition(tableInfo, field);
			if (!TextUtils.isEmpty(definition)) {
				definitions.add(definition);
			}
		}

		definitions.addAll(createUniqueDefinition(tableInfo));

		return String.format("CREATE TABLE IF NOT EXISTS %s (%s);", tableInfo.getTableName(),
				TextUtils.join(", ", definitions));
	}

	@SuppressWarnings("unchecked")
	public static String createColumnDefinition(TableInfo tableInfo, Field field) {
		StringBuilder definition = new StringBuilder();

		Class<?> type = field.getType();
		final String name = tableInfo.getColumnName(field);
		final TypeSerializer typeSerializer = Cache.getParserForType(field.getType());
		final Column column = field.getAnnotation(Column.class);

		if (typeSerializer != null) {
			type = typeSerializer.getSerializedType();
		}

		if (TYPE_MAP.containsKey(type)) {
			definition.append(name);
			definition.append(" ");
			definition.append(TYPE_MAP.get(type).toString());
		}
		else if (ReflectionUtils.isModel(type)) {
			definition.append(name);
			definition.append(" ");
			definition.append(SQLiteType.INTEGER.toString());
		}
		else if (ReflectionUtils.isSubclassOf(type, Enum.class)) {
			definition.append(name);
			definition.append(" ");
			definition.append(SQLiteType.TEXT.toString());
		}

		if (!TextUtils.isEmpty(definition)) {

			if (name.equals(tableInfo.getIdName())) {
				definition.append(" PRIMARY KEY AUTOINCREMENT");
			}else if(column!=null){
				if (column.length() > -1) {
					definition.append("(");
					definition.append(column.length());
					definition.append(")");
				}

				if (column.notNull()) {
					definition.append(" NOT NULL ON CONFLICT ");
					definition.append(column.onNullConflict().toString());
				}

				if (column.unique()) {
					definition.append(" UNIQUE ON CONFLICT ");
					definition.append(column.onUniqueConflict().toString());
				}
			}

			if (FOREIGN_KEYS_SUPPORTED && ReflectionUtils.isModel(type)) {
				definition.append(" REFERENCES ");
				definition.append(Cache.getTableInfo((Class<? extends Model>) type).getTableName());
				definition.append("("+tableInfo.getIdName()+")");
				definition.append(" ON DELETE ");
				definition.append(column.onDelete().toString().replace("_", " "));
				definition.append(" ON UPDATE ");
				definition.append(column.onUpdate().toString().replace("_", " "));
			}
		}
		else {
			Log.e("No type mapping for: " + type.toString());
		}

		return definition.toString();
	}

	@SuppressWarnings("unchecked")
	public static <T extends Model> List<T> processCursor(Class<? extends Model> type, Cursor cursor) {
		TableInfo tableInfo = Cache.getTableInfo(type);
		String idName = tableInfo.getIdName();
		final List<T> entities = new ArrayList<T>();

		try {
			Constructor<?> entityConstructor = type.getConstructor();

			if (cursor.moveToFirst()) {
                /**
                 * Obtain the columns ordered to fix issue #106 (https://github.com/pardom/ActiveAndroid/issues/106)
                 * when the cursor have multiple columns with same name obtained from join tables.
                 */
                List<String> columnsOrdered = new ArrayList<String>(Arrays.asList(cursor.getColumnNames()));
				do {
					Model entity = Cache.getEntity(type, cursor.getLong(columnsOrdered.indexOf(idName)));
					if (entity == null) {
						entity = (T) entityConstructor.newInstance();
					}

					entity.loadFromCursor(cursor);
					entities.add((T) entity);
				}
				while (cursor.moveToNext());
			}

		}
		catch (NoSuchMethodException e) {
			throw new RuntimeException(
                "Your model " + type.getName() + " does not define a default " +
                "constructor. The default constructor is required for " +
                "now in ActiveAndroid models, as the process to " +
                "populate the ORM model is : " +
                "1. instantiate default model " +
                "2. populate fields"
            );
		}
		catch (Exception e) {
			Log.e("Failed to process cursor.", e);
		}

		return entities;
	}

	private static int processIntCursor(final Cursor cursor) {
        if (cursor.moveToFirst()) {
            return cursor.getInt(0);
	    }
        return 0;
    }

	public static List<String> lexSqlScript(String sqlScript) {
		ArrayList<String> sl = new ArrayList<String>();
		boolean inString = false, quoteNext = false;
		StringBuilder b = new StringBuilder(100);

		for (int i = 0; i < sqlScript.length(); i++) {
			char c = sqlScript.charAt(i);

			if (c == ';' && !inString && !quoteNext) {
				sl.add(b.toString());
				b = new StringBuilder(100);
				inString = false;
				quoteNext = false;
				continue;
			}

			if (c == '\'' && !quoteNext) {
				inString = !inString;
			}

			quoteNext = c == '\\' && !quoteNext;

			b.append(c);
		}

		if (b.length() > 0) {
			sl.add(b.toString());
		}

		return sl;
	}
}
