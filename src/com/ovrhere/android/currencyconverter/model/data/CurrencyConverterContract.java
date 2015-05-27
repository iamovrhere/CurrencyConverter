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
package com.ovrhere.android.currencyconverter.model.data;

import java.util.Locale;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * The currency data contract for the currency database.
 * @author Jason J.
 * @version 1.0.0-20150527
 */
public class CurrencyConverterContract {
	
    /** Content authority for this app. */ //must be unique
    final static public String CONTENT_AUTHORITY = "com.ovrhere.android.currencyconverter";
    /** Base URI used for construction of app URIs. */
    final static public Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    /** Uri path for exchange rates. */
    final static public String PATH_EXCHANGE = "exchange_rate";
    /** Uri path for display orders. */
    final static public String PATH_ORDER = "display_order";
	
    /** Table for list of supported currencies and their display order.
     * @version 1.0.0-20150527 */
	static public class DisplayOrderEntry implements BaseColumns  {
		/** Defines content uri base for map entries. */
        final static public Uri CONTENT_URI =
        		BASE_CONTENT_URI.buildUpon().appendPath(PATH_ORDER).build();
        
        /** Defines map type for [dir]. */
        final static public String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ORDER;
        /** Defines map type for [item]. */
        final static public String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ORDER;
        
        /** The table name of map entries. */
        final public static String TABLE_NAME = "display_order";
        //note: BaseColumns supplies the _ID column.
		
		/** String. Unique key. The currency code in ISO 4217 form; 3 letters. */
		final static public String COLUMN_CURRENCY_CODE = "currency_code";
		/** Int. The optional order in which to list currencies.
		 * Values that are -1 are to be ignored.   */
		final static public String COLUMN_DEF_DISPLAY_ORDER = "def_display_order";
		
		/////////////////////////////////////////////////////////////////////////////////////////////////
		/// Utility methods
		////////////////////////////////////////////////////////////////////////////////////////////////
		
		/**
         * Builds Uri for a specific currency order entry.
         * @param id The db id.
         * @return The structured Uri for a given currency order entry.
         */
        public static Uri buildDisplayOrderUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }        
	}
    
	
	/** Table for list of currency exchanges: SOURCE -> DEST.  
	 * @version 1.0.0-20150521
	 * */
	static public class ExchangeRateEntry implements BaseColumns {	
		/** Defines content uri base for map entries. */
        final static public Uri CONTENT_URI =
        		BASE_CONTENT_URI.buildUpon().appendPath(PATH_EXCHANGE).build();
        /** Defines map type for [dir]. */
        final static public String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_EXCHANGE;
        /** Defines map type for [item]. */
        final static public String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_EXCHANGE;
        
        /** The table name of map entries. */
        final public static String TABLE_NAME = "exchange_rate";
        //note: BaseColumns supplies the _ID column.
		
		/** String. The source currency code in ISO 4217 form; 3 letters.  */
		final static public String COLUMN_SOURCE_CURRENCY_CODE = "source_code";
		/** String. Foreign key ({@link DisplayOrderEntry#COL_CURRENCY_CODE}).  
		 * The destination currency code in ISO 4217 form; 3 letters.  */
		final static public String COLUMN_DEST_CURRENCY_CODE = "dest_code";
		/**  Double. The rate from SOURCE to the DEST currency in question. */
		final static public String COLUMN_EXCHANGE_RATE = "exchange_rate";

		/////////////////////////////////////////////////////////////////////////////////////////////////
		/// Utility methods
		////////////////////////////////////////////////////////////////////////////////////////////////
		
		/**
         * Builds Uri for a specific exchange rate entry.
         * @param id The exchange db _ID.
         * @return The structured Uri for a given exchange rate entry.
         */
        public static Uri buildExchangeRateUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
        
        
        /**
         * Builds Uri for exchange rates from a particular source currency.
         * Expects join with {@link DisplayOrderEntry}.
         * @param sourceCurrency The starting currency.
         * @return The structured Uri for a group of entries.
         */
        public static Uri buildExchangeRateWithSourceCurrency(String sourceCurrency) {
        	return CONTENT_URI.buildUpon()
        				.appendPath(sourceCurrency.toLowerCase(Locale.US))
        				.build();
        }
        
        /**
         * Builds Uri for exchanges rate from a particular source -> destination currency.
         * @param sourceCode The starting currency.
         * @param destCode The destination currency being converted to.
         * @return The structured Uri for a given exchange rate entry.
         */
        public static Uri buildExchangeRateFromSourceToDest(String sourceCode,
        		String destCode) {
        	return CONTENT_URI.buildUpon()
        				.appendPath(sourceCode.toLowerCase(Locale.US))
        				.appendPath(destCode.toLowerCase(Locale.US))
        				.build();
        }
        
        /**
         * Parses the source currency code from uri.
         * @param uri The uri to dissect. 
         * @return The currency code, in lower case.
         * @see #buildExchangeRateFromSourceToDest(String, String)
         * @see #buildExchangeRateWithSourceCurrency(String) 
         */
        public static String getSourceCurrencyFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
		}
        
        /**
         * Parses the destination currency code from uri.
         * @param uri The uri to dissect.
         * @return The currency code, in lower case. 
         * @see #buildExchangeRateFromSourceToDest(String, String)
         */
        public static String getDestCurrencyFromUri(Uri uri) {
            return uri.getPathSegments().get(2);
		}	
		
	}
	
	
}
