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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.content.Context;

import com.activeandroid.serializer.TypeSerializer;
import com.activeandroid.util.Log;
import com.activeandroid.util.ReflectionUtils;

public class Configuration {

    public final static String SQL_PARSER_LEGACY = "legacy";
    public final static String SQL_PARSER_DELIMITED = "delimited";

	//////////////////////////////////////////////////////////////////////////////////////
	// PRIVATE MEMBERS
	//////////////////////////////////////////////////////////////////////////////////////

	private Context mContext;
	private String mDatabaseName;
	private int mDatabaseVersion;
	private String mSqlParser;
	private List<Class<? extends Model>> mModelClasses;
	private List<Class<? extends TypeSerializer>> mTypeSerializers;
	private int mCacheSize;

	//////////////////////////////////////////////////////////////////////////////////////
	// CONSTRUCTORS
	//////////////////////////////////////////////////////////////////////////////////////

	private Configuration(Context context) {
		mContext = context;
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// PUBLIC METHODS
	//////////////////////////////////////////////////////////////////////////////////////

	public Context getContext() {
		return mContext;
	}

	public String getDatabaseName() {
		return mDatabaseName;
	}

	public int getDatabaseVersion() {
		return mDatabaseVersion;
	}
	
	public String getSqlParser() {
	    return mSqlParser;
	}

	public List<Class<? extends Model>> getModelClasses() {
		return mModelClasses;
	}

	public List<Class<? extends TypeSerializer>> getTypeSerializers() {
		return mTypeSerializers;
	}

	public int getCacheSize() {
		return mCacheSize;
	}

	public boolean isValid() {
		return mModelClasses != null && mModelClasses.size() > 0;
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// INNER CLASSES
	//////////////////////////////////////////////////////////////////////////////////////

	public static class Builder {
		//////////////////////////////////////////////////////////////////////////////////////
		// PRIVATE CONSTANTS
		//////////////////////////////////////////////////////////////////////////////////////

		private static final String AA_DB_NAME = "AA_DB_NAME";
		private static final String AA_DB_VERSION = "AA_DB_VERSION";
		private final static String AA_MODELS = "AA_MODELS";
		private final static String AA_SERIALIZERS = "AA_SERIALIZERS";
		private final static String AA_SQL_PARSER = "AA_SQL_PARSER";

		private static final int DEFAULT_CACHE_SIZE = 1024;
		private static final String DEFAULT_DB_NAME = "Application.db";
		private static final String DEFAULT_SQL_PARSER = SQL_PARSER_LEGACY;

		//////////////////////////////////////////////////////////////////////////////////////
		// PRIVATE MEMBERS
		//////////////////////////////////////////////////////////////////////////////////////

		private Context mContext;

		private Integer mCacheSize;
		private String mDatabaseName;
		private Integer mDatabaseVersion;
		private String mSqlParser;
		private List<Class<? extends Model>> mModelClasses;
		private List<Class<? extends TypeSerializer>> mTypeSerializers;

		//////////////////////////////////////////////////////////////////////////////////////
		// CONSTRUCTORS
		//////////////////////////////////////////////////////////////////////////////////////

		public Builder(Context context) {
			mContext = context.getApplicationContext();
			mCacheSize = DEFAULT_CACHE_SIZE;
		}

		//////////////////////////////////////////////////////////////////////////////////////
		// PUBLIC METHODS
		//////////////////////////////////////////////////////////////////////////////////////

		public Builder setCacheSize(int cacheSize) {
			mCacheSize = cacheSize;
			return this;
		}

		public Builder setDatabaseName(String databaseName) {
			mDatabaseName = databaseName;
			return this;
		}

		public Builder setDatabaseVersion(int databaseVersion) {
			mDatabaseVersion = databaseVersion;
			return this;
		}
		
		public Builder setSqlParser(String sqlParser) {
		    mSqlParser = sqlParser;
		    return this;
		}

		public Builder addModelClass(Class<? extends Model> modelClass) {
			if (mModelClasses == null) {
				mModelClasses = new ArrayList<Class<? extends Model>>();
			}

			mModelClasses.add(modelClass);
			return this;
		}

		public Builder addModelClasses(Class<? extends Model>... modelClasses) {
			if (mModelClasses == null) {
				mModelClasses = new ArrayList<Class<? extends Model>>();
			}

			mModelClasses.addAll(Arrays.asList(modelClasses));
			return this;
		}

		public Builder setModelClasses(Class<? extends Model>... modelClasses) {
			mModelClasses = Arrays.asList(modelClasses);
			return this;
		}

		public Builder addTypeSerializer(Class<? extends TypeSerializer> typeSerializer) {
			if (mTypeSerializers == null) {
				mTypeSerializers = new ArrayList<Class<? extends TypeSerializer>>();
			}

			mTypeSerializers.add(typeSerializer);
			return this;
		}

		public Builder addTypeSerializers(Class<? extends TypeSerializer>... typeSerializers) {
			if (mTypeSerializers == null) {
				mTypeSerializers = new ArrayList<Class<? extends TypeSerializer>>();
			}

			mTypeSerializers.addAll(Arrays.asList(typeSerializers));
			return this;
		}

		public Builder setTypeSerializers(Class<? extends TypeSerializer>... typeSerializers) {
			mTypeSerializers = Arrays.asList(typeSerializers);
			return this;
		}

		public Configuration create() {
			Configuration configuration = new Configuration(mContext);
			configuration.mCacheSize = mCacheSize;

			// Get database name from meta-data
			if (mDatabaseName != null) {
				configuration.mDatabaseName = mDatabaseName;
			} else {
				configuration.mDatabaseName = getMetaDataDatabaseNameOrDefault();
			}

			// Get database version from meta-data
			if (mDatabaseVersion != null) {
				configuration.mDatabaseVersion = mDatabaseVersion;
			} else {
				configuration.mDatabaseVersion = getMetaDataDatabaseVersionOrDefault();
			}

			// Get SQL parser from meta-data
			if (mSqlParser != null) {
			    configuration.mSqlParser = mSqlParser;
			} else {
			    configuration.mSqlParser = getMetaDataSqlParserOrDefault();
			}
			
			// Get model classes from meta-data
			if (mModelClasses != null) {
				configuration.mModelClasses = mModelClasses;
			} else {
				final String modelList = ReflectionUtils.getMetaData(mContext, AA_MODELS);
				if (modelList != null) {
					configuration.mModelClasses = loadModelList(modelList.split(","));
				}
			}

			// Get type serializer classes from meta-data
			if (mTypeSerializers != null) {
				configuration.mTypeSerializers = mTypeSerializers;
			} else {
				final String serializerList = ReflectionUtils.getMetaData(mContext, AA_SERIALIZERS);
				if (serializerList != null) {
					configuration.mTypeSerializers = loadSerializerList(serializerList.split(","));
				}
			}

			return configuration;
		}

		//////////////////////////////////////////////////////////////////////////////////////
		// PRIVATE METHODS
		//////////////////////////////////////////////////////////////////////////////////////

		// Meta-data methods

		private String getMetaDataDatabaseNameOrDefault() {
			String aaName = ReflectionUtils.getMetaData(mContext, AA_DB_NAME);
			if (aaName == null) {
				aaName = DEFAULT_DB_NAME;
			}

			return aaName;
		}

		private int getMetaDataDatabaseVersionOrDefault() {
			Integer aaVersion = ReflectionUtils.getMetaData(mContext, AA_DB_VERSION);
			if (aaVersion == null || aaVersion == 0) {
				aaVersion = 1;
			}

			return aaVersion;
		}

		private String getMetaDataSqlParserOrDefault() {
		    final String mode = ReflectionUtils.getMetaData(mContext, AA_SQL_PARSER);
		    if (mode == null) {
		        return DEFAULT_SQL_PARSER;
		    }
		    return mode;
		}

		private List<Class<? extends Model>> loadModelList(String[] models) {
			final List<Class<? extends Model>> modelClasses = new ArrayList<Class<? extends Model>>();
			final ClassLoader classLoader = mContext.getClass().getClassLoader();
			for (String model : models) {
				try {
					Class modelClass = Class.forName(model.trim(), false, classLoader);
					if (ReflectionUtils.isModel(modelClass)) {
						modelClasses.add(modelClass);
					}
				}
				catch (ClassNotFoundException e) {
					Log.e("Couldn't create class.", e);
				}
			}

			return modelClasses;
		}

		private List<Class<? extends TypeSerializer>> loadSerializerList(String[] serializers) {
			final List<Class<? extends TypeSerializer>> typeSerializers = new ArrayList<Class<? extends TypeSerializer>>();
			final ClassLoader classLoader = mContext.getClass().getClassLoader();
			for (String serializer : serializers) {
				try {
					Class serializerClass = Class.forName(serializer.trim(), false, classLoader);
					if (ReflectionUtils.isTypeSerializer(serializerClass)) {
						typeSerializers.add(serializerClass);
					}
				}
				catch (ClassNotFoundException e) {
					Log.e("Couldn't create class.", e);
				}
			}

			return typeSerializers;
		}

	}
}
