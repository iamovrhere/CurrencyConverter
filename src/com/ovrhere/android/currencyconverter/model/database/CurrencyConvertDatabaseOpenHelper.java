/*
 * Copyright 2015 Jason J. (iamovrhere)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.ovrhere.android.currencyconverter.model.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

/**
 * The database open helper for the currency converter application.
 * Contains convenience functions to get helpers using {@link CurrencyConvertDatabaseSchema}. 
 * 
 * @author Jason J.
 * @version 0.2.0-20140901
 * @see CurrencyConvertDatabaseSchema
 */
public class CurrencyConvertDatabaseOpenHelper extends DatabaseOpenHelper {
	/** The schema for the sms filter database helper. */
	static private DatabaseSchema currConvDatabaseSchema = 
			new CurrencyConvertDatabaseSchema();
	
	/** Convenience function for {@link #getWriteHelper(Context, DatabaseSchema)}
     * using the {@link CurrencyConvertDatabaseSchema}.  
     * @return A writable {@link DatabaseOpenHelper}.      */
	public static DatabaseOpenHelper getWriteHelper(Context context){
		return getWriteHelper(context, currConvDatabaseSchema);
    }
    
    /** Convenience function for {@link #getReadHelper(Context, DatabaseSchema)}
     * using the {@link CurrencyConvertDatabaseSchema}.  
     * @return A read-only {@link DatabaseOpenHelper}.      */
	public static DatabaseOpenHelper getReadHelper(Context context){
    	return getReadHelper(context, currConvDatabaseSchema);
    }
	/** Calls DatabaseOpenHelper constructor. */
	protected CurrencyConvertDatabaseOpenHelper(Context context, DatabaseSchema databaseSchema) {
		super(context, databaseSchema);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		//not needed atm
	}
}