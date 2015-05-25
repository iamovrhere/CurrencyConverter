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
import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.util.Log;

import com.ovrhere.android.currencyconverter.model.data.CurrencyConverterContract;
import com.ovrhere.android.currencyconverter.model.parsers.AbstractJsonParser;

/**
 * <p>The JSON parser for the yahoo currency exchange api.
 * Found at: <code>http://query.yahooapis.com/v1/public/yql?...&format=json</code></p>
 * 
 * Note that this will create double values from compact results;
 * i.e. take USD -> CAD rates and manufacture CAD -> USD rates. 
 * 
 * @author Jason J.
 * @version 0.2.0-20150523
 * @see YahooApiCurrencyRequest
 */
public class YahooApiCurrencyJsonParser extends AbstractJsonParser<ContentValues[]>{
	/** For debugging purposes. */
	final static private String LOGTAG = YahooApiCurrencyJsonParser.class
			.getSimpleName();
	
	/** The root tag of the query. */
	final static private String TAG_QUERY_ROOT = "query";
	/** The outer tag of results. */
	final static private String TAG_RESULTS = "results";
	/** The outer tag containing the array of rates.	 */
	final static private String TAG_RATE = "rate";
	
	/** The tag containing id with the rate in the form: "USDCAD".	 */
	final static private String TAG_RATE_ID = "id";
	/** The tag containing the actual exchange rate. */
	final static private String TAG_EXCHANGE_RATE = "Rate";
	
	/*
	 * JSON Form is:
	 * {
	 * 	"query":{
	 * 		"count":7,
	 * 		"created":"2014-09-29T12:12:31Z",
	 * 		"lang":"en-US",
	 * 		"results":{
	 * 			"rate":[
	 * 				{"id":"USDEUR",...,"Rate":"0.7876",...},
	 * 				{...},
	 * 				...
	 * 				]
	 * 			}
	 * 		}
	 * 	}
	 */
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// End constants
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * {@inheritDoc}
	 * @return The array of {@link ContentValues}. 
	 */
	@Override
	protected ContentValues[] parseJsonToReturnData() throws IOException {
		ContentValues[] results = new ContentValues[]{};
		mJsonReader.beginObject();
		
		while (mJsonReader.hasNext()){
			final String ROOT = mJsonReader.nextName();
			
			if (ROOT.equals(TAG_QUERY_ROOT)){ //arrived at: "query":{
				if (nextNotNull()){
					List<ContentValues> res = parseJsonAtRoot();
					results = new ContentValues[res.size()];
					res.toArray(results);
				}
			} else {
				mJsonReader.skipValue();
			}
		}
		
		mJsonReader.endObject();
		return results;
	} 
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Helper methods
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** Parses the root of JSON to the data.
	 * @throws IOException Re-thrown exception
	 * @return The content values populated using the keys in 
	 * {@link CurrencyConverterContract.ExchangeRateEntry}
	 */
	private List<ContentValues> parseJsonAtRoot() throws IOException {
		List<ContentValues> results = new ArrayList<ContentValues>();
		mJsonReader.beginObject();
		
		while (mJsonReader.hasNext()) {
			final String RESULTS = mJsonReader.nextName();
			
			if (RESULTS.equals(TAG_RESULTS)) { //arrived at: "results":{
				if (nextNotNull()) {
					parseJsonAtRate(results);
				}
			} else { //we are not interesting in other values
				mJsonReader.skipValue();
			}
		}
		
		mJsonReader.endObject();
		return results;		
	}
	
	/** Parses the Json stream starting at {@link #TAG_RATE}.
	 * @param results The results to return
	 * @throws IOException Re-thrown
	 */
	private void parseJsonAtRate(List<ContentValues> results)
			throws IOException {
		mJsonReader.beginObject();
		
		while (mJsonReader.hasNext()){
			final String RATES = mJsonReader.nextName();
			
			if (RATES.equals(TAG_RATE)){ //arrived at: "rate":[
				if (nextNotNull()){
					mJsonReader.beginArray();					
					while (mJsonReader.hasNext()){
						
						CodeRatePair ratePair = null;
						try {
							ratePair = parseCodeRatePair();
							results.add(ratePair.toContentValues());
							results.add(ratePair.toReverseContentValues());
							
						} catch (IllegalArgumentException badPair ) {
							Log.w(LOGTAG, "Failed to parse pair: " + badPair);
						}
					}					
					mJsonReader.endArray();
				}
			} else {
				mJsonReader.skipValue();
			}
		}
		
		mJsonReader.endObject();
	}

	/** Parses the rate pair from the json stream.
	 * @return The rate pair if successfully parsed or <code>null</code>
	 * @throws IOException Re-thrown
	 * @throws NumberFormatException Re-thrown during parsing
	 * @throws IllegalArgumentException Re-thrown
	 */
	private CodeRatePair parseCodeRatePair() throws IOException, 
		IllegalArgumentException, NumberFormatException {
		mJsonReader.beginObject();
		//arrived at: {"id":"USDEUR",...,"Rate":"0.7876",...},
		
		String idText = "";
		double rate = 0.0d;
		while (mJsonReader.hasNext()){
			final String TAG = mJsonReader.nextName();
			if (TAG.equals(TAG_RATE_ID) && nextNotNull()){
				idText = mJsonReader.nextString();
					
			} else if (TAG.equals(TAG_EXCHANGE_RATE)  && nextNotNull()){
				try {
					rate = mJsonReader.nextDouble();
				} catch (NumberFormatException e){
					Log.w(LOGTAG, 
							"Rate was not parsed correctly for currency \""+
									idText+"\"; skipping. ");
					throw e;
				}
			} else {
				mJsonReader.skipValue();
			}
		}
		
		mJsonReader.endObject();
				
		return new CodeRatePair(idText, rate); 
	}
	
}
