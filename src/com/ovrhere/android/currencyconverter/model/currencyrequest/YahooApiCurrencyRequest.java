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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import com.ovrhere.android.currencyconverter.model.requests.AbstractSimpleHttpRequest;

/**
 * Makes yahoo api request for currencies. Requests are made to be non-redundant 
 * that is to say a request will not have BOTH USD->JPY && JPY->USD, but only one.
 * If the complement is needed, it should be calculated (1.0/rate).
 * 
 * <p>Provides callbacks for both
 * error handling and stream handling. 
 * Take note that the default timeout is {@value #DEFAULT_TIMEOUT}ms and
 * can be changed via {@link #setRequestTimeout(int)}. Additionally, JSON requests
 * can be set by {@link #setJsonFormat(boolean)}.</p>
 * <p>
 * Note that an instance of {@link YahooApiCurrencyRequest} will only run 
 * one request at a time and that requests are blocking</p>
 * @author Jason J.
 * @version 0.4.1-20140929
 */
public class YahooApiCurrencyRequest extends AbstractSimpleHttpRequest {
	/** The logtag for debuggin. */
	@SuppressWarnings("unused")
	final static private String LOGTAG = YahooApiCurrencyRequest.class
			.getSimpleName();
	
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
	/** Append to {@link #PREPARED_API_URL}/{@link #preparedRequest} to get 
	 * json format. */
	final static private String JSON_API_APPEND = "&format=json";
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// End constants
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** The prepared request ready for execution. */
	private String preparedRequest = "";
	/** Whether not to use json format. */
	private boolean jsonFormat = false;
	
	
	/** Creates and prepares a new request.
	 * @param sourceCodes The list of source currency codes in ISO 4217 form.
	 * @param destCodes The list of destination codes  in ISO 4217 form.
	 * @param onRequestEventListener The listener to process the results and errors.
	 */
	public YahooApiCurrencyRequest(String[] sourceCode, String[] destCodes,
			OnRequestEventListener onRequestEventListener) {
		super();
		prepareRequest(Arrays.asList(sourceCode), Arrays.asList(destCodes));
		this.setOnRequestEventListener(onRequestEventListener);
	}
	
	/** Creates and prepares a new request.
	 * @param sourceCodes The list of source currency codes in ISO 4217 form.
	 * @param destCodes The list of destination codes in ISO 4217 form.
	 * @param onRequestEventListener The listener to process the results and errors.
	 */
	public YahooApiCurrencyRequest
			(List<String> sourceCodes, List<String> destCodes, 
			OnRequestEventListener onRequestEventListener) {
		List<String> mDestCodes = new ArrayList<String>();
		mDestCodes.addAll(destCodes);
		prepareRequest(sourceCodes, mDestCodes);
		this.setOnRequestEventListener(onRequestEventListener);
	}
	
	/** Whether to use json format for the response. False by default.
	 * @param jsonFormat <code>true</code> for JSON, <code>false</code> for XML.
	 */
	public void setJsonFormat(boolean jsonFormat) {
		this.jsonFormat = jsonFormat;
		if (jsonFormat){
			if (!preparedRequest.contains(JSON_API_APPEND)){
				//add json string
				preparedRequest += JSON_API_APPEND;
			}
		} else { //remove json string
			preparedRequest.replaceAll(JSON_API_APPEND, "");
		}	
	}
	
	@Override
	protected String getPreparedRequest() {
		return preparedRequest;
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Helper methods
	////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Prepares the request url by formatting the input appropriately to CSV form: 
	 * <code>"USDEUR","USDJPY","USDGBP",...</code>
	 * @param sourceCodes The list source currency codes
	 * @param destCodes The list of destination codes 
	 * (note: will remove elements from this; cannot be same list as sourceCodes)
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
		//insert records & replace spaces (as the api does not handle spaces well)
		//We could also use URLEncoder.encode(String, Locale);
		preparedRequest  = String.format(Locale.US, PREPARED_API_URL, codeList)
								.replaceAll(" ", "%20");
		
		if (jsonFormat){
			preparedRequest += JSON_API_APPEND;
		}
	}
}
