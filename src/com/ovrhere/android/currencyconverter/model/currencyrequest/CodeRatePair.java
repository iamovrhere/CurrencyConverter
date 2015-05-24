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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;

import android.content.ContentValues;
import android.util.Log;

import com.ovrhere.android.currencyconverter.model.data.CurrencyConverterContract;
import com.ovrhere.android.currencyconverter.model.data.CurrencyConverterContract.ExchangeRateEntry;

/** Simple internal dao for easy management. 
 * @version 0.4.0-20150523 */
class CodeRatePair {
	/** Always rounded to 6 decimals; RoundingMode HALF_UP */
	final public double rate;
	/** Always uppercase. */
	final public String destCode;
	/** Always uppercase. */
	final public String srcCode;
	
	/**
	 * Creates a simple code rate pair based on the raw ids and rates.
	 * @param idText The id text expected in the form "USDCAD" where
	 * "USD" is the source code, and "CAD" is the destination code.
	 * @param rate The exchange rate from srcCode to destCode.
	 * @throws IllegalArgumentException
	 */
	public CodeRatePair(String idText, double rate) throws IllegalArgumentException {
		if (idText.length() != 6) {
			Log.w("CodeRatePair", 
					"Unable to extract currency code from id: "+idText);
			throw new IllegalArgumentException("Cannot parse this rate pair");
		}
		this.destCode = idText.substring(3).toUpperCase(Locale.US);
		this.srcCode = idText.substring(0,3).toUpperCase(Locale.US);
		this.rate = roundTo6(rate);
	}
	
	/**
	 * @return  The content values populated using the keys in 
	 * {@link CurrencyConverterContract.ExchangeRateEntry}
	 * for the src -> dest pair. Such that "USDCAD" 0.81 gives
	 * "USD" -> "CAD" = 0.8100
	 */
	public ContentValues toContentValues() {
		ContentValues cvPair = new ContentValues();
		cvPair.put(ExchangeRateEntry.COLUMN_SOURCE_CURRENCY_CODE, srcCode);
		cvPair.put(ExchangeRateEntry.COLUMN_DEST_CURRENCY_CODE, destCode);							
		cvPair.put(ExchangeRateEntry.COLUMN_EXCHANGE_RATE, rate);
		return cvPair;
	}
	
	/**
	 * @return  The content values populated using the keys in 
	 * {@link CurrencyConverterContract.ExchangeRateEntry}
	 * for the dest -> src pair. Such that "USDCAD" 0.81 gives
	 * "CAD" -> "USD" = 1.2300
	 */
	public ContentValues toReverseContentValues() {
		ContentValues cvPair = new ContentValues();
		cvPair.put(ExchangeRateEntry.COLUMN_SOURCE_CURRENCY_CODE, destCode);
		cvPair.put(ExchangeRateEntry.COLUMN_DEST_CURRENCY_CODE, srcCode);							
		cvPair.put(ExchangeRateEntry.COLUMN_EXCHANGE_RATE, roundTo6(1.0d/rate));
		return cvPair;
	}
		
	@Override
	public String toString() {
		return super.toString()+
				"[ sourceCode:"+srcCode+",destCode:"+destCode+
				" rate:"+rate+"]";
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Utility methods
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** Neatly round input to 6 decimal places. */
	private static double roundTo6(double num) {
		return new BigDecimal(num).setScale(6, RoundingMode.HALF_UP).doubleValue();
	}
	
}
