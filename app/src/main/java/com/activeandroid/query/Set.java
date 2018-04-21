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

import com.activeandroid.util.SQLiteUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class Set implements Sqlable {
	private Update mUpdate;

	private String mSet;
	private String mWhere;

	private List<Object> mSetArguments;
	private List<Object> mWhereArguments;

	public Set(Update queryBase, String set) {
		mUpdate = queryBase;
		mSet = set;

		mSetArguments = new ArrayList<Object>();
		mWhereArguments = new ArrayList<Object>();
	}

	public Set(Update queryBase, String set, Object... args) {
		mUpdate = queryBase;
		mSet = set;

		mSetArguments = new ArrayList<Object>();
		mWhereArguments = new ArrayList<Object>();

		mSetArguments.addAll(Arrays.asList(args));
	}

	public Set where(String where) {
		mWhere = where;
		mWhereArguments.clear();

		return this;
	}

	public Set where(String where, Object... args) {
		mWhere = where;
		mWhereArguments.clear();
		mWhereArguments.addAll(Arrays.asList(args));

		return this;
	}

	@Override
	public String toSql() {
		StringBuilder sql = new StringBuilder();
		sql.append(mUpdate.toSql());
		sql.append("SET ");
		sql.append(mSet);
		sql.append(" ");

		if (mWhere != null) {
			sql.append("WHERE ");
			sql.append(mWhere);
			sql.append(" ");
		}

		return sql.toString();
	}

	public void execute() {
		SQLiteUtils.execSql(toSql(), getArguments());
	}

	public String[] getArguments() {
		final int setSize = mSetArguments.size();
		final int whereSize = mWhereArguments.size();
		final String[] args = new String[setSize + whereSize];

		for (int i = 0; i < setSize; i++) {
			args[i] = mSetArguments.get(i).toString();
		}

		for (int i = 0; i < whereSize; i++) {
			args[i + setSize] = mWhereArguments.get(i).toString();
		}

		return args;
	}
}
