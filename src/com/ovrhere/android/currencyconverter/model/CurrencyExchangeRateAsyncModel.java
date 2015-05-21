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
package com.ovrhere.android.currencyconverter.model;

import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;

import com.ovrhere.android.currencyconverter.R;
import com.ovrhere.android.currencyconverter.dao.CurrencyData;
import com.ovrhere.android.currencyconverter.dao.SimpleExchangeRates;
import com.ovrhere.android.currencyconverter.model.asyncmodel.AsyncModel;
import com.ovrhere.android.currencyconverter.model.asyncmodel.RunnableHeadlessFragment;
import com.ovrhere.android.currencyconverter.model.currencyrequest.YahooApiCurrencyJsonParser;
import com.ovrhere.android.currencyconverter.model.currencyrequest.YahooApiCurrencyRequest;
import com.ovrhere.android.currencyconverter.model.currencyrequest.YahooApiCurrencyXmlParser;
import com.ovrhere.android.currencyconverter.prefs.PreferenceUtils;
import com.ovrhere.android.currencyconverter.utils.Timestamp;
/**
 * 
 * Remember to call {@link #dispose()} to start cleanup and release 
 * the {@link Context}.
 * 
 * @author Jason J.
 * @version 0.4.1-20140929
 */
public class CurrencyExchangeRateAsyncModel extends AsyncModel 
implements YahooApiCurrencyRequest.OnRequestEventListener {
	/** Class name for debugging purposes. */
	final static private String CLASS_NAME = CurrencyExchangeRateAsyncModel.class
			.getSimpleName();
	/** The log tag to use. */
	final static private String LOGTAG = CLASS_NAME;
	/** The boolean for debugging. */
	final static private boolean DEBUG = false;
	
	/** The tag for this fragment. */
	final static private String RUNNABLE_HEADLESS_FRAG_TAG = 
			CLASS_NAME+"."+RunnableHeadlessFragment.class.getSimpleName();
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// End private constants
	////////////////////////////////////////////////////////////////////////////////////////////////

	/** Retrieves all records from the database.
	 * If accompanied by a boolean <code>true</code>, 
	 * cached records are forced to update and ONLY replies if they are
	 * updated.  
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
	/** The local access to shared preferences. */
	private SharedPreferences prefs = null;
	
	/** A list of all currencies. */
	final private List<String> currencyCodeList = new ArrayList<String>(); 
	
	/** Whether or not the current request is using JSON.
	 *  Set in {@link #requestApiValues(List)}	 */
	private boolean usingJson = false;
	
	/** Whether the model is disposed. false by default, true in #dispose() */
	volatile private boolean isDisposed = false;
	/** Whether or not the model is updating.
	 * Set true in {@link #onStart(InputStream)} and false in {@link #onComplete()}
	 * or {@link #onException(Exception)}	 */
	volatile private boolean isUpdating = false;
	
	/** Runnable to dispose of the contexts. This is so if we are disposing 
	 * while a thread is still running we can dispose AFTER we do the work. */
	private Runnable asyncDispose = new Runnable() {		
		@Override
		public void run() {
			mLocalModel = null; 
			res = null;
			mfragManager = null;	
			prefs = null;
			//clear all contexts
			
			if (DEBUG){
				Log.d(LOGTAG, "asyncDispose in: "+ 
			CurrencyExchangeRateAsyncModel.this.hashCode());
			}
		}
	};
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// End class members 
	////////////////////////////////////////////////////////////////////////////////////////////////
	/** Initializes asynchronous model. 
	 * @param activity The application's activity. */
	public CurrencyExchangeRateAsyncModel(FragmentActivity activity) {
		this.mLocalModel = new CurrencyExchangeRateModel(activity);
		this.res = activity.getResources();
		this.currencyCodeList.addAll( 
		Arrays.asList(
					res.getStringArray(R.array.currConv_rateOrder))
					);
		mfragManager = activity.getSupportFragmentManager();
		//prepare the headless fragment in advance.
		RunnableHeadlessFragment runFrag = getHeadlessFrag();
		if (runFrag.getRunnable() != null){ //if there are any runnables
			try {
				YahooApiCurrencyRequest req = (YahooApiCurrencyRequest) 
						runFrag.getRunnable();
				//update any running to the current object
				req.setOnRequestEventListener(this);
			} catch (ClassCastException e){}
		}
		
		this.prefs = PreferenceUtils.getPreferences(activity);
	}

	
	@Override
	public void dispose() {
		super.dispose();
		isDisposed = true;
		if (!isUpdating){
			disposeHelper();
		}
	}
	
	
	@Override
	public int sendMessage(int what, Object object) {
		if (mLocalModel == null){
			return 1;
		}
		if (DEBUG){
			Log.d(LOGTAG, "sendMessage: "+what);
		}
		
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
	

	/*
	 * Response logic is as follows:
	 * -If records are empty/forced update: request update
	 * -A) If continuing as normal: 
	 *   + update database 
	 *   + re-fetch db values and send values
	 * -B) If the model is "disposed" during an update: 
	 *  + Set a check for disposal
	 *  + Run request until update and update the model-x
	 *  + dispose model-x after update; releasing context
	 *  + if request is made in new model-y while model-x is still running:
	 *    re-attach model to runnable, and follow route "A" above
	 */
	
	/** The basic request of records.	 
	 * @param force <code>true</code> forces and update, <code>false</code>
	 * does not.*/
	private void requestRecords(boolean force){
		List<CurrencyData> records = mLocalModel.getAllRecords();
		if (force){
			requestApiValues(records);
			return;
		}
		if (records.isEmpty()){
			if (initDefaultDatabase()){
				records = mLocalModel.getAllRecords();
				requestApiValues(records);
			} else {
				notifyHandlers(ERROR_REQUEST_FAILED, null);
				return;
			}
		}
		replyWithRecords(records);
	}
	
	/** The request for api values from {@link YahooApiCurrencyRequest}. */
	private void requestApiValues(List<CurrencyData> records){
		if (isUpdating){
			return; //we are already updating, it will return eventually
		}
		isUpdating = true;
		//the lazy approach
		usingJson = prefs.getBoolean(
				res.getString(R.string.currConv_pref_KEY_USE_JSON_REQUEST), 
				false);
		
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
				new YahooApiCurrencyRequest(currencyCodeList, currencyCodeList, 
						this);
		request.setJsonFormat(usingJson);
		if (DEBUG){
			Log.d(LOGTAG, "Using request: " + request.toString());
		}
		
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
							R.string.currConv_USD_symbol, 
							R.string.currConv_USD_code,
							R.string.currConv_USD_name,
							R.array.currConv_USD_defRates)
					);
			mLocalModel.insertRecord(
					createCurrencyData(
							R.string.currConv_CAD_symbol, 
							R.string.currConv_CAD_code,
							R.string.currConv_CAD_name,
							R.array.currConv_CAD_defRates)
				);
			mLocalModel.insertRecord(
					createCurrencyData(
							R.string.currConv_EUR_symbol, 
							R.string.currConv_EUR_code,
							R.string.currConv_EUR_name,
							R.array.currConv_EUR_defRates)
				);
			mLocalModel.insertRecord(
					createCurrencyData(
							R.string.currConv_GBP_symbol, 
							R.string.currConv_GBP_code,
							R.string.currConv_GBP_name,
							R.array.currConv_GBP_defRates)
				);
			mLocalModel.insertRecord(
					createCurrencyData(
							R.string.currConv_JPY_symbol, 
							R.string.currConv_JPY_code,
							R.string.currConv_JPY_name,
							R.array.currConv_JPY_defRates)
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
	 * @param rateArrayId The rate string array resource to use.
	 * @return CurrencyData
	 * @throws ParseException
	 * @throws {@link NumberFormatException}
	 */
	private CurrencyData createCurrencyData(int symbolId, int codeId, int nameId, 
			int rateArrayId) throws ParseException{
		String defRates[] = res.getStringArray(rateArrayId);
		if (defRates.length != currencyCodeList.size()){
			if (DEBUG){
				Log.w(LOGTAG, "Irregular behavior: Mismatched lists?");
			}
			throw new IndexOutOfBoundsException();
		}
		HashMap<String, Double> exchangeRates = new HashMap<String, Double>();
		final int SIZE = currencyCodeList.size();
		for (int index = 0; index < SIZE; index++) {
			exchangeRates.put(
					currencyCodeList.get(index), 
					Double.parseDouble(defRates[index]));
		}
		
		CurrencyData.Builder build = new CurrencyData.Builder();
		build.setCurrency(	res.getString(symbolId),
							res.getString(codeId),
							res.getString(nameId))
			.setModifiedTimestamp(
							res.getString(R.string.currConv_defRate_updateTime)
					)
			.setExchangeRates(exchangeRates);
		
		return 	build.create();
	}
	
	/** Updates the local model's record rates
	 * @param rates The rates to apply to the records. 	 */
	private void updateRecordRates(SimpleExchangeRates rates) {
		List<CurrencyData> records = mLocalModel.getAllRecords();
		String timestamp = Timestamp.getUtc();
		
		for(CurrencyData record : records){
			String sCode = record.getCurrencyCode();
			HashMap<String, Double> rateMap = new HashMap<String, Double>();
			
			for (String dCode : currencyCodeList) {
				double rate= rates.getRate(sCode, dCode);
				rateMap.put(dCode, rate);
			}
			rateMap.put(sCode, 1.0d); //put itself, for safety.
			
			int id = record.getId();
			
			CurrencyData.Builder builder = new CurrencyData.Builder();
			try {
				builder.setId(id)
				.setCurrency(
					record.getCurrencySymbol(), 
					sCode, 
					record.getCurrencyName())
				.setModifiedTimestamp(timestamp)
				.setExchangeRates(rateMap);
				
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
				
			} catch (IllegalArgumentException e){
				if (DEBUG){
					Log.e(LOGTAG, "Bad argument given at: " +
							sCode+" - " + e);
					e.printStackTrace();
					throw e; //this should kill
				}
				//TODO thought: perhaps this should wipe database/reset it?
			} catch (Exception e){
				if (DEBUG){
					Log.e(LOGTAG, "Other exception occurred at: " +
							sCode+" - " + e);
					e.printStackTrace();
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
	
	/** Disposes via the runnable method. */
	private void disposeHelper() {
		if (isDisposed){
			asyncDispose.run();
		}
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Implemented Listeners
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	@Override
	public void onStart(InputStream in) {
		if (DEBUG){
			Log.d(LOGTAG, "Starting in object: " +this.hashCode());
		}		
		isUpdating = true;
		
		SimpleExchangeRates rates = null;		
		try {
			if (usingJson){
				YahooApiCurrencyJsonParser parser = new YahooApiCurrencyJsonParser();
				rates = parser.parseJsonStream(in);
			} else {
				YahooApiCurrencyXmlParser parser = new YahooApiCurrencyXmlParser();
				rates = parser.parseXmlStream(in);
			}
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
		disposeHelper();
	}
	
	
	@Override
	public void onException(Exception exception) {
		if (DEBUG){
			Log.d(LOGTAG, "Exception in object: " +this.hashCode());
		}
		try {
			throw exception;		
		} catch (SocketTimeoutException e){
			if (DEBUG){
				e.printStackTrace();
			}
			notifyHandlers(ERROR_REQUEST_TIMEOUT, null);
		} catch (IOException e){
			if (DEBUG){
				e.printStackTrace();
			}
			notifyHandlers(ERROR_REQUEST_FAILED, null);
		} catch (Exception e){
			if (DEBUG){
				e.printStackTrace();
			}
			notifyHandlers(ERROR_REQUEST_FAILED, null);
		}

		isUpdating = false;
		disposeHelper();
	}


	@Override
	public void onResponseCode(int responseCode) {
		if (DEBUG){
			Log.d(LOGTAG, "Response code: " + responseCode);
		}
	}
	
	@Override
	public void onComplete() {
		if (DEBUG){
			Log.d(LOGTAG, "request: onComplete in: " + this.hashCode());
		}
		//reply when finished.
		replyWithRecords(mLocalModel.getAllRecords());
		getHeadlessFrag().setRunable(null); //remove runnable
		
		isUpdating = false;
		disposeHelper();
	}
	
	
}
