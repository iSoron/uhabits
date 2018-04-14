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

import com.activeandroid.Cache;
import com.activeandroid.Model;

public final class Join implements Sqlable {
	static enum JoinType {
		LEFT, OUTER, INNER, CROSS
	}

	private From mFrom;
	private Class<? extends Model> mType;
	private String mAlias;
	private JoinType mJoinType;
	private String mOn;
	private String[] mUsing;

	Join(From from, Class<? extends Model> table, JoinType joinType) {
		mFrom = from;
		mType = table;
		mJoinType = joinType;
	}

	public Join as(String alias) {
		mAlias = alias;
		return this;
	}

	public From on(String on) {
		mOn = on;
		return mFrom;
	}

	public From on(String on, Object... args) {
		mOn = on;
		mFrom.addArguments(args);
		return mFrom;
	}

	public From using(String... columns) {
		mUsing = columns;
		return mFrom;
	}

	@Override
	public String toSql() {
		StringBuilder sql = new StringBuilder();

		if (mJoinType != null) {
			sql.append(mJoinType.toString()).append(" ");
		}

		sql.append("JOIN ");
		sql.append(Cache.getTableName(mType));
		sql.append(" ");

		if (mAlias != null) {
			sql.append("AS ");
			sql.append(mAlias);
			sql.append(" ");
		}

		if (mOn != null) {
			sql.append("ON ");
			sql.append(mOn);
			sql.append(" ");
		}
		else if (mUsing != null) {
			sql.append("USING (");
			sql.append(TextUtils.join(", ", mUsing));
			sql.append(") ");
		}

		return sql.toString();
	}
}
