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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import android.net.Uri;

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
 * @version 0.5.0-20150521
 */
public class YahooApiCurrencyRequest extends AbstractSimpleHttpRequest {
	/** The logtag for debugging. */
	@SuppressWarnings("unused")
	final static private String LOGTAG = YahooApiCurrencyRequest.class
			.getSimpleName();
	
	/** The API base url. */
	final static private String API_BASE = "http://query.yahooapis.com/v1/public/yql";
		
	/** Format query String. */
	final static private String QUERY_FORMAT = "format";
	/** Env query String. */
	final static private String QUERY_ENV = "env";	
	/** Prepared Select query String. */
	final static private String QUERY_PREPARED_SELECT = "q";	
	
	/** The API query.  
	 *  Use {@link String#format(java.util.Locale, String, Object...)  
	 *  to replace this with the actual request form. 
	 *  Note the form is: <code>"USDEUR","USDJPY","USDGBP",...</code> 
	 *  where USD is the starting currency. */
	final static private String VALUE_PREPARED_SELECT =
			"select * from yahoo.finance.xchange where pair in (%s)";
			
	final static private String VALUE_ENV_TABLE = "store://datatables.org/alltableswithkeys";
	
	/** The format type. */
	final static private String VALUE_FORMAT_JSON = "json";
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// End constants
	////////////////////////////////////////////////////////////////////////////////////////////////
		
	/** Whether not to use json format. */
	private boolean jsonFormat = false;
	
	/** The select query for the query {@link #QUERY_PREPARED_SELECT}. */
	final private String selectQuery;
	
	
	/** Creates and prepares a new request.
	 * @param sourceCodes The list of source currency codes in ISO 4217 form.
	 * @param destCodes The list of destination codes  in ISO 4217 form.
	 * @param onRequestEventListener The listener to process the results and errors.
	 */
	public YahooApiCurrencyRequest(String[] sourceCodes, String[] destCodes,
			OnRequestEventListener onRequestEventListener) {
		super();
		List<String> dstCodes = new ArrayList<String>();
		dstCodes.addAll(Arrays.asList(destCodes));
		List<String> srcCodes = new ArrayList<String>();
		srcCodes.addAll(Arrays.asList(sourceCodes));
		
		this.selectQuery = prepareQuery(srcCodes, dstCodes);
		this.setOnRequestEventListener(onRequestEventListener);
	}
	
	/** Creates and prepares a new request.
	 * @param sourceCodes The list of source currency codes in ISO 4217 form.
	 * @param destCodes The list of destination codes in ISO 4217 form.
	 * @param onRequestEventListener The listener to process the results and errors.
	 */
	public YahooApiCurrencyRequest (List<String> sourceCodes, List<String> destCodes, 
			OnRequestEventListener onRequestEventListener) {
		List<String> dstCodes = new ArrayList<String>();
		dstCodes.addAll(destCodes);
		this.selectQuery = prepareQuery(sourceCodes, dstCodes);
		this.setOnRequestEventListener(onRequestEventListener);
	}
	
	/** Whether to use json format for the response. False by default.
	 * @param jsonFormat <code>true</code> for JSON, <code>false</code> for XML.
	 */
	public void setJsonFormat(boolean jsonFormat) {
		this.jsonFormat = jsonFormat;
	}
	
	@Override
	protected Uri getUriRequest() {
		Uri.Builder builder = Uri.parse(API_BASE).buildUpon();
		builder	.appendQueryParameter(QUERY_PREPARED_SELECT, selectQuery)
				.appendQueryParameter(QUERY_ENV, VALUE_ENV_TABLE);
		
		if (jsonFormat) {
			builder.appendQueryParameter(QUERY_FORMAT, VALUE_FORMAT_JSON);
		}
		
		return builder.build();
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
	private String prepareQuery(List<String> sourceCodes, List<String> destCodes){
		String codeList = "";
		for (String sCode : sourceCodes){
			final int SIZE = destCodes.size();
			for (int index = 0; index < SIZE; index++){
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
		return String.format(Locale.US, VALUE_PREPARED_SELECT, codeList);
	}
}
