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
package com.ovrhere.android.currencyconverter.model.currencyrequest;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import android.util.Log;

/**
 * Makes yahoo api request for currencies. Requests are made to be non-redundant 
 * that is to say a request will not have BOTH USD -> JPY && JPY->USD, but only one.
 * If the complement is needed, it should be calculated (1.0/rate).
 * 
 * <p>Provides callbacks for both
 * error handling and stream handling. 
 * Take note that the default timeout is {@value #DEFAULT_TIMEOUT}ms and
 * can be changed via {@link #setRequestTimeout(int)}. Additionally, JSON requests
 * can be set by {@link #setJsonFormat(boolean)}.</p>
 * <p>
 * Note that an instance of {@link YahooApiCurrencyRequest} will only run 
 * one request at a time.</p>
 * @author Jason J.
 * @version 0.2.0-20140908
 */
public class YahooApiCurrencyRequest implements Runnable {
	/** The logtag for debuggin. */
	final static private String LOGTAG = YahooApiCurrencyRequest.class
			.getSimpleName();
	/** The default timeout period in milliseconds. */ 
	final static private int DEFAULT_TIMEOUT = 10000; //ms
	
	/** The API url to  base the query on.  
	 *  Use {@link String#format(java.util.Locale, String, Object...)  
	 *  to replace this with the actual request form. 
	 *  Note the form is: <code>"USDEUR","USDJPY","USDGBP",...</code> 
	 *  where USD is the starting currency. */
	final static private String PREPARED_API_URL =
			"http://query.yahooapis.com/v1/public/yql?"
			+ "q=select * from yahoo.finance.xchange where pair in (" +
			"%s"+
			")&env=store://datatables.org/alltableswithkeys";
	/** Append to {@link #PREPARED_API_URL} to get json format. */
	final static private String JSON_API_APPEND = "&format=json";
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// End constants
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** The prepared request ready for execution. */
	private String preparedRequest = "";
	/** The required listener for the request. */
	private OnRequestEventListener mRequestEventListener = null;
	/** The request timeout period in milliseconds. */
	private int requestTimeout = DEFAULT_TIMEOUT;
	/** Whether not to use json format. */
	private boolean jsonFormat = false;
	
	
	/** Creates and prepares a new request.
	 * @param sourceCodes The list of source currency codes
	 * @param destCodes The list of destination codes
	 * @param onRequestEventListener The listener to process the results and errors.
	 */
	public YahooApiCurrencyRequest(String[] sourceCode, String[] destCodes,
			OnRequestEventListener onRequestEventListener) {
		prepareRequest(Arrays.asList(sourceCode), Arrays.asList(destCodes));
		this.mRequestEventListener = onRequestEventListener;
	}
	
	/** Creates and prepares a new request.
	 * @param sourceCodes The list of source currency codes
	 * @param destCodes The list of destination codes
	 * @param onRequestEventListener The listener to process the results and errors.
	 */
	public YahooApiCurrencyRequest
			(List<String> sourceCodes, List<String> destCodes, 
			OnRequestEventListener onRequestEventListener) {
		List<String> mSourceCodes = new ArrayList<String>(); 
		mSourceCodes.addAll(sourceCodes);
		List<String> mDestCodes = new ArrayList<String>();
		mDestCodes.addAll(destCodes);
		prepareRequest(mSourceCodes, mDestCodes);
		this.mRequestEventListener = onRequestEventListener;
	}
	
	/** Creates and prepares a new request.
	 * @deprecated Use {@link #YahooApiCurrencyRequest(String[], String[], OnRequestEventListener)}
	 * instead
	 * @param sourceCode The source currency code
	 * @param destCodes The list of destination codes
	 * @param onRequestEventListener The listener to process the results and errors.
	 */
	@Deprecated
	public YahooApiCurrencyRequest(String sourceCode, String[] destCodes,
			OnRequestEventListener onRequestEventListener) {
		List<String> sourceCodes = new ArrayList<String>();
		sourceCodes.add(sourceCode);
		prepareRequest(sourceCodes, Arrays.asList(destCodes));
		this.mRequestEventListener = onRequestEventListener;
	}
	
