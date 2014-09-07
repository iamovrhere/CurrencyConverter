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

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.content.res.Resources;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;

import com.ovrhere.android.currencyconverter.R;
import com.ovrhere.android.currencyconverter.dao.CurrencyData;
import com.ovrhere.android.currencyconverter.model.asyncmodel.AsyncModel;
import com.ovrhere.android.currencyconverter.model.asyncmodel.RunnableHeadlessFragment;
import com.ovrhere.android.currencyconverter.model.currencyrequest.YahooApiCurrencyRequest;
import com.ovrhere.android.currencyconverter.model.currencyrequest.YahooApiCurrencyXmlParser;
import com.ovrhere.android.currencyconverter.utils.Timestamp;

/**
 * 
 * Remember to call {@link #dispose()} to start cleanup and release 
 * the {@link Context}.
 * 
 * @author Jason J.
 * @version 0.2.0-20140905
 */
public class CurrencyExchangeRateAsyncModel extends AsyncModel 
implements YahooApiCurrencyRequest.OnRequestEventListener {
	/** Class name for debugging purposes. */
	final static private String CLASS_NAME = CurrencyExchangeRateAsyncModel.class
			.getSimpleName();
	/** The log tag to use. */
	final static private String LOGTAG = CLASS_NAME;
	/** The boolean for debugging. */
	final static private boolean DEBUG = true;
	
	/** The tag for this fragment. */
	final static private String RUNNABLE_HEADLESS_FRAG_TAG = 
			CLASS_NAME+"."+RunnableHeadlessFragment.class.getSimpleName();
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// End private constants
	////////////////////////////////////////////////////////////////////////////////////////////////

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
	 * cached records are forced to update. 
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
	/** The fragment manager for getting the fragment. */
	private FragmentManager mfragManager = null;
	/** The base currency code. */
	final private String baseCurrencyCode; 
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// End class members 
	////////////////////////////////////////////////////////////////////////////////////////////////
	/** Initializes asynchronous model. 
	 * @param activity The application's activity. */
	public CurrencyExchangeRateAsyncModel(FragmentActivity activity) {
		this.mLocalModel = new CurrencyExchangeRateModel(activity);
		this.res = activity.getResources();
		this.baseCurrencyCode = 
				res.getString(R.string.com_ovrhere_currConv_USD_code);
		mfragManager = activity.getSupportFragmentManager();
		//prepare the headless fragment in advance.
		getHeadlessFrag(); 
	}

	
	/** {@link Deprecated} Use {@link #CurrencyExchangeRateAsyncModel(FragmentActivity)}.
	 *   @param context The context to use when opening the model. */
	@Deprecated
	public CurrencyExchangeRateAsyncModel(Context context) {
		this.mLocalModel = new CurrencyExchangeRateModel(context);
		this.res = context.getResources();
		this.baseCurrencyCode = 
				res.getString(R.string.com_ovrhere_currConv_USD_code);
	}
	
	@Override
	public void dispose() {
		super.dispose();
		this.mLocalModel = null; 
		this.res = null;
		this.mfragManager = null;
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
			if (((Boolean) object)){
				requestRecords(true);
			} else {
				requestRecords();
			}
			return 0;

		default:
			//break;
		}
		return -1;
	}	
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Request methods
	////////////////////////////////////////////////////////////////////////////////////////////////
	/** Convenience for {@link #requestRecords(boolean)} with <code>false</code>. */
	private void requestRecords(){
		requestRecords(false);
	}
	/** The basic request of records.	 
	 * @param force <code>true</code> forces and update, <code>false</code>
	 * does not.*/
	private void requestRecords(boolean force){
		List<CurrencyData> records = mLocalModel.getAllRecords();
		if (records.isEmpty()){
			if (initDefaultDatabase()){
				records = mLocalModel.getAllRecords();
				requestApiValues(records);
			} else {
				notifyHandlers(ERROR_REQUEST_FAILED, null);
				return;
			}
		} else if (force){
			requestApiValues(records);
		}
		replyWithRecords(records);
	}
	/** The request for api values from {@link YahooApiCurrencyRequest}. */
	private void requestApiValues(List<CurrencyData> records){
		if (records.isEmpty()){
			if (DEBUG){
				Log.w(LOGTAG, "The records are empty. What are we to update?");
			}
			return; //we will not try
		}
		List<String> destCodes = new ArrayList<String>();
		for(CurrencyData cd : records ){
			destCodes.add(cd.getCurrencyCode());
		}
		YahooApiCurrencyRequest request = 
				new YahooApiCurrencyRequest(baseCurrencyCode, destCodes, 
						this);
		notifyHandlers(NOTIFY_UPDATING_RATES, null);
		RunnableHeadlessFragment runFrag = getHeadlessFrag();
		runFrag.setRunable(request);
		runFrag.startThread();
		
	}

		
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Helper methods
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** Sends reply with records, ensuring the drawables are placed first. 
	 * @param records The records to attach drawables to and send.	 */
	private void replyWithRecords(List<CurrencyData> records) {
		for (Iterator<CurrencyData> iterator = records.iterator(); iterator.hasNext();) {
			FlagResourceMap.setCurrencyFlagDrawable(res, iterator.next());	
		}
		notifyHandlers(REPLY_RECORDS_RESULT, records);
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
	
	/** Updates the local model's record rates
	 * @param rates The rates to apply to the records. 	 */
	private void updateRecordRates(HashMap<String, Float> rates) {
		List<CurrencyData> records = mLocalModel.getAllRecords();
		String timestamp = Timestamp.getUtc();
		//add the base currency code to be updated as well.
		rates.put(baseCurrencyCode, 1.0f);
		
		for(CurrencyData record : records){
			String cCode = record.getCurrencyCode();
			if (rates.containsKey(cCode) ){ //if the rate was found
				float rate = rates.get(cCode);
				int id = record.getId();
				CurrencyData.Builder builder = new CurrencyData.Builder();
				try {
					builder.setId(id)
					.setCurrency(
						record.getCurrencySymbol(), 
						cCode, 
						record.getCurrencyName(), 
						rate)
					.setModifiedTimestamp(timestamp);
					if (DEBUG){
						Log.d(LOGTAG, 
								"Updating: "+id + " - " + builder.create() );
					}
					mLocalModel.modifyRecord(id, builder.create());
				} catch (SQLException e) {
					if (DEBUG){
						Log.e(LOGTAG, 
								"Record "+record.getId()+" not updated: " + e);
					}
				} catch (ParseException e) {
					if (DEBUG){
						e.printStackTrace();
					}
				}
			} else {
				if (DEBUG){ //the record is not found in result.
					Log.d(LOGTAG, "Skipping '"+cCode+"'");
				}
			}
		}
	}
	
	/** Gets headless fragment if found in manager. Otherwise, places it into 
	 * the manager
	 * @return The headless fragment to attach runnables to.	 */
	private RunnableHeadlessFragment getHeadlessFrag() {
		RunnableHeadlessFragment runFrag = (RunnableHeadlessFragment)
				mfragManager.findFragmentByTag(RUNNABLE_HEADLESS_FRAG_TAG);
		if (runFrag == null ){
			runFrag = new RunnableHeadlessFragment();
			mfragManager.beginTransaction()
					.add(runFrag, RUNNABLE_HEADLESS_FRAG_TAG)
					.commit();
		}
		return runFrag;
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Implemented Listeners
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	@Override
	public void onStart(InputStream in) {
		HashMap<String, Float> rates = new HashMap<String, Float>();		
		try {
			YahooApiCurrencyXmlParser parser = new YahooApiCurrencyXmlParser();
			rates = parser.parseXmlStream(in);
			in.close();
		} catch (XmlPullParserException e){
			if (DEBUG){
				e.printStackTrace();
			}
		} catch (IOException e) {
			if (DEBUG){
				e.printStackTrace();
			}
		}		
		updateRecordRates(rates);
		replyWithRecords(mLocalModel.getAllRecords());
	}
	
	
	@Override
	public void onException(Exception exception) {
		try {
			throw exception;
		} catch (IOException e){
			if (DEBUG){
				e.printStackTrace();
			}
			notifyHandlers(ERROR_REQUEST_TIMEOUT, null);
		} catch (Exception e){
			if (DEBUG){
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public void onComplete() {
		if (DEBUG){
			Log.d(LOGTAG, "request: onComplete");
		}
		//nothing to do here
	}
	
	
}
