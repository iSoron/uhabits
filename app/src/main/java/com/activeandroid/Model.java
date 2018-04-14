package com.activeandroid;

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

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;
import com.activeandroid.serializer.TypeSerializer;
import com.activeandroid.util.Log;
import com.activeandroid.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("unchecked")
public abstract class Model {

	/** Prime number used for hashcode() implementation. */
	private static final int HASH_PRIME = 739;

	//////////////////////////////////////////////////////////////////////////////////////
	// PRIVATE MEMBERS
	//////////////////////////////////////////////////////////////////////////////////////

	private Long mId = null;

	private final TableInfo mTableInfo;
	private final String idName;
	//////////////////////////////////////////////////////////////////////////////////////
	// CONSTRUCTORS
	//////////////////////////////////////////////////////////////////////////////////////

	public Model() {
		mTableInfo = Cache.getTableInfo(getClass());
		idName = mTableInfo.getIdName();
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// PUBLIC METHODS
	//////////////////////////////////////////////////////////////////////////////////////

	public final Long getId() {
		return mId;
	}

	public final void delete() {
		Cache.openDatabase().delete(mTableInfo.getTableName(), idName+"=?", new String[] { getId().toString() });
		Cache.removeEntity(this);
	}

	public final Long save() {
		final SQLiteDatabase db = Cache.openDatabase();
		final ContentValues values = new ContentValues();

		for (Field field : mTableInfo.getFields()) {
			final String fieldName = mTableInfo.getColumnName(field);
			Class<?> fieldType = field.getType();

			field.setAccessible(true);

			try {
				Object value = field.get(this);

				if (value != null) {
					final TypeSerializer typeSerializer = Cache.getParserForType(fieldType);
					if (typeSerializer != null) {
						// serialize data
						value = typeSerializer.serialize(value);
						// set new object type
						if (value != null) {
							fieldType = value.getClass();
							// check that the serializer returned what it promised
							if (!fieldType.equals(typeSerializer.getSerializedType())) {
								Log.w(String.format("TypeSerializer returned wrong type: expected a %s but got a %s",
										typeSerializer.getSerializedType(), fieldType));
							}
						}
					}
				}

				// TODO: Find a smarter way to do this? This if block is necessary because we
				// can't know the type until runtime.
				if (value == null) {
					values.putNull(fieldName);
				}
				else if (fieldType.equals(Byte.class) || fieldType.equals(byte.class)) {
					values.put(fieldName, (Byte) value);
				}
				else if (fieldType.equals(Short.class) || fieldType.equals(short.class)) {
					values.put(fieldName, (Short) value);
				}
				else if (fieldType.equals(Integer.class) || fieldType.equals(int.class)) {
					values.put(fieldName, (Integer) value);
				}
				else if (fieldType.equals(Long.class) || fieldType.equals(long.class)) {
					values.put(fieldName, (Long) value);
				}
				else if (fieldType.equals(Float.class) || fieldType.equals(float.class)) {
					values.put(fieldName, (Float) value);
				}
				else if (fieldType.equals(Double.class) || fieldType.equals(double.class)) {
					values.put(fieldName, (Double) value);
				}
				else if (fieldType.equals(Boolean.class) || fieldType.equals(boolean.class)) {
					values.put(fieldName, (Boolean) value);
				}
				else if (fieldType.equals(Character.class) || fieldType.equals(char.class)) {
					values.put(fieldName, value.toString());
				}
				else if (fieldType.equals(String.class)) {
					values.put(fieldName, value.toString());
				}
				else if (fieldType.equals(Byte[].class) || fieldType.equals(byte[].class)) {
					values.put(fieldName, (byte[]) value);
				}
				else if (ReflectionUtils.isModel(fieldType)) {
					values.put(fieldName, ((Model) value).getId());
				}
				else if (ReflectionUtils.isSubclassOf(fieldType, Enum.class)) {
					values.put(fieldName, ((Enum<?>) value).name());
				}
			}
			catch (IllegalArgumentException e) {
				Log.e(e.getClass().getName(), e);
			}
			catch (IllegalAccessException e) {
				Log.e(e.getClass().getName(), e);
			}
		}

		if (mId == null) {
			mId = db.insert(mTableInfo.getTableName(), null, values);
		}
		else {
			db.update(mTableInfo.getTableName(), values, idName+"=" + mId, null);
		}

		return mId;
	}

	// Convenience methods

	public static void delete(Class<? extends Model> type, long id) {
		TableInfo tableInfo = Cache.getTableInfo(type);
		new Delete().from(type).where(tableInfo.getIdName()+"=?", id).execute();
	}

	public static <T extends Model> T load(Class<T> type, long id) {
		TableInfo tableInfo = Cache.getTableInfo(type);
		return (T) new Select().from(type).where(tableInfo.getIdName()+"=?", id).executeSingle();
	}

	// Model population

	public final void loadFromCursor(Cursor cursor) {
        /**
         * Obtain the columns ordered to fix issue #106 (https://github.com/pardom/ActiveAndroid/issues/106)
         * when the cursor have multiple columns with same name obtained from join tables.
         */
        List<String> columnsOrdered = new ArrayList<String>(Arrays.asList(cursor.getColumnNames()));
		for (Field field : mTableInfo.getFields()) {
			final String fieldName = mTableInfo.getColumnName(field);
			Class<?> fieldType = field.getType();
			final int columnIndex = columnsOrdered.indexOf(fieldName);

			if (columnIndex < 0) {
				continue;
			}

			field.setAccessible(true);

			try {
				boolean columnIsNull = cursor.isNull(columnIndex);
				TypeSerializer typeSerializer = Cache.getParserForType(fieldType);
				Object value = null;

				if (typeSerializer != null) {
					fieldType = typeSerializer.getSerializedType();
				}

				// TODO: Find a smarter way to do this? This if block is necessary because we
				// can't know the type until runtime.
				if (columnIsNull) {
					field = null;
				}
				else if (fieldType.equals(Byte.class) || fieldType.equals(byte.class)) {
					value = cursor.getInt(columnIndex);
				}
				else if (fieldType.equals(Short.class) || fieldType.equals(short.class)) {
					value = cursor.getInt(columnIndex);
				}
				else if (fieldType.equals(Integer.class) || fieldType.equals(int.class)) {
					value = cursor.getInt(columnIndex);
				}
				else if (fieldType.equals(Long.class) || fieldType.equals(long.class)) {
					value = cursor.getLong(columnIndex);
				}
				else if (fieldType.equals(Float.class) || fieldType.equals(float.class)) {
					value = cursor.getFloat(columnIndex);
				}
				else if (fieldType.equals(Double.class) || fieldType.equals(double.class)) {
					value = cursor.getDouble(columnIndex);
				}
				else if (fieldType.equals(Boolean.class) || fieldType.equals(boolean.class)) {
					value = cursor.getInt(columnIndex) != 0;
				}
				else if (fieldType.equals(Character.class) || fieldType.equals(char.class)) {
					value = cursor.getString(columnIndex).charAt(0);
				}
				else if (fieldType.equals(String.class)) {
					value = cursor.getString(columnIndex);
				}
				else if (fieldType.equals(Byte[].class) || fieldType.equals(byte[].class)) {
					value = cursor.getBlob(columnIndex);
				}
				else if (ReflectionUtils.isModel(fieldType)) {
					final long entityId = cursor.getLong(columnIndex);
					final Class<? extends Model> entityType = (Class<? extends Model>) fieldType;

					Model entity = Cache.getEntity(entityType, entityId);
					if (entity == null) {
						entity = new Select().from(entityType).where(idName+"=?", entityId).executeSingle();
					}

					value = entity;
				}
				else if (ReflectionUtils.isSubclassOf(fieldType, Enum.class)) {
					@SuppressWarnings("rawtypes")
					final Class<? extends Enum> enumType = (Class<? extends Enum>) fieldType;
					value = Enum.valueOf(enumType, cursor.getString(columnIndex));
				}

				// Use a deserializer if one is available
				if (typeSerializer != null && !columnIsNull) {
					value = typeSerializer.deserialize(value);
				}

				// Set the field value
				if (value != null) {
					field.set(this, value);
				}
			}
			catch (IllegalArgumentException e) {
				Log.e(e.getClass().getName(), e);
			}
			catch (IllegalAccessException e) {
				Log.e(e.getClass().getName(), e);
			}
			catch (SecurityException e) {
				Log.e(e.getClass().getName(), e);
			}
		}

		if (mId != null) {
			Cache.addEntity(this);
		}
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// PROTECTED METHODS
	//////////////////////////////////////////////////////////////////////////////////////

	protected final <T extends Model> List<T> getMany(Class<T> type, String foreignKey) {
		return new Select().from(type).where(Cache.getTableName(type) + "." + foreignKey + "=?", getId()).execute();
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// OVERRIDEN METHODS
	//////////////////////////////////////////////////////////////////////////////////////

	@Override
	public String toString() {
		return mTableInfo.getTableName() + "@" + getId();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Model && this.mId != null) {
			final Model other = (Model) obj;

			return this.mId.equals(other.mId)							
							&& (this.mTableInfo.getTableName().equals(other.mTableInfo.getTableName()));
		} else {
			return this == obj;
		}
	}

	@Override
	public int hashCode() {
		int hash = HASH_PRIME;
		hash += HASH_PRIME * (mId == null ? super.hashCode() : mId.hashCode()); //if id is null, use Object.hashCode()
		hash += HASH_PRIME * mTableInfo.getTableName().hashCode();
		return hash; //To change body of generated methods, choose Tools | Templates.
	}
}
