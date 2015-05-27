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
 * distributed under the Liscense is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.ovrhere.android.currencyconverter.model.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.ovrhere.android.currencyconverter.model.data.CurrencyConverterContract.DisplayOrderEntry;
import com.ovrhere.android.currencyconverter.model.data.CurrencyConverterContract.ExchangeRateEntry;


/**
 * Creates the database for holding exchange rates between currencies.
 * @author Jason J.
 * @version 1.0.0-20150527
 */
public class CurrencyConverterDbHelper extends SQLiteOpenHelper {
	
	/** The old database to drop. See {@link CurrencyConvertDatabaseOpenHelper}. */
	private static class OLD_DATABASE {
		/** The database name. This should be unique to the application. */
		final static public String DATABASE_NAME = "Ovrhere_Currency_Converter";
		
		/* The PUBLIC database version and schema version. 
		 * Increment this when a public release needs to change the database. */
		//final static public int DATABASE_VERSION = 1;
		/* The table name. */
		//final static public String CURRENCY_LIST_TABLE_NAME = 
		//		"currency_list";
		/* The table name. */
		//final static public String CURRENCY_EXCHANGE_RATES_TABLE_NAME = 
		//		"currency_exchange_rates";
	}
	

	//for version 1 see: CurrencyConvertDatabaseOpenHelper
    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 2; 

    protected static final String DATABASE_NAME = "currencyconverter.db";
    
    
    private Context mContext = null;
    
    public CurrencyConverterDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);          
        this.mContext = context;
    }
	
	@Override
	public void onCreate(SQLiteDatabase sqLiteDb) {
		createExchangeRatesDB(sqLiteDb);
		createCurrencyOrderDB(sqLiteDb);
		mContext.deleteDatabase(OLD_DATABASE.DATABASE_NAME);		
	}

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        // Database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over. 
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + ExchangeRateEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + DisplayOrderEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //// helper methods
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /** Creates the {@link DisplayOrderEntry} database table. */
    private void createCurrencyOrderDB(SQLiteDatabase sqLiteDatabase) {
        final String SQL_CREATE_ORDER_TABLE = "CREATE TABLE " + DisplayOrderEntry.TABLE_NAME + " (" +
        		DisplayOrderEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +

				DisplayOrderEntry.COLUMN_CURRENCY_CODE + " TEXT NOT NULL, " +
				DisplayOrderEntry.COLUMN_DEF_DISPLAY_ORDER + " INTEGER NOT NULL, " +
                
				" UNIQUE (" +
					DisplayOrderEntry.COLUMN_CURRENCY_CODE +  
				") ON CONFLICT REPLACE );"; 

        sqLiteDatabase.execSQL(SQL_CREATE_ORDER_TABLE);
    }
    
    /** Creates the {@link ExchangeRateEntry} database table. */
    private void createExchangeRatesDB(SQLiteDatabase sqLiteDatabase) {
        final String SQL_CREATE_EXCHANGE_TABLE = "CREATE TABLE " + ExchangeRateEntry.TABLE_NAME + " (" +
				ExchangeRateEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +

                ExchangeRateEntry.COLUMN_SOURCE_CURRENCY_CODE + " TEXT NOT NULL, " +
                ExchangeRateEntry.COLUMN_DEST_CURRENCY_CODE + " TEXT NOT NULL, " +
				ExchangeRateEntry.COLUMN_EXCHANGE_RATE + " REAL NOT NULL, " +
                
				//we enforce that each destination has an order. 
				" FOREIGN KEY (" + ExchangeRateEntry.COLUMN_DEST_CURRENCY_CODE + ") REFERENCES " +
				DisplayOrderEntry.TABLE_NAME + " (" + DisplayOrderEntry.COLUMN_CURRENCY_CODE + "), " +
				
				" UNIQUE (" +
					ExchangeRateEntry.COLUMN_SOURCE_CURRENCY_CODE + ", " +
					ExchangeRateEntry.COLUMN_DEST_CURRENCY_CODE + 
				") ON CONFLICT REPLACE);"; //we don't need old records

        sqLiteDatabase.execSQL(SQL_CREATE_EXCHANGE_TABLE);
    }

}
