package com.activeandroid.query;

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

import android.text.TextUtils;

import com.activeandroid.Model;

public final class Select implements Sqlable {
	private String[] mColumns;
	private boolean mDistinct = false;
	private boolean mAll = false;

	public Select() {
	}

	public Select(String... columns) {
		mColumns = columns;
	}

	public Select(Column... columns) {
		final int size = columns.length;
		mColumns = new String[size];
		for (int i = 0; i < size; i++) {
			mColumns[i] = columns[i].name + " AS " + columns[i].alias;
		}
	}

	public Select distinct() {
		mDistinct = true;
		mAll = false;

		return this;
	}

	public Select all() {
		mDistinct = false;
		mAll = true;

		return this;
	}

	public From from(Class<? extends Model> table) {
		return new From(table, this);
	}

	public static class Column {
		String name;
		String alias;

		public Column(String name, String alias) {
			this.name = name;
			this.alias = alias;
		}
	}

	@Override
	public String toSql() {
		StringBuilder sql = new StringBuilder();

		sql.append("SELECT ");

		if (mDistinct) {
			sql.append("DISTINCT ");
		}
		else if (mAll) {
			sql.append("ALL ");
		}

		if (mColumns != null && mColumns.length > 0) {
			sql.append(TextUtils.join(", ", mColumns) + " ");
		}
		else {
			sql.append("* ");
		}

		return sql.toString();
	}
}