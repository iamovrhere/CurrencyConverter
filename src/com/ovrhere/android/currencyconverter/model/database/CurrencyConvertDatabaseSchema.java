/*
 * Copyright 2014 Jason J.
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
 * @version 0.1.0-20140609
 */
public class CurrencyConvertDatabaseSchema extends DatabaseSchema {
	/** The database name. This should be unique to the application. */
	final static public String DATABASE_NAME = "Ovrhere_Currency_Converter";
	/** The PUBLIC database version and schema version. 
	 * Increment this when a public release needs to change the database. */
	final static public int DATABASE_VERSION = 1;
	
	/** Table for exchange rates compared to USD. */
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
	
	/** The array of sql-lite create commands. */
	public static String[] DATABASE_TABLE_CREATES = new String[]{
		"CREATE TABLE " + EXCHANGE_RATES_USD.TABLE_NAME + " ("+
				EXCHANGE_RATES_USD.COL_ID + " INT, " +
				EXCHANGE_RATES_USD.COL_CURRENCY_CODE + " TEXT PRIMARY KEY, "  +
				EXCHANGE_RATES_USD.COL_CURRENCY_SYMBOL + " TEXT, " +
				EXCHANGE_RATES_USD.COL_CURRENCY_NAME + " TEXT, " +
				EXCHANGE_RATES_USD.COL_USD_RATE + " TEXT, " +
				EXCHANGE_RATES_USD.COL_LAST_MODIFIED + " TEXT "+
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
