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
package com.ovrhere.android.currencyconverter.oldmodel;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.ovrhere.android.currencyconverter.oldmodel.dao.CurrencyData;
import com.ovrhere.android.currencyconverter.oldmodel.database.CurrencyConvertDatabaseOpenHelper;
import com.ovrhere.android.currencyconverter.oldmodel.database.CurrencyConvertDatabaseSchema;
import com.ovrhere.android.currencyconverter.oldmodel.database.DatabaseOpenHelper;
import com.ovrhere.android.currencyconverter.oldmodel.localmodel.ReadWriteModel;
import com.ovrhere.android.currencyconverter.utils.Timestamp;

/**
 * The local model for the database for cached Exchange rates. 
 * @author Jason J.
 * @version 0.1.4-20140929
 */
@Deprecated
class CurrencyExchangeRateModel 
	implements ReadWriteModel<CurrencyData, List<CurrencyData>> {
	/** The log tag. */
	final static private String LOGTAG = 
			CurrencyExchangeRateModel.class.getSimpleName();
	/** Formatted string for exception message.  Accepts one string. */
	final static private String DETAILED_EXCEPTION_INSERT = 
			"An error occured inserting record: '%s' ";
	/** Formatted string for exception message.  Accepts one string. */
	final static private String DETAILED_EXCEPTION_UPDATE = 
			"An error occured updating record: '%s' ";
	/** Formatted string for exception message. Accepts one string. */
	final static private String DETAILED_EXCEPTION_DATE_PARSE_ERROR = 
			"Date did not parse correctly for: '%s'";
	
	/** All columns to return in query related to the currency list. */
	final static private String[] COLUMNS_ALL_CURRENCY_LIST_DATA = 
			new String[]{
		CurrencyConvertDatabaseSchema.CURRENCY_LIST.COL_ID,
		CurrencyConvertDatabaseSchema.CURRENCY_LIST.COL_CURRENCY_CODE,
		CurrencyConvertDatabaseSchema.CURRENCY_LIST.COL_CURRENCY_NAME,
		CurrencyConvertDatabaseSchema.CURRENCY_LIST.COL_CURRENCY_SYMBOL,
		CurrencyConvertDatabaseSchema.CURRENCY_LIST.COL_LAST_MODIFIED
		
	};
	
	/** All columns to return in query related to the exchange rates. */
	final static private String[] COLUMNS_ALL_EXCHANGE_RATE_DATA = 
			new String[]{
		CurrencyConvertDatabaseSchema.CURRENCY_EXCHANGE_RATES.COL_ID,
		CurrencyConvertDatabaseSchema.CURRENCY_EXCHANGE_RATES.COL_SOURCE_CURRENCY_CODE,
		CurrencyConvertDatabaseSchema.CURRENCY_EXCHANGE_RATES.COL_DEST_CURRENCY_CODE,
		CurrencyConvertDatabaseSchema.CURRENCY_EXCHANGE_RATES.COL_EXCHANGE_RATE
		
	};
	
	/*private final String PREPARED_QUERY_ALL_CURRENCY_DATA = 
			"SELECT * FROM "+ 
			CurrencyConvertDatabaseSchema.CURRENCY_LIST.TABLE_NAME +
			" a INNER JOIN "+
			CurrencyConvertDatabaseSchema.CURRENCY_EXCHANGE_RATES.TABLE_NAME +
			" b ON a."+ CurrencyConvertDatabaseSchema.CURRENCY_LIST.COL_CURRENCY_CODE +
			" = b."+ CurrencyConvertDatabaseSchema.CURRENCY_EXCHANGE_RATES.COL_SOURCE_CURRENCY_CODE +
			" WHERE a.";*/
	
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
				
		SQLiteDatabase wDb = null; 
		try {		
			synchronized (writer) { //in case any other threads close it.
				wDb = writer.getWritableDatabaseOrThrow();
				
				String sourceCode = record.getCurrencyCode();
				HashMap<String, Double> rates = record.getRates();
				ContentValues cv1 = new ContentValues();
				List<ContentValues> cv2 = new ArrayList<ContentValues>();
				
				prepareRecordForCurrencyListDb(record, cv1);
				prepareRecordForExchangeRatesDb(sourceCode, rates, cv2);
				
				
				String recordName = record.toString();
				wDb.beginTransaction();
				try{
					insertRecordIntoTable(
							CurrencyConvertDatabaseSchema.CURRENCY_LIST.TABLE_NAME, 
							recordName, wDb, cv1);
					for (ContentValues cv : cv2) {
						insertRecordIntoTable(
								CurrencyConvertDatabaseSchema
									.CURRENCY_EXCHANGE_RATES.TABLE_NAME, 
								recordName, wDb, cv);
					}
					wDb.setTransactionSuccessful();
				} finally {
					wDb.endTransaction();
				}
			}
		} finally{
			//close isn't actually required, but nice.
			wDb.close();
			writer.softClose();
		}
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
			
		SQLiteDatabase wDb = null;
		try {
			wDb = writer.getWritableDatabaseOrThrow();
			
			String sourceCode = record.getCurrencyCode();
			HashMap<String, Double> rates = record.getRates();
			ContentValues cv1 = new ContentValues();
			List<ContentValues> cv2 = new ArrayList<ContentValues>();
			
			prepareRecordForCurrencyListDb(record, cv1);
			
			List<String> targetCodes = 
					prepareRecordForExchangeRatesDb(sourceCode, rates, cv2);
			
			cv1.put(	CurrencyConvertDatabaseSchema.CURRENCY_LIST.COL_ID, id);
			String recordName = record.toString();
			
			wDb.beginTransaction();
			try{
				//update the currency list records
				updateTableRecord(
						CurrencyConvertDatabaseSchema.CURRENCY_LIST.TABLE_NAME, 
						recordName, 
						CurrencyConvertDatabaseSchema.CURRENCY_LIST.COL_ID+" = ? ",
						new String[]{ String.valueOf(id)}, 
						wDb, cv1);
				
				//update the exchange rates
				for (int index = 0; index < cv2.size(); index++) {
					updateTableRecord(
							CurrencyConvertDatabaseSchema
								.CURRENCY_EXCHANGE_RATES.TABLE_NAME, 
							recordName, 
							CurrencyConvertDatabaseSchema
								.CURRENCY_EXCHANGE_RATES.COL_SOURCE_CURRENCY_CODE+
							" = ? AND "+
							CurrencyConvertDatabaseSchema
								.CURRENCY_EXCHANGE_RATES.COL_DEST_CURRENCY_CODE+
							" = ? ", 
							new String[]{
									record.getCurrencyCode(), 
									targetCodes.get(index)}, 
							wDb, 
							cv2.get(index));
				}
				
				wDb.setTransactionSuccessful();
			} finally {
				wDb.endTransaction();
			}
		} finally {
			//close isn't actually required, but nice.
			wDb.close();
			writer.softClose();
		}
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
		
		SQLiteDatabase rDb = null;
		try {
			rDb = reader.getReadableDatabase();
			
			CurrencyData result = null; //the ultimate result.
			
			Cursor cursor = rDb.query(
						CurrencyConvertDatabaseSchema.CURRENCY_LIST.TABLE_NAME, 
						COLUMNS_ALL_CURRENCY_LIST_DATA, 
						CurrencyConvertDatabaseSchema.CURRENCY_LIST.COL_ID+
						" = ? ",
						new String[]{ String.valueOf(id) }, 
						null, null, null);	
			
			List<CurrencyData> results = new ArrayList<CurrencyData>();
			listCursorToCurrencyDataList(results, cursor);
			if (!results.isEmpty()){
				String code = results.get(0).getCurrencyCode();
				Cursor cursor2 = rDb.query(
						CurrencyConvertDatabaseSchema
							.CURRENCY_EXCHANGE_RATES.TABLE_NAME, 
						COLUMNS_ALL_EXCHANGE_RATE_DATA, 
						CurrencyConvertDatabaseSchema
							.CURRENCY_EXCHANGE_RATES.COL_SOURCE_CURRENCY_CODE+
						" = ? ",
						new String[]{ String.valueOf(code) }, 
						null, null, null);	
				results = rateCursorToCurrencyDataList(results, cursor2);	
				result = results.get(0);
			}				
			return result;
			
		} finally {
			//close isn't actually required, but nice.
			rDb.close();
			reader.softClose();
		}		
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
		
		SQLiteDatabase rDb = null;
		try {
			rDb = reader.getReadableDatabase();
			
			CurrencyData result =  null;
			
			Cursor cursor = rDb.query(
					CurrencyConvertDatabaseSchema.CURRENCY_LIST.TABLE_NAME, 
					COLUMNS_ALL_CURRENCY_LIST_DATA, 
					CurrencyConvertDatabaseSchema.CURRENCY_LIST.COL_CURRENCY_CODE+
					" = ? ",
					new String[]{ key }, 
					null, null, null);	
	
			Cursor cursor2 = rDb.query(
					CurrencyConvertDatabaseSchema
						.CURRENCY_EXCHANGE_RATES.TABLE_NAME, 
					COLUMNS_ALL_EXCHANGE_RATE_DATA, 
					CurrencyConvertDatabaseSchema
						.CURRENCY_EXCHANGE_RATES.COL_SOURCE_CURRENCY_CODE+
					" = ? ",
					new String[]{ key }, 
					null, null, null);	
		
			List<CurrencyData> results = new ArrayList<CurrencyData>();
			listCursorToCurrencyDataList(results, cursor);
			if (!results.isEmpty()){
				results = rateCursorToCurrencyDataList(results, cursor2);	
				result = results.get(0);
			}
			
			
			return result;
		} finally {
			//close isn't actually required, but nice.
			rDb.close();
			reader.softClose();	
		}
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
		SQLiteDatabase rDb = null;
		try {
			rDb = reader.getReadableDatabase();
			
			Cursor cursor = rDb.query(
						CurrencyConvertDatabaseSchema.CURRENCY_LIST.TABLE_NAME, 
						COLUMNS_ALL_CURRENCY_LIST_DATA, 
						null, null, null, null, null);	
			Cursor cursor2 = rDb.query(
					CurrencyConvertDatabaseSchema.CURRENCY_EXCHANGE_RATES.TABLE_NAME, 
					COLUMNS_ALL_EXCHANGE_RATE_DATA, 
					null, null, null, null, null);	
		
			listCursorToCurrencyDataList(list, cursor);
			list = rateCursorToCurrencyDataList(list, cursor2);
		
			return list;
		} finally {
			//close isn't actually required, but nice.
			rDb.close();
			reader.softClose();
		}		
	}

	
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Utility functions
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** Inserts record into given table, throwing error if an error occurs. 
	 * @param tableName The table name.
	 * @param recordName The record name (for exceptions)
	 * @param writableDb The database handle to write with.
	 * @param cv The contents to input.
	 * @throws SQLException An exception if no results change.
	 */
	static private void insertRecordIntoTable(String tableName, String recordName,
			SQLiteDatabase writableDb, ContentValues cv) throws SQLException {
		long result = writableDb.insert(tableName, null, cv);		
		if (result < 0){
			throw new SQLException(
					String.format(DETAILED_EXCEPTION_INSERT, recordName)
					);
		}
	}
	
	/** Updates table recorud based upon conditions supplied.
	 * @param tableName The table name
	 * @param recordName The record name (for debugging)
	 * @param preparedCondition The prepared statment for which rows
	 * @param conditionValues The conditional values
	 * @param writeableDb The writable db for access
	 * @param cv The values
	 * @throws SQLException If no records are updated.
	 */
	static private void updateTableRecord(String tableName, String recordName,
			String preparedCondition, String[] conditionValues,
			SQLiteDatabase writeableDb, ContentValues cv) throws SQLException {
		long result =
				writeableDb.update( tableName, cv, preparedCondition, conditionValues);		
		if (result < 0){
			throw new SQLException(
					String.format(DETAILED_EXCEPTION_UPDATE, recordName)
					);
		}
	}
	
	/** Validates the currency data record for insertion. */
	static private void validateRecord(CurrencyData record) {
		if (	record.getCurrencyCode() == null ||
				record.getCurrencyName() == null ||
				record.getCurrencySymbol() == null ||
				record.getRates().isEmpty() ){
			//record is not valid for insertion.
			throw new IllegalArgumentException(
					"Record must contain: " +
					"currency code, currency name, currency symbol and an " +
					"exchange rate >= 0. " +
					"Values are: currencyCode(" + record.getCurrencyCode() +
					"), currencyName(" + record.getCurrencyName() +
					"), currencySymbol(" + record.getCurrencySymbol()+") "
					);
		}
	}
	/** Prepares the record into contents values for insertion/updates.
	 * Does not set content value for id. 
	 * @param record The record to insert.
	 * @param cv The returned content values.
	 */
	static private void prepareRecordForCurrencyListDb(CurrencyData record, 
			ContentValues cv){
		cv.put(	CurrencyConvertDatabaseSchema.CURRENCY_LIST.COL_CURRENCY_CODE,
				record.getCurrencyCode());
		cv.put(	CurrencyConvertDatabaseSchema.CURRENCY_LIST.COL_CURRENCY_NAME,
				record.getCurrencyName());
		cv.put(	CurrencyConvertDatabaseSchema.CURRENCY_LIST.COL_CURRENCY_SYMBOL,
				record.getCurrencySymbol());
		String timestamp = record.getModifiedTimestamp();
		if (timestamp == null || timestamp.isEmpty()){
			timestamp = Timestamp.getUtc();
		}
		cv.put(	CurrencyConvertDatabaseSchema.CURRENCY_LIST.COL_LAST_MODIFIED,
				timestamp);	
	}
	
	/** Prepares the record into contents values for insertion/updates.
	 * Does not set content value for id. 
	 * @param record The record to insert.
	 * @param cv The returned content values.
	 * @return The list of currency code keys in the cv (of same length)
	 */
	static private List<String> prepareRecordForExchangeRatesDb(String sourceCode,
			HashMap<String, Double> rates,
			List<ContentValues> cvList){
		List<String> targetCodes = new ArrayList<String>();
		for (Entry<String, Double> rate : rates.entrySet()) {
			ContentValues cv = new ContentValues();
			cv.put(	
				CurrencyConvertDatabaseSchema.CURRENCY_EXCHANGE_RATES.COL_SOURCE_CURRENCY_CODE,
				sourceCode);
			cv.put(	
				CurrencyConvertDatabaseSchema.CURRENCY_EXCHANGE_RATES.COL_DEST_CURRENCY_CODE,
				rate.getKey());
			cv.put(	
				CurrencyConvertDatabaseSchema.CURRENCY_EXCHANGE_RATES.COL_EXCHANGE_RATE,
				rate.getValue());
			targetCodes.add(rate.getKey());
			cvList.add(cv);
		}
		return targetCodes;
	}
	
	/**
	 * Converts cursor into value.
	 * @param list The currency data parsed. Sometimes just 1 element.
	 * @param cursor
	 */
	static private void listCursorToCurrencyDataList
			(List<CurrencyData> list, Cursor cursor){
		//list of all column numbers in the query.
		//See: COLUMNS_ALL_CURRENCY_LIST_DATA for order.
		final int id = cursor.getColumnIndexOrThrow(
					CurrencyConvertDatabaseSchema.CURRENCY_LIST.COL_ID
				);
		final int code = cursor.getColumnIndexOrThrow(
				CurrencyConvertDatabaseSchema.CURRENCY_LIST.COL_CURRENCY_CODE
			);
		final int name = cursor.getColumnIndexOrThrow(
				CurrencyConvertDatabaseSchema.CURRENCY_LIST.COL_CURRENCY_NAME
			);
		final int symbol = cursor.getColumnIndexOrThrow(
				CurrencyConvertDatabaseSchema.CURRENCY_LIST.COL_CURRENCY_SYMBOL
			);
		final int timestamp = cursor.getColumnIndexOrThrow(
				CurrencyConvertDatabaseSchema.CURRENCY_LIST.COL_LAST_MODIFIED
			);
		
		
		while (cursor.moveToNext()){
			CurrencyData.Builder builder = new CurrencyData.Builder();
			try {
				builder
					.setId(cursor.getInt(id))
					.setCurrency(	cursor.getString(symbol),
									cursor.getString(code),
									cursor.getString(name))
					.setModifiedTimestamp(
							cursor.getString(timestamp)
						);
			} catch (NumberFormatException e) {
				Log.e(LOGTAG, "Rate did not parse correctly for: "+
						cursor.getString(code));
			} catch (ParseException e) {
				Log.e(LOGTAG, 
						String.format(DETAILED_EXCEPTION_DATE_PARSE_ERROR, 
								cursor.getString(code))
								);
			}
			list.add(builder.create());
		}
	}
	
	/**
	 * Steps through the cursor to populate the passed currency data list.
	 * Assumes the query has been done using {@link #COLUMNS_ALL_EXCHANGE_RATE_DATA}.
	 * @param list The list to update.
	 * @param cursor The cursor to step through.
	 * @return The updated results with rates. 
	 */
	static private List<CurrencyData> rateCursorToCurrencyDataList
				(List<CurrencyData> list, Cursor cursor) {
		//list of all column numbers in the query.
		//See: COLUMNS_ALL_EXCHANGE_RATE_DATA
		//for order.
		
		final int rate = cursor.getColumnIndexOrThrow(
				CurrencyConvertDatabaseSchema.CURRENCY_EXCHANGE_RATES.COL_EXCHANGE_RATE
			);
		final int sCode = cursor.getColumnIndexOrThrow(
				CurrencyConvertDatabaseSchema.CURRENCY_EXCHANGE_RATES.COL_SOURCE_CURRENCY_CODE
			);
		final int dCode = cursor.getColumnIndexOrThrow(
				CurrencyConvertDatabaseSchema.CURRENCY_EXCHANGE_RATES.COL_DEST_CURRENCY_CODE
			);
		
		//this is incredibly inefficient
		HashMap<String, HashMap<String,Double>> rateMap = 
				new HashMap<String, HashMap<String,Double>>();
		while (cursor.moveToNext()){
			String source = cursor.getString(sCode);
			if (!rateMap.containsKey(source)){ //if there is no group for this source
				rateMap.put(source, new HashMap<String, Double>());
			}
			try { //put rate for source -> dCode
			rateMap.get(source).put(
						cursor.getString(dCode), 
						Double.parseDouble(cursor.getString(rate))
					);
			} catch (NumberFormatException e) {
				Log.e(LOGTAG, "Rate did not parse correctly for: "+
						source+cursor.getString(dCode));
			}
		}
		List<CurrencyData> results = new ArrayList<CurrencyData>();
		for (CurrencyData currencyData : list) {
			HashMap<String, Double> rates = 
					rateMap.get(currencyData.getCurrencyCode());
			if (rates != null){
				currencyData.updateRates(rates);
			}
			results.add(currencyData);
		}
		return results;
	}
}
