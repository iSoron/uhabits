package com.activeandroid.annotation;

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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Column {
	public enum ConflictAction {
		ROLLBACK, ABORT, FAIL, IGNORE, REPLACE
	}

	public enum ForeignKeyAction {
		SET_NULL, SET_DEFAULT, CASCADE, RESTRICT, NO_ACTION
	}

	public String name() default "";

	public int length() default -1;

	public boolean notNull() default false;

	public ConflictAction onNullConflict() default ConflictAction.FAIL;

	public ForeignKeyAction onDelete() default ForeignKeyAction.NO_ACTION;

	public ForeignKeyAction onUpdate() default ForeignKeyAction.NO_ACTION;

	public boolean unique() default false;

	public ConflictAction onUniqueConflict() default ConflictAction.FAIL;

	/*
	 * If set uniqueGroups = {"group_name"}, we will create a table constraint with group.
	 *
	 * Example:
	 *
	 * @Table(name = "table_name")
	 * public class Table extends Model {
	 *     @Column(name = "member1", uniqueGroups = {"group1"}, onUniqueConflicts = {ConflictAction.FAIL})
	 *     public String member1;
	 *
	 *     @Column(name = "member2", uniqueGroups = {"group1", "group2"}, onUniqueConflicts = {ConflictAction.FAIL, ConflictAction.IGNORE})
	 *     public String member2;
	 *
	 *     @Column(name = "member3", uniqueGroups = {"group2"}, onUniqueConflicts = {ConflictAction.IGNORE})
	 *     public String member3;
	 * }
	 *
	 * CREATE TABLE table_name (..., UNIQUE (member1, member2) ON CONFLICT FAIL, UNIQUE (member2, member3) ON CONFLICT IGNORE)
	 */
	public String[] uniqueGroups() default {};

	public ConflictAction[] onUniqueConflicts() default {};

	/*
	 * If set index = true, we will create a index with single column.
	 *
	 * Example:
	 *
	 * @Table(name = "table_name")
	 * public class Table extends Model {
	 *     @Column(name = "member", index = true)
	 *     public String member;
	 * }
	 *
	 * Execute CREATE INDEX index_table_name_member on table_name(member)
	 */
	public boolean index() default false;

	/*
	 * If set indexGroups = {"group_name"}, we will create a index with group.
	 *
	 * Example:
	 *
	 * @Table(name = "table_name")
	 * public class Table extends Model {
	 *     @Column(name = "member1", indexGroups = {"group1"})
	 *     public String member1;
	 *
	 *     @Column(name = "member2", indexGroups = {"group1", "group2"})
	 *     public String member2;
	 *
	 *     @Column(name = "member3", indexGroups = {"group2"})
	 *     public String member3;
	 * }
	 *
	 * Execute CREATE INDEX index_table_name_group1 on table_name(member1, member2)
	 * Execute CREATE INDEX index_table_name_group2 on table_name(member2, member3)
	 */
	public String[] indexGroups() default {};
}
