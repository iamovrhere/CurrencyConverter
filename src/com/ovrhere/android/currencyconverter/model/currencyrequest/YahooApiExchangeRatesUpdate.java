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
package com.ovrhere.android.currencyconverter.model.currencyrequest;

import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;

import org.xmlpull.v1.XmlPullParserException;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.util.Log;

import com.ovrhere.android.currencyconverter.model.data.CurrencyConverterContract.ExchangeRateEntry;
import com.ovrhere.android.currencyconverter.model.requests.AbstractSimpleHttpRequest.OnRequestEventListener;

/**
 * The currency update with requests, parses, and database update. Use {@link #run()} to
 * run. Will retry {@value #RETRY_LIMIT} times every {@value #RETRY_INTERVAL} milliseconds
 * if not successful on the first attempt.
 * 
 * @author Jason J.
 * @version 0.2.0-20150525
 */
public class YahooApiExchangeRatesUpdate implements Runnable,  OnRequestEventListener{
	/** Class name for debugging purposes. */
	final static private String LOGTAG = YahooApiExchangeRatesUpdate.class
			.getSimpleName();
	/** Verbose debug; for when we want all the excessive details. */
	final static private boolean VERBOSE_DEBUG = false;	

	/** The number of times to retry the connection. */
	private static final int RETRY_LIMIT = 3;
	/** The wait time between attempts. */
	private static final long RETRY_INTERVAL = 1000; //ms

	/** Resolver used to insert into the database.  */
	private final ContentResolver mContentResolver; 
	/** Used to perform requests in run. */
	private final YahooApiCurrencyRequest mRequest;
	/** Whether or not to request/parse for json. */
	private final boolean mUseJson;
	
	/** The value for if the update was successful. */
	private boolean mUpdateSuccessful = false;
	
	
	/** the number of attempts thus far. */
	private int mRetryAttempts = 0;
	
	/**
	 * Uses request & parses together to update the content provider.
	 * @param resolver {@link ContentResolver} used to update the database. 
	 * @param currencyList The list of currents, (both ways) to get exchange rates for. 
	 * @param useJson <code>true</code> to use json request + parsing, <code>false</code>
	 * to use XML
	 */
	public YahooApiExchangeRatesUpdate(ContentResolver resolver, String[] currencyList, boolean useJson) {
		this.mContentResolver = resolver;
		this.mUseJson = useJson;		
		this.mRequest = new YahooApiCurrencyRequest(currencyList, currencyList, this);
		this.mRequest.setJsonFormat(useJson);
	}
	

	
	@Override
	public void run() {
		mRequest.run();
	}
	
	/**
	 * @return <code>true</code> if it completed successfully, 
	 * <code>false</code> if it did not.
	 */
	public boolean isUpdateSuccessful() {
		return mUpdateSuccessful;
	}
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Helper methods.
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	private void retry(){
		if (mRetryAttempts++ < RETRY_LIMIT) {
			try {
				Thread.sleep(RETRY_INTERVAL);
			} catch (InterruptedException e) {}
			run();
		}
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Event Listener
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	@Override
	public void onStart(InputStream in) {
		ContentValues[] values = new ContentValues[]{};
		if (mUseJson) {
			try {
				values = new YahooApiCurrencyJsonParser().parseJsonStream(in);
			} catch (IOException badParsing) {
				if (VERBOSE_DEBUG){
					badParsing.printStackTrace();
				}
			}
		} else {
			try {
				values = new YahooApiCurrencyXmlParser().parseXmlStream(in);
			} catch (XmlPullParserException parsingIssue) {
				if (VERBOSE_DEBUG){
					parsingIssue.printStackTrace();
				}
			} catch (IOException ioIssue) {
				if (VERBOSE_DEBUG){
					ioIssue.printStackTrace();
				}
			}
		}
		long insertCount = mContentResolver.bulkInsert(ExchangeRateEntry.CONTENT_URI, values);		
		//we're done!
		if (VERBOSE_DEBUG) {
			Log.d(LOGTAG, insertCount + " records inserted");
		}
	}
	
	@Override
	public void onResponseCode(int responseCode) {
		if (VERBOSE_DEBUG) {
			Log.d(LOGTAG, "Response code: " + responseCode);
		}
	}		
	@Override
	public void onException(Exception e) {
		if (e instanceof SocketTimeoutException) {
			Log.w(LOGTAG, "Connection timed out");
			retry();
		} else if (e instanceof IOException) {
			Log.w(LOGTAG, "IO issue: Could not connect to server:" + e);
			retry();
		} else {
			Log.e(LOGTAG, "Unexpected error during update: " + e);
		}
		if (VERBOSE_DEBUG){ 
			e.printStackTrace();
		}
	}		
	@Override
	public void onComplete() {
		this.mUpdateSuccessful = true;
	}
	
}
