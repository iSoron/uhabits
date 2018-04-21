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

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Set;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.serializer.TypeSerializer;

public final class ReflectionUtils {
	//////////////////////////////////////////////////////////////////////////////////////
	// PUBLIC METHODS
	//////////////////////////////////////////////////////////////////////////////////////

	public static boolean isModel(Class<?> type) {
		return isSubclassOf(type, Model.class) && (!Modifier.isAbstract(type.getModifiers()));
	}

	public static boolean isTypeSerializer(Class<?> type) {
		return isSubclassOf(type, TypeSerializer.class);
	}

	// Meta-data

	@SuppressWarnings("unchecked")
	public static <T> T getMetaData(Context context, String name) {
		try {
			final ApplicationInfo ai = context.getPackageManager().getApplicationInfo(context.getPackageName(),
					PackageManager.GET_META_DATA);

			if (ai.metaData != null) {
				return (T) ai.metaData.get(name);
			}
		}
		catch (Exception e) {
			Log.w("Couldn't find meta-data: " + name);
		}

		return null;
	}
	
	public static Set<Field> getDeclaredColumnFields(Class<?> type) {
		Set<Field> declaredColumnFields = Collections.emptySet();
		
		if (ReflectionUtils.isSubclassOf(type, Model.class) || Model.class.equals(type)) {
			declaredColumnFields = new LinkedHashSet<Field>();
			
			Field[] fields = type.getDeclaredFields();
			Arrays.sort(fields, new Comparator<Field>() {
				@Override
				public int compare(Field field1, Field field2) {
					return field2.getName().compareTo(field1.getName());
				}
			});
			for (Field field : fields) {
				if (field.isAnnotationPresent(Column.class)) {
					declaredColumnFields.add(field);
				}
			}
	
			Class<?> parentType = type.getSuperclass();
			if (parentType != null) {
				declaredColumnFields.addAll(getDeclaredColumnFields(parentType));
			}
		}
		
		return declaredColumnFields;		
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// PRIVATE METHODS
	//////////////////////////////////////////////////////////////////////////////////////

	public static boolean isSubclassOf(Class<?> type, Class<?> superClass) {
		if (type.getSuperclass() != null) {
			if (type.getSuperclass().equals(superClass)) {
				return true;
			}

			return isSubclassOf(type.getSuperclass(), superClass);
		}

		return false;
	}
}