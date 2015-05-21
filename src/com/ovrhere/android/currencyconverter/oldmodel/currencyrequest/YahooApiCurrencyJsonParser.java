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
package com.ovrhere.android.currencyconverter.oldmodel.currencyrequest;

import java.io.IOException;

import android.util.Log;

import com.ovrhere.android.currencyconverter.oldmodel.dao.SimpleExchangeRates;
import com.ovrhere.android.currencyconverter.oldmodel.parsers.AbstractJsonParser;

/**
 * The JSON parser for the yahoo currency exchange api.
 * Found at: <code>http://query.yahooapis.com/v1/public/yql?...&format=json</code>
 * @author Jason J.
 * @version 0.1.0-20140929
 *  @see YahooApiCurrencyRequest
 */
@Deprecated
public class YahooApiCurrencyJsonParser 
	extends AbstractJsonParser<SimpleExchangeRates>{
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
	
	@Override
	protected SimpleExchangeRates parseJsonToReturnData() throws IOException {
		SimpleExchangeRates results = new SimpleExchangeRates(false);
		jsonReader.beginObject();
		
		while (jsonReader.hasNext()){
			final String ROOT = jsonReader.nextName();
			
			if (ROOT.equals(TAG_QUERY_ROOT)){ //arrived at: "query":{
				if (nextNotNull()){
					parseJsonAtRoot(results);
				}
			} else {
				jsonReader.skipValue();
			}
		}
		
		jsonReader.endObject();
		return results;
	} 
	
	/** Parses the root of JSON to the data.
	 * @param results The return data
	 * @throws IOException Re-thrown exception
	 */
	private void parseJsonAtRoot(SimpleExchangeRates results) 
			throws IOException {
		jsonReader.beginObject();
		
		while (jsonReader.hasNext()){
			final String RESULTS = jsonReader.nextName();
			
			if (RESULTS.equals(TAG_RESULTS)){ //arrived at: "results":{
				if (nextNotNull()){
					parseJsonAtRate(results);
				}
			} else {
				jsonReader.skipValue();
			}
		}
		
		jsonReader.endObject();
	}
	
	/** Parses the Json stream starting at {@link #TAG_RATE}.
	 * @param results The results to return
	 * @throws IOException Re-thrown
	 */
	private void parseJsonAtRate(SimpleExchangeRates results)
			throws IOException {
		jsonReader.beginObject();
		
		while (jsonReader.hasNext()){
			final String RATES = jsonReader.nextName();
			
			if (RATES.equals(TAG_RATE)){ //arrived at: "rate":[
				if (nextNotNull()){
					jsonReader.beginArray();					
					while (jsonReader.hasNext()){
						CodeRatePair pair = parseCodeRatePair();
						if (pair == null){
							continue; //cannot add
						} 
						results.addRate(pair.srcCode, pair.destCode, pair.rate);
					}					
					jsonReader.endArray();
				}
			} else {
				jsonReader.skipValue();
			}
		}
		
		jsonReader.endObject();
	}
	
	/** Parses the rate pair from the json stream.
	 * @return The rate pair if sucessfully parsed or <code>null</code>
	 * @throws IOException Re-thrown
	 */
	private CodeRatePair parseCodeRatePair() throws IOException {
		jsonReader.beginObject();
		//arrived at: {"id":"USDEUR",...,"Rate":"0.7876",...},
		
		CodeRatePair ratePair = new CodeRatePair();
		
		while (jsonReader.hasNext()){
			final String TAG = jsonReader.nextName();
			if (TAG.equals(TAG_RATE_ID) && nextNotNull()){
				String idText = jsonReader.nextString();
				if (!ratePair.extractCurrencyCodes(idText)){
					//if we cannot extract the pair
					ratePair = null; //we have failed
					break;
				}				
			} else if (TAG.equals(TAG_EXCHANGE_RATE)  && nextNotNull()){
				try {
					ratePair.rate = jsonReader.nextDouble();
				} catch (NumberFormatException e){
					Log.w(LOGTAG, 
							"Rate was not parsed correctly for currency \""+
							ratePair.destCode+"\"; skipping. ");
					ratePair = null; //we have failed
					break;
				}
			} else {
				jsonReader.skipValue();
			}
		}
		
		jsonReader.endObject();
		
		return ratePair;
	}
	
}
