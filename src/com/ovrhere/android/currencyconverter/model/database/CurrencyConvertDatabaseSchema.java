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

import java.util.Locale;

/**
 * The currency data schema for currency converter application.
 * @author Jason J.
 * @version 0.3.0-20140907
 */
public class CurrencyConvertDatabaseSchema extends DatabaseSchema {
	/** The database name. This should be unique to the application. */
	final static public String DATABASE_NAME = "Ovrhere_Currency_Converter";
	/** The PUBLIC database version and schema version. 
	 * Increment this when a public release needs to change the database. */
	final static public int DATABASE_VERSION = 1;
	
	/** Table for exchange rates compared to USD. */
	@Deprecated
	static public class EXCHANGE_RATES_USD {
		/** The table name. */
		final static public String TABLE_NAME = 
				EXCHANGE_RATES_USD.class.getSimpleName().toLowerCase(Locale.US);
		/**  The row id. Integer. */
		final static public String COL_ID = "_id";
		/**  The currency code, 3 letters. Primary key. String. */
		final static public String COL_CURRENCY_CODE = "currency_code";
		/**  The currency symbol, $, ¥, £, etc. String. */
		final static public String COL_CURRENCY_SYMBOL = "currency_symbol";
		/**  The longer, more readable name of the currency. String. */
		final static public String COL_CURRENCY_NAME = "currency_name";
		/**  The rate from USD to the currency in question. USD should == 1.0. 
		 * Float/String. */
		final static public String COL_USD_RATE = "usd_rate";
		/**  The last time the rate was modified; this is used in caching.
		  * The date+time is given in GMT or UTC. String. */
		final static public String COL_LAST_MODIFIED = "last_modified";
	}
	
	/** Table for list of supported currencies.
	 * Now normalized with {@link CURRENCY_EXCHANGE_RATES}. */
	static public class CURRENCY_LIST {
		/** The table name. */
		final static public String TABLE_NAME = 
				CURRENCY_LIST.class.getSimpleName().toLowerCase(Locale.US);
		/**  The row id. Integer. Primary key. */
		final static public String COL_ID = "_id";
		/**  The currency code, 3 letters. Primary key. String. */
		final static public String COL_CURRENCY_CODE = "currency_code";
		/**  The currency symbol, $, ¥, £, etc. String. */
		final static public String COL_CURRENCY_SYMBOL = "currency_symbol";
		/**  The longer, more readable name of the currency. String. */
		final static public String COL_CURRENCY_NAME = "currency_name";
		/**  The last time the rate was modified; this is used in caching.
		  * The date+time is given in GMT or UTC. String. */
		final static public String COL_LAST_MODIFIED = "last_modified";
	}
	
	/** Table for list of currency exchanges: SOURCE -> DEST. 
	 *  Normalized with {@link CURRENCY_LIST}. */
	static public class CURRENCY_EXCHANGE_RATES {
		/** The table name. */
		final static public String TABLE_NAME = 
				CURRENCY_EXCHANGE_RATES.class.getSimpleName().toLowerCase(Locale.US);
		/**  The row id. Integer. Primary key. */
		final static public String COL_ID = "_id";
		/**  The source currency code, 3 letters. Primary key. String. */
		final static public String COL_SOURCE_CURRENCY_CODE = "source_currency_code";
		/**  The destination currency code, 3 letters. Primary key. String. */
		final static public String COL_DEST_CURRENCY_CODE = "dest_currency_code";
		/**  The rate from SOURCE to the DEST currency in question. Float/String. */
		final static public String COL_EXCHANGE_RATE = "exchange_rate";
	}
	
	/** The array of sql-lite create commands. */
	public static String[] DATABASE_TABLE_CREATES = new String[]{
		"CREATE TABLE " + CURRENCY_LIST.TABLE_NAME + " ("+
			CURRENCY_LIST.COL_ID + " INTEGER PRIMARY KEY, " +
			CURRENCY_LIST.COL_CURRENCY_CODE + " TEXT UNIQUE NOT NULL, "  +
			CURRENCY_LIST.COL_CURRENCY_SYMBOL + " TEXT NOT NULL, " +
			CURRENCY_LIST.COL_CURRENCY_NAME + " TEXT NOT NULL, " +
			CURRENCY_LIST.COL_LAST_MODIFIED + " TEXT NOT NULL "+			
		" );",
		
		"CREATE TABLE " + CURRENCY_EXCHANGE_RATES.TABLE_NAME + " ("+
			CURRENCY_EXCHANGE_RATES.COL_ID + " INTEGER PRIMARY KEY, " +
			CURRENCY_EXCHANGE_RATES.COL_SOURCE_CURRENCY_CODE + " TEXT NOT NULL, "  +
			CURRENCY_EXCHANGE_RATES.COL_DEST_CURRENCY_CODE + " TEXT NOT NULL, " +
			CURRENCY_EXCHANGE_RATES.COL_EXCHANGE_RATE + " TEXT NOT NULL, " +
			" UNIQUE (" + 
			CURRENCY_EXCHANGE_RATES.COL_SOURCE_CURRENCY_CODE +", " +
			CURRENCY_EXCHANGE_RATES.COL_DEST_CURRENCY_CODE +" ) " +
		" );"
		
	};
	@Override
	public String getName() {
		return DATABASE_NAME;
	}

	@Override
	public String[] getTableCreates() {
		return DATABASE_TABLE_CREATES;
	}
	
	@Override
	public int getVersion() {
		return DATABASE_VERSION;
	}	
}
