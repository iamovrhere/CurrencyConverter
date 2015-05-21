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

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Log;

import com.ovrhere.android.currencyconverter.oldmodel.dao.SimpleExchangeRates;
import com.ovrhere.android.currencyconverter.oldmodel.parsers.AbstractXmlParser;

/** The xml parser for the yahoo currency exchange api.
 * Found at: <code>http://query.yahooapis.com/v1/public/yql?...</code>
 * @author Jason J.
 * @version 0.3.1-20140929 
 * @see YahooApiCurrencyRequest */
@Deprecated
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
		if (ratePair.extractCurrencyCodes(idText) == false){
			return null;
		}
		
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
	/// CodeRatePair - Moved to package directory
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	
}
