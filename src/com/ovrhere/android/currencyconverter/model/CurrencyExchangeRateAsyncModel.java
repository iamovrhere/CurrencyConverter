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
import java.util.Iterator;
import java.util.List;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

import com.ovrhere.android.currencyconverter.R;
import com.ovrhere.android.currencyconverter.dao.CurrencyData;
import com.ovrhere.android.currencyconverter.model.asyncmodel.AsyncModel;

/**
 * 
 * Remember to call {@link #dispose()} to start cleanup and release 
 * the {@link Context}.
 * 
 * @author Jason J.
 * @version 0.1.1-20140901
 */
public class CurrencyExchangeRateAsyncModel extends AsyncModel {
	/** The log tag to use. */
	final static private String LOGTAG = 
			CurrencyExchangeRateAsyncModel.class.getSimpleName();
	/** The boolean for debugging. */
	final static private boolean DEBUG = true;

	/* * Retrieves a single record from the database. 
	 * May be accompanied by an id (integer or string). 
	 * Responds via {@link #REPLY_RECORDS_RESULT}. */
	//final static public int REQUEST_GET_SINGLE_RECORD = 0x003;
	/* * Retrieves multiple records from the database. 
	 * May be accompanied by an id (integer list or string list). 
	 * Responds via {@link #REPLY_RECORDS_RESULT}. */
	//final static public int REQUEST_GET_MULTI_RECORDS = 0x004;
	/** Retrieves all records from the database.
	 * If accompanied by a boolean <code>true</code>, 
	 * only cached records are retrieved. 
	 * Responds via {@link #REPLY_RECORDS_RESULT}. */
	final static public int REQUEST_GET_ALL_RECORDS = 0x005;
		
	
	/** Reply of relevant records. 
	 * Accompanied by a {@link List} of {@link CurrencyData}. */
	final static public int REPLY_RECORDS_RESULT = 0x100;
	
	/** Notification for when records are being initialized. */
	final static public int NOTIFY_INITIALIZING_DATABASE = 0x200;
	/** Notification for when records are being updated from web.*/
	final static public int NOTIFY_UPDATING_RATES = 0x201;
	
	
	/**  Error given if request timesout. */
	final static public int ERROR_REQUEST_TIMEOUT = 0x4001;
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// End Request/Reply constants
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// End bundle key constants
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** The local model to access the database. Uses context. */
	private CurrencyExchangeRateModel mLocalModel = null;
	/** Resources handler. Uses context. */
	private Resources res = null;
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// End class members 
	////////////////////////////////////////////////////////////////////////////////////////////////
	/** The context to use when opening the model. 
	 * @param context The application context. */
	public CurrencyExchangeRateAsyncModel(Context context) {
		this.mLocalModel = new CurrencyExchangeRateModel(context);
		this.res = context.getResources();
	}
	
	@Override
	public void dispose() {
		super.dispose();
		this.mLocalModel = null; 
		this.res = null;
	}
	
	@Override
	public int sendMessage(int what, Object object) {
		if (mLocalModel == null){
			return 1;
		}
		if (DEBUG){
			Log.d(LOGTAG, "sendMessage: "+what);
		}
		// TODO Complete implementation.
		switch (what) {
		case REQUEST_GET_ALL_RECORDS:
			requestRecords();
			return 0;

		default:
			//break;
		}
		return -1;
	}	
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Request methods
	////////////////////////////////////////////////////////////////////////////////////////////////
	/** The basic request of records.	 */
	private void requestRecords(){
		List<CurrencyData> records = mLocalModel.getAllRecords();
		if (records.isEmpty()){
			if (initDefaultDatabase()){
				records = mLocalModel.getAllRecords();				
			} else {
				notifyHandlers(ERROR_REQUEST_FAILED, null);
				return;
			}
		}
		setFlagDrawables(records);
		notifyHandlers(REPLY_RECORDS_RESULT, records);
	}

		
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Helper methods
	////////////////////////////////////////////////////////////////////////////////////////////////
	/** Sets the flag drawables for the currency records.	 */
	private void setFlagDrawables(List<CurrencyData> records) {
		for (Iterator<CurrencyData> iterator = records.iterator(); iterator.hasNext();) {
			FlagResourceMap.setCurrencyFlagDrawable(res, iterator.next());	
		}
	}
	
	/** Sets up the local database for the first time. 
	 * @return <code>true</code> if successful. */
	private boolean initDefaultDatabase(){
		try {
			notifyHandlers(NOTIFY_INITIALIZING_DATABASE, null);
			mLocalModel.insertRecord(
					createCurrencyData(
							R.string.com_ovrhere_currConv_USD_symbol, 
							R.string.com_ovrhere_currConv_USD_code,
							R.string.com_ovrhere_currConv_USD_name,
							R.string.com_ovrhere_currConv_defRate_USD)
					);
			mLocalModel.insertRecord(
					createCurrencyData(
							R.string.com_ovrhere_currConv_CAD_symbol, 
							R.string.com_ovrhere_currConv_CAD_code,
							R.string.com_ovrhere_currConv_CAD_name,
							R.string.com_ovrhere_currConv_defRate_CAD)
				);
			mLocalModel.insertRecord(
					createCurrencyData(
							R.string.com_ovrhere_currConv_EUR_symbol, 
							R.string.com_ovrhere_currConv_EUR_code,
							R.string.com_ovrhere_currConv_EUR_name,
							R.string.com_ovrhere_currConv_defRate_EUR)
				);
			mLocalModel.insertRecord(
					createCurrencyData(
							R.string.com_ovrhere_currConv_GBP_symbol, 
							R.string.com_ovrhere_currConv_GBP_code,
							R.string.com_ovrhere_currConv_GBP_name,
							R.string.com_ovrhere_currConv_defRate_GBP)
				);
			mLocalModel.insertRecord(
					createCurrencyData(
							R.string.com_ovrhere_currConv_JPY_symbol, 
							R.string.com_ovrhere_currConv_JPY_code,
							R.string.com_ovrhere_currConv_JPY_name,
							R.string.com_ovrhere_currConv_defRate_JPY)
				);
		} catch (ParseException e) {
			Log.e(LOGTAG, "Float parse error occurred");
			e.printStackTrace();
			return false;
		} catch (SQLException e) {
			Log.e(LOGTAG, "Database error occurred");
			e.printStackTrace();
			return false;
		}
		return true;							
	}
	/**
	 * Creates {@link CurrencyData} based upon the ids supplied.
	 * @param symbolId The symbol string resource to use (e.g. $)
	 * @param codeId The 3 letter code string resource to use 
	 * @param nameId The name string resource to use
	 * @param rateId The rate string/float resource to use.
	 * @return CurrencyData
	 * @throws ParseException
	 */
	private CurrencyData createCurrencyData(int symbolId, int codeId, int nameId, 
			int rateId) throws ParseException{
		CurrencyData.Builder build = new CurrencyData.Builder();
		build.setCurrency(	res.getString(symbolId),
							res.getString(codeId),
							res.getString(nameId),
							Float.parseFloat (res.getString (rateId)))
			.setModifiedTimestamp(
							res.getString(R.string.com_ovrhere_currConv_defRate_updateTime)
					);		
		return build.create();		
	}
}
