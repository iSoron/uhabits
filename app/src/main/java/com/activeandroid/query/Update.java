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

import com.activeandroid.Cache;
import com.activeandroid.Model;

public final class Update implements Sqlable {
	private Class<? extends Model> mType;

	public Update(Class<? extends Model> table) {
		mType = table;
	}

	public Set set(String set) {
		return new Set(this, set);
	}

	public Set set(String set, Object... args) {
		return new Set(this, set, args);
	}

	Class<? extends Model> getType() {
		return mType;
	}

	@Override
	public String toSql() {
		StringBuilder sql = new StringBuilder();
		sql.append("UPDATE ");
		sql.append(Cache.getTableName(mType));
		sql.append(" ");

		return sql.toString();
	}
}
