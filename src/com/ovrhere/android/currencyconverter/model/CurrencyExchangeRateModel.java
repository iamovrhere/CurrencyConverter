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
package com.ovrhere.android.currencyconverter.model;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import com.ovrhere.android.currencyconverter.dao.CurrencyData;
import com.ovrhere.android.currencyconverter.model.database.CurrencyConvertDatabaseOpenHelper;
import com.ovrhere.android.currencyconverter.model.database.CurrencyConvertDatabaseSchema;
import com.ovrhere.android.currencyconverter.model.database.DatabaseOpenHelper;
import com.ovrhere.android.currencyconverter.model.localmodel.ReadWriteModel;
import com.ovrhere.android.currencyconverter.utils.Timestamp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * The local model for the database for cached Exchange rates. 
 * @author Jason J.
 * @version 0.1.2-20140906
 */
class CurrencyExchangeRateModel 
	implements ReadWriteModel<CurrencyData, List<CurrencyData>> {
	/** The log tag. */
	final static private String LOGTAG = 
			CurrencyExchangeRateModel.class.getSimpleName();
	
	/** All columns to return in the query.  */
	final static private String[] COLUMNS_ALL_CURRENCY_DATA =new String[] {
			CurrencyConvertDatabaseSchema.EXCHANGE_RATES_USD.COL_ID,
			CurrencyConvertDatabaseSchema.EXCHANGE_RATES_USD.COL_CURRENCY_CODE,
			CurrencyConvertDatabaseSchema.EXCHANGE_RATES_USD.COL_CURRENCY_NAME,
			CurrencyConvertDatabaseSchema.EXCHANGE_RATES_USD.COL_CURRENCY_SYMBOL,
			CurrencyConvertDatabaseSchema.EXCHANGE_RATES_USD.COL_USD_RATE,
			CurrencyConvertDatabaseSchema.EXCHANGE_RATES_USD.COL_LAST_MODIFIED
		};
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// End constants
	////////////////////////////////////////////////////////////////////////////////////////////////
		
	/** The current context being used to access the database. */
	private Context mContext = null;
	
	/** The model use to access the database.
	 * @param context The current context.	 */
	public CurrencyExchangeRateModel(Context context) {
		this.mContext = context;
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Write to database
	////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Inserts a new currency record into the database.
	 * @param record The record must contain:
	 * <ul><li>A currency code</li><li>A currency name</li>
	 * <li>A currency symbol</li><li>An exchange rate</li>
	 * </ul>
	 * If a date is included, it will be used, otherwise the current time is used.
	 * @throws SQLException 
	 */
	@Override
	public void insertRecord(CurrencyData record) throws SQLException {
		validateRecord(record);		
		DatabaseOpenHelper writer = 
				CurrencyConvertDatabaseOpenHelper.getWriteHelper(mContext); 
		SQLiteDatabase wDb = writer.getWritableDatabaseOrThrow();
		ContentValues cv = new ContentValues();
		prepareRecordForDb(record, cv);
		long result = wDb.insert(
						CurrencyConvertDatabaseSchema.EXCHANGE_RATES_USD.TABLE_NAME, 
						null, 
						cv
					);
		if (result < 0){
			throw new SQLException("An error occured inserting record: '"
					+record.toString()+"'");
		}
		wDb.close();
		writer.softClose();
	}
	
	/**
	 * Updates an existing currency record in the database.
	 * @param record The record must contain:
	 * <ul><li>A currency code</li><li>A currency name</li>
	 * <li>A currency symbol</li><li>An exchange rate</li>
	 * </ul>
	 * If a date is included, it will be used, otherwise the current time is used.
	 * @throws SQLException If the update fails to effect any rows 
	 */
	@Override
	public void modifyRecord(int id, CurrencyData record) throws SQLException {
		validateRecord(record);		
		DatabaseOpenHelper writer = 
				CurrencyConvertDatabaseOpenHelper.getWriteHelper(mContext); 
		SQLiteDatabase wDb = writer.getWritableDatabaseOrThrow();
		ContentValues cv = new ContentValues();
		prepareRecordForDb(record, cv);
		cv.put(	CurrencyConvertDatabaseSchema.EXCHANGE_RATES_USD.COL_ID, id);
		long result =
				wDb.update(
						CurrencyConvertDatabaseSchema.EXCHANGE_RATES_USD.TABLE_NAME, 
						cv, 
						CurrencyConvertDatabaseSchema.EXCHANGE_RATES_USD.COL_ID+
						" = ? ",
						new String[]{ String.valueOf(id) }
						);
		
		if (result < 0){
			throw new SQLException("An error occured updating record: '"
					+record.toString()+"'");
		}
		wDb.close();
		writer.softClose();
	}
	
	/** Unimplemented. */
	@Override
	public boolean deleteRecord(int id) {
		// TODO Auto-generated method stub
		return false;
	}
	/** Unimplemented. */
	@Override
	public boolean deleteRecord(CurrencyData record) {
		// TODO Auto-generated method stub
		return false;
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Read from database
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** Gets the single record based upon the requested id.
	 * @param id The record id.
	 * @return The requested currency data record or <code>null</code>.
	 */
	@Override
	public CurrencyData getRecord(int id) {
		CurrencyConvertDatabaseOpenHelper.getReadHelper(mContext);
		DatabaseOpenHelper reader = 
				CurrencyConvertDatabaseOpenHelper.getReadHelper(mContext);
		SQLiteDatabase rDb = reader.getReadableDatabase();
		
		Cursor cursor = rDb.query(
					CurrencyConvertDatabaseSchema.EXCHANGE_RATES_USD.TABLE_NAME, 
					COLUMNS_ALL_CURRENCY_DATA, 
					CurrencyConvertDatabaseSchema.EXCHANGE_RATES_USD.COL_ID+
					" = ? ",
					new String[]{ String.valueOf(id) }, 
					null, null, null);		
		
		CurrencyData result = cursorToCurrencyData(cursor);
		
		rDb.close();
		reader.softClose();	
		return result;
	}
	
	/** Gets the single record based upon the requested key.
	 * @param key The currency code of the record to return
	 * @return The requested currency data record or <code>null</code>.
	 */
	@Override
	public CurrencyData getRecord(String key) {
		CurrencyConvertDatabaseOpenHelper.getReadHelper(mContext);
		DatabaseOpenHelper reader = 
				CurrencyConvertDatabaseOpenHelper.getReadHelper(mContext);
		SQLiteDatabase rDb = reader.getReadableDatabase();
		
		Cursor cursor = rDb.query(
					CurrencyConvertDatabaseSchema.EXCHANGE_RATES_USD.TABLE_NAME, 
					COLUMNS_ALL_CURRENCY_DATA, 
					CurrencyConvertDatabaseSchema.EXCHANGE_RATES_USD.COL_CURRENCY_CODE+
					" = ? ",
					new String[]{ key }, 
					null, null, null);		
		
		CurrencyData result = cursorToCurrencyData(cursor);
		
		rDb.close();
		reader.softClose();	
		return result;
	}
	
	/**
	 * Returns all currency data records.
	 * @return A list of all the currency record datas or an empty list.
	 */
	@Override
	public List<CurrencyData> getAllRecords() {
		List<CurrencyData> list = new ArrayList<CurrencyData>();
		DatabaseOpenHelper reader = 
				CurrencyConvertDatabaseOpenHelper.getReadHelper(mContext);
		SQLiteDatabase rDb = reader.getReadableDatabase();
		
		Cursor cursor = rDb.query(
					CurrencyConvertDatabaseSchema.EXCHANGE_RATES_USD.TABLE_NAME, 
					COLUMNS_ALL_CURRENCY_DATA, 
					null, null, null, null, null);		
		 
		cursorToCurrencyDataList(list, cursor);
		
		rDb.close();
		reader.softClose();		
		return list;
	}

	
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Utility functions
	////////////////////////////////////////////////////////////////////////////////////////////////
	/** Validates the currency data record for insertion. */
	static private void validateRecord(CurrencyData record) {
		if (	record.getCurrencyCode() == null ||
				record.getCurrencyName() == null ||
				record.getCurrencySymbol() == null ||
				record.getRateFromUSD() < 0 ){
			//record is not valid for insertion.
			throw new IllegalArgumentException(
					"Record must contain: " +
					"currency code, currency name, currency symbol and an " +
					"exchange rate >= 0. " +
					"Values are: currencyCode(" + record.getCurrencyCode() +
					"), currencyName(" + record.getCurrencyCode() +
					"), currencySymbol(" + record.getCurrencyCode() +
					"), rate(" + record.getCurrencyCode() + "). "
					);
		}
	}
	/** Prepares the record into contents values for insertion/updates.
	 * Does not set content value for id. 
	 * @param record The record to insert.
	 * @param cv The returned content values.
	 */
	static private void prepareRecordForDb(CurrencyData record, ContentValues cv){
		cv.put(	CurrencyConvertDatabaseSchema.EXCHANGE_RATES_USD.COL_CURRENCY_CODE,
				record.getCurrencyCode());
		cv.put(	CurrencyConvertDatabaseSchema.EXCHANGE_RATES_USD.COL_CURRENCY_NAME,
				record.getCurrencyName());
		cv.put(	CurrencyConvertDatabaseSchema.EXCHANGE_RATES_USD.COL_CURRENCY_SYMBOL,
				record.getCurrencySymbol());
		cv.put(	CurrencyConvertDatabaseSchema.EXCHANGE_RATES_USD.COL_USD_RATE,
				record.getRateFromUSD());
		String timestamp = record.getModifiedTimestamp();
		if (timestamp == null || timestamp.isEmpty()){
			timestamp = Timestamp.getUtc();
		}
		cv.put(	CurrencyConvertDatabaseSchema.EXCHANGE_RATES_USD.COL_LAST_MODIFIED,
				timestamp);	
	}
	
	/**
	 * Converts cursor into value.
	 * @param cursor
	 * @return The currency data from cursor query or <code>null</code>.
	 */
	static private CurrencyData cursorToCurrencyData(Cursor cursor){
		//list of all column numbers in the query.
		//See: COLUMNS_ALL_CURRENCY_DATA for order.
		final int id = cursor.getColumnIndexOrThrow(
					CurrencyConvertDatabaseSchema.EXCHANGE_RATES_USD.COL_ID
				);
		final int code = cursor.getColumnIndexOrThrow(
				CurrencyConvertDatabaseSchema.EXCHANGE_RATES_USD.COL_CURRENCY_CODE
			);
		final int name = cursor.getColumnIndexOrThrow(
				CurrencyConvertDatabaseSchema.EXCHANGE_RATES_USD.COL_CURRENCY_NAME
			);
		final int symbol = cursor.getColumnIndexOrThrow(
				CurrencyConvertDatabaseSchema.EXCHANGE_RATES_USD.COL_CURRENCY_SYMBOL
			);
		final int rate = cursor.getColumnIndexOrThrow(
				CurrencyConvertDatabaseSchema.EXCHANGE_RATES_USD.COL_USD_RATE
			);
		final int timestamp = cursor.getColumnIndexOrThrow(
				CurrencyConvertDatabaseSchema.EXCHANGE_RATES_USD.COL_LAST_MODIFIED
			);
		
		while (cursor.moveToFirst()){
			CurrencyData.Builder builder = new CurrencyData.Builder();
			try {
				builder
					.setId(cursor.getInt(id))
					.setCurrency(	cursor.getString(symbol),
									cursor.getString(code),
									cursor.getString(name),
									Float.parseFloat(cursor.getString(rate))
									)
					.setModifiedTimestamp(
							cursor.getString(timestamp)
						);
			} catch (NumberFormatException e) {
				Log.e(LOGTAG, "Rate did not parse correctly for: "+
						cursor.getString(code));
			} catch (ParseException e) {
				Log.e(LOGTAG, "Date did not parse correctly for: "+
						cursor.getString(code));
			}
			return builder.create();
		}
		return null;
	}
	
	/**
	 * Steps throw the cursor to populate the passed currency data list.
	 * Assumes the query has been done using {@link #COLUMNS_ALL_CURRENCY_DATA}.
	 * @param list The list to populate.
	 * @param cursor The cursor to step through. 
	 */
	static private void cursorToCurrencyDataList(List<CurrencyData> list, Cursor cursor) {
		//list of all column numbers in the query.
		//See: COLUMNS_ALL_CURRENCY_DATA for order.
		final int id = cursor.getColumnIndexOrThrow(
					CurrencyConvertDatabaseSchema.EXCHANGE_RATES_USD.COL_ID
				);
		final int code = cursor.getColumnIndexOrThrow(
				CurrencyConvertDatabaseSchema.EXCHANGE_RATES_USD.COL_CURRENCY_CODE
			);
		final int name = cursor.getColumnIndexOrThrow(
				CurrencyConvertDatabaseSchema.EXCHANGE_RATES_USD.COL_CURRENCY_NAME
			);
		final int symbol = cursor.getColumnIndexOrThrow(
				CurrencyConvertDatabaseSchema.EXCHANGE_RATES_USD.COL_CURRENCY_SYMBOL
			);
		final int rate = cursor.getColumnIndexOrThrow(
				CurrencyConvertDatabaseSchema.EXCHANGE_RATES_USD.COL_USD_RATE
			);
		final int timestamp = cursor.getColumnIndexOrThrow(
				CurrencyConvertDatabaseSchema.EXCHANGE_RATES_USD.COL_LAST_MODIFIED
			);
		
		while (cursor.moveToNext()){
			CurrencyData.Builder builder = new CurrencyData.Builder();
			try {
				builder
					.setId(cursor.getInt(id))
					.setCurrency(	cursor.getString(symbol),
									cursor.getString(code),
									cursor.getString(name),
									Float.parseFloat(cursor.getString(rate))
									)
					.setModifiedTimestamp(
							cursor.getString(timestamp)
						);
			} catch (NumberFormatException e) {
				Log.e(LOGTAG, "Rate did not parse correctly for: "+
						cursor.getString(code));
			} catch (ParseException e) {
				Log.e(LOGTAG, "Date did not parse correctly for: "+
						cursor.getString(code));
			}
			list.add(builder.create());
		}
	}
}
