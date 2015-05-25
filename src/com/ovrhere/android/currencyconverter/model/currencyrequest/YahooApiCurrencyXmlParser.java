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

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.ContentValues;
import android.util.Log;

import com.ovrhere.android.currencyconverter.model.parsers.AbstractXmlParser;

/** <p>The xml parser for the yahoo currency exchange api.
 * Found at: <code>http://query.yahooapis.com/v1/public/yql?...</code></p>
 * 
 * Note that this will create double values from compact results;
 * i.e. take USD -> CAD rates and manufacture CAD -> USD rates.
 * 
 * @author Jason J.
 * @version 0.3.1-20140929 
 * @see YahooApiCurrencyRequest */
public class YahooApiCurrencyXmlParser extends AbstractXmlParser<ContentValues[]> {
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
	
	/* XML Form is: 
	 * <query yahoo:count="7" yahoo:created="2014-09-29T12:52:03Z" yahoo:lang="en-US">
	 * 	<results>
	 * 		<rate id="USDEUR">
	 * 			<Name>USD to EUR</Name>
	 * 			<Rate>0.7872</Rate>
	 * 			...
	 * 		</rate>
	 * 		<rate id="USDJPY">
	 * 		...
	 * 		</rate>
	 * 	</results>
	 * </query>
	 */
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// End constants
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** Initializes parser.
	 * @throws XmlPullParserException if parser or factory fails to be created.*/
	public YahooApiCurrencyXmlParser() throws XmlPullParserException {
		super();
	}
	
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Override methods
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * {@inheritDoc}
	 * @return The array of {@link ContentValues}. 
	 * @throws XmlPullParserException If the XML does not parse correctly (due to poor form).
	 */
	@Override
	protected ContentValues[] parseXmlToReturnData() throws XmlPullParserException, 
		IOException {
		return parseXmlToExchangeRatesDao();
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Helper methods
	////////////////////////////////////////////////////////////////////////////////////////////////

	
	/** Parses the xml to a list using prepared PullParser 
	 * @throws XmlPullParserException  re-thrown from next
	 * @throws IOException  re-thrown from next */
	private ContentValues[] parseXmlToExchangeRatesDao() throws XmlPullParserException, 
		IOException {
		List<ContentValues> results = new ArrayList<ContentValues>();
		
		mPullParser.nextTag();
		mPullParser.require(XmlPullParser.START_TAG, null, TAG_QUERY_ROOT);
		mPullParser.nextTag();
		mPullParser.require(XmlPullParser.START_TAG, null, TAG_RESULTS);
				
		while (mPullParser.next() != XmlPullParser.END_TAG) {
			if (mPullParser.getEventType() != XmlPullParser.START_TAG) {
				continue; //skip end tags
	        }
			
			String name = mPullParser.getName();
			if (name.equalsIgnoreCase(TAG_RATE_ID)){
				CodeRatePair ratePair = parseRateBlock();
				if (ratePair == null){
					continue;
				}
				
				results.add(ratePair.toContentValues());
				results.add(ratePair.toReverseContentValues());
				
			} else {
				skipTag(); //skip all other tags
			}
		}
		
		ContentValues[] resultsArray = new ContentValues[results.size()];
		results.toArray(resultsArray);
		
		return resultsArray;
	}
	/** Parses the currency rate block (e.g. USDEUR) 
	 * @return The currency code + rate pair or <code>null</code> if unsuccessful.
	 * @throws XmlPullParserException re-thrown from next
	 * @throws IOException re-thrown from next
	 * @throws IllegalArgumentException re-thrown if there's a parsing error
	 */
	private CodeRatePair parseRateBlock() throws XmlPullParserException, IOException,
		IllegalArgumentException {
		
		String idText = (String) mPullParser.getAttributeValue(null, "id");
		double rate = 0.0d;
		
		while (mPullParser.next() != XmlPullParser.END_TAG) {
			if (mPullParser.getEventType() != XmlPullParser.START_TAG) {
				continue;
	        }
			String name = mPullParser.getName();
			if (name.equalsIgnoreCase(TAG_EXCHANGE_RATE)){
				String rateText = readText();
				
				try {
					rate = Double.parseDouble(rateText);
				} catch (NumberFormatException rateFailedToParse){
					Log.w(LOGTAG, 
							"Rate was not parsed correctly for currency \""+
									idText+"\"; skipping. ");
					throw rateFailedToParse;
				}
			} else {
				skipTag(); //skip all other tags at this level.
			}
		}
		
		return new CodeRatePair(idText, rate);
	}	
	
}
