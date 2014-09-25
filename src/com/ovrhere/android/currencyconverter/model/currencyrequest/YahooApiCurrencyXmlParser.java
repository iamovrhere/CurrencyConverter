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

import java.io.IOException;
import java.util.Locale;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Log;

import com.ovrhere.android.currencyconverter.dao.SimpleExchangeRates;
import com.ovrhere.android.currencyconverter.model.parsers.AbstractXmlParser;

/** The xml parser for the yahoo currency exchange api.
 * Found at: <code>http://query.yahooapis.com/v1/public/yql?...</code>
 * @author Jason J.
 * @version 0.3.0-20140925 
 * @see YahooApiCurrencyRequest */
public class YahooApiCurrencyXmlParser extends AbstractXmlParser<SimpleExchangeRates> {
		/** The tag for debugging purposes. */
	final static private String LOGTAG = YahooApiCurrencyXmlParser.class
			.getSimpleName();
		
	/** The root tag of the query. */
	final static private String TAG_QUERY_ROOT = "query";
	/** The outer tag of results. */
	final static private String TAG_RESULTS = "results";
	/** The outer tag containing the full rate details with an id;
	 * <code>&lt;rate id="USDEUR"&gt;</code>	 */
	final static private String TAG_RATE_ID = "rate";
	/** The tag containing the actual exchange rate. */
	final static private String TAG_EXCHANGE_RATE = "Rate";
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// End contants
	////////////////////////////////////////////////////////////////////////////////////////////////
	/** Initializes parser.
	 * @throws XmlPullParserException if parser or factory fails to be created.*/
	public YahooApiCurrencyXmlParser() throws XmlPullParserException {
		super();
	}
	
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Override methods
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	@Override
	protected SimpleExchangeRates parseXmlToReturnData()
		throws XmlPullParserException, IOException {
		return parseXmlToExchangeRatesDao();
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Helper methods
	////////////////////////////////////////////////////////////////////////////////////////////////

	
	/** Parses the xml to a list using prepared PullParser 
	 * @throws XmlPullParserException  re-thrown from next
	 * @throws IOException  re-thrown from next */
	private SimpleExchangeRates parseXmlToExchangeRatesDao() 
			throws XmlPullParserException, IOException {
		//hashmap for ISO currency codes -> USD rates
		SimpleExchangeRates results = new SimpleExchangeRates(false);
		
		pullParser.nextTag();
		pullParser.require(XmlPullParser.START_TAG, null, TAG_QUERY_ROOT);
		pullParser.nextTag();
		pullParser.require(XmlPullParser.START_TAG, null, TAG_RESULTS);
				
		while (pullParser.next() != XmlPullParser.END_TAG) {
			if (pullParser.getEventType() != XmlPullParser.START_TAG) {
				continue;
	        }
			String name = pullParser.getName();
			if (name.equalsIgnoreCase(TAG_RATE_ID)){
				CodeRatePair ratePair = parseRateBlock();
				if (ratePair == null){
					continue;
				}
				results.addRate(
						ratePair.srcCode, 
						ratePair.destCode, 
						ratePair.rate );
			} else {
				skipTag(); //skip all other tags
			}
		}

		return results;
	}
	/** Parses the currency rate block (e.g. USDEUR) 
	 * @return The currency code + rate pair or <code>null</code> if unsuccessful.
	 * @throws XmlPullParserException re-thrown from next
	 * @throws IOException re-thrown from next
	 */
	private CodeRatePair parseRateBlock() 
			throws XmlPullParserException, IOException {
		CodeRatePair ratePair = new CodeRatePair();
		
		String idText = (String) pullParser.getAttributeValue(null, "id");
		if (idText.length() < 6){
			Log.w(LOGTAG, 
					"Skipping; Unable to extract currency code from id: "+idText);
			return null;
		}
		//extract the last 3 characters.
		ratePair.destCode = idText.substring(3).toUpperCase(Locale.US);
		ratePair.srcCode = idText.substring(0,3).toUpperCase(Locale.US);
		
		while (pullParser.next() != XmlPullParser.END_TAG) {
			if (pullParser.getEventType() != XmlPullParser.START_TAG) {
				continue;
	        }
			String name = pullParser.getName();
			if (name.equalsIgnoreCase(TAG_EXCHANGE_RATE)){
				String rateText = readText();	
				try {
					ratePair.rate = Float.parseFloat(rateText);
				} catch (NumberFormatException e){
					Log.w(LOGTAG, 
							"Rate was not parsed correctly for currency \""+
							ratePair.destCode+"\"; skipping. ");
					return null;
				}
			} else {
				skipTag(); //skip all other tags at this level.
			}
		}
		return ratePair;
	}
	
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Internal classes
	////////////////////////////////////////////////////////////////////////////////////////////////
	/** Simple internal dao for easy management. 
	 * @version 0.2.0-20140908 */
	static private class CodeRatePair {
		public float rate = 0.0f;
		public String destCode = "";
		public String srcCode = "";
		
		@Override
		public String toString() {
			return super.toString()+
					"[ sourceCode:"+srcCode+",destCode:"+destCode+
					" rate:"+rate+"]";
		}
	}
	
}