	/** Creates and prepares a new request.	 * 
	 * @deprecated Use {@link #YahooApiCurrencyRequest(List, List, OnRequestEventListener)}
	 * instead.
	 * @param sourceCode The source currency code
	 * @param destCodes The list of destination codes
	 * @param onRequestEventListener The listener to process the results and errors.
	 */
	@Deprecated
	public YahooApiCurrencyRequest(String sourceCode, List<String> destCodes,
			OnRequestEventListener onRequestEventListener) {
		List<String> sourceCodes = new ArrayList<String>();
		sourceCodes.add(sourceCode);
		List<String> mDestCodes = new ArrayList<String>();;
		mDestCodes.addAll(mDestCodes);
		prepareRequest(sourceCodes, mDestCodes);
		this.mRequestEventListener = onRequestEventListener;
	}
	
	/** Sets a request event listener. 
	 * @param onRequestEventListener The implementer of this interface	 */
	public void setOnRequestEventListener(
			OnRequestEventListener onRequestEventListener) {
		this.mRequestEventListener = onRequestEventListener;
	}
	/** Sets how long in milliseconds before the request gives up. Will not 
	 * take effect during a request.
	 * @param requestTimeout The time in milliseconds	 */
	public void setRequestTimeout(int requestTimeout) {
		this.requestTimeout = requestTimeout;
	}
	/** Returns the timeout period before giving up.
	 * @return The timeout in milliseconds	 */
	public long getRequestTimeout() {
		return requestTimeout;
	}
	/** Whether to use json format for the response. False by default.
	 * @param jsonFormat <code>true</code> for JSON, <code>false</code> for XML.
	 */
	public void setJsonFormat(boolean jsonFormat) {
		this.jsonFormat = jsonFormat;
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Runnable
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	@Override
	public void run() {
		synchronized (preparedRequest) {
			final int QUERY_TIMEOUT = requestTimeout;
			
			HttpURLConnection urlConnection = null;
			int responseCode = 0;
			try {
				URL url = new URL(preparedRequest);
				urlConnection = (HttpURLConnection) url.openConnection();
				urlConnection.setConnectTimeout((int) QUERY_TIMEOUT);
				urlConnection.setReadTimeout((int) QUERY_TIMEOUT);
				urlConnection.setRequestMethod("GET");
				urlConnection.setDoInput(true);
			    // Start query
				urlConnection.connect();
				responseCode = urlConnection.getResponseCode();

				InputStream in = 
						new BufferedInputStream(urlConnection.getInputStream());
				
				mRequestEventListener.onStart(in);
			} catch (MalformedURLException e){
				Log.e(LOGTAG, "Poorly formed request: "+e);
				mRequestEventListener.onException(e);
				return;
			} catch(IOException e){
				Log.e(LOGTAG, 
						"Cannot perform request (response code:"+
						responseCode+"): "+e);
				mRequestEventListener.onException(e);
				return;				
			}  catch (Exception e){
				Log.w(LOGTAG, "Unexpected error occurred: " + e);
				mRequestEventListener.onException(e);
				return;
			}
			mRequestEventListener.onComplete();	
		}
		
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Helper methods
	////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Prepares the request url by formatting the input appropriately to CSV form: 
	 * <code>"USDEUR","USDJPY","USDGBP",...</code>
	 * @param sourceCodes The list source currency codes
	 * @param destCodes The list of destination codes
	 */
	private void prepareRequest(List<String> sourceCodes, List<String> destCodes){
		String codeList = "";
		for (String sCode : sourceCodes){
			for (int index = 0; index < destCodes.size(); index++){
				String dCode = destCodes.get(index);
				if (sCode.equalsIgnoreCase(dCode)){
					continue;
				}
				if (!codeList.isEmpty()){
					codeList += ",";
				}
				codeList += "\""+sCode+dCode+"\"";
			}
			destCodes.remove(sCode); //do not request twice
		}		
		preparedRequest  = String.format(Locale.US, PREPARED_API_URL, codeList)
								.replaceAll(" ", "%20");
		if (jsonFormat){
			preparedRequest += JSON_API_APPEND;
		}
	}
	
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Internal interfaces
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** Provides a list of methods to notify the listener of runnable events 
	 * and results.
	 *  @version 0.1.0-20140905	 */
	public interface OnRequestEventListener {
		/** Sends any exceptions encountered to the listener.
		 * @param e The forwarded exception, if any.		 */
		public void onException(Exception e);
		/** Sent when the request starts
		 * @param in The input stream being used		 */
		public void onStart(InputStream in);
		/** When the request run has been concluded. */
		public void onComplete();
	}

}
