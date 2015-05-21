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
package com.ovrhere.android.currencyconverter.utils;

import java.text.NumberFormat;
import java.util.Currency;

import com.ovrhere.android.currencyconverter.oldmodel.dao.CurrencyData;

/**
 * Performs calculations & formatting regarding currency. 
 * @author Jason J.
 * @version 0.4.0-20140908
 */
public class CurrencyCalculator {
	/** The minimal format precision. */
	final static private int FORMAT_MIN_PRECISION = 4;	
	/** The detailed format precision. */
	final static private int FORMAT_DETAILED_PRECISION = 6;

	/** Convenience function for {@link #format(CurrencyData, double, boolean)}.
	 * Same as calling with <code>detailed</code> <code>false</code>.
	 * @param currencyData The currency data to get decimal data from.
	 * @param amount The amount to format
	 * @return The formatted currency string.
	 */
	static public String format(CurrencyData currencyData, 
			double amount){
		return format(currencyData, amount, false);
	}
	
	/** Formats currents according the currency data
	 * @param currencyData The currency data to get decimal data from.
	 * @param amount The amount to format
	 * @param detailed <code>true</code> to give more decimal points, 
	 * <code>false</code> to give the default currency digits. 
	 * @return The formatted currency string.	 */
	static public String format(CurrencyData currencyData, 
			double amount, boolean detailed){
		NumberFormat numFormat = NumberFormat.getInstance();
		Currency currency = currencyData.getCurrency();
		numFormat.setCurrency(currency);
		int fracDigits = 
				currency.getDefaultFractionDigits() > FORMAT_MIN_PRECISION ?
				currency.getDefaultFractionDigits() : FORMAT_MIN_PRECISION;
		if (detailed){
			//as most currencies supported will have at least 2 decimals, so add two more
			fracDigits = FORMAT_DETAILED_PRECISION;
		}
		numFormat.setMinimumFractionDigits(fracDigits);
		numFormat.setMaximumFractionDigits(fracDigits);
		
		return numFormat.format(amount);
	}
	
	/**
	 * Converts an amount of currency <code>source</code> 
	 * from currency <code>source</code> to currency <code>dest</code>
	 * @param source The currency to convert from (must have valid positive rate).
	 * @param dest The currency to convert to (must have valid positive rate)
	 * @param amount The amount of currency <code>source</code> to convert
	 * @return The amount of currency <code>dest</code>.
	 * @throws IllegalArgumentException If one of the currencies does not contain
	 * a valid rate.
	 */
	static public double convert(CurrencyData source, CurrencyData dest, 
			double amount){
		String sourceCurrency = source.getCurrencyCode();
		String destCurrency = dest.getCurrencyCode();
		double fromToRate = source.getRate(destCurrency); //try the best choice
		if (dest.getRate(sourceCurrency) > 0){
			fromToRate = 1.0d/dest.getRate(sourceCurrency); //try the next best thing
		} 
		if (fromToRate <= 0) { //we give up.
			throw new IllegalArgumentException(
					"Currency Data must contain valid rates.");
		}
		
		return fromToRate * amount;		
	}
	
	/**
	 * Converts an amount of currency <code>source</code> 
	 * from currency <code>source</code> to currency <code>dest</code>
	 * @param sourceUSD The uSD rate for source currency (must be positive)
	 * @param dest The currency to convert to (must have valid rate)
	 * @param amount The amount of currency <code>source</code> to convert
	 * @return The amount of currency <code>dest</code>.
	 */
	@Deprecated
	static public double convert(double sourceUSD, 
			CurrencyData dest, double amount){
		if (sourceUSD <= 0 || dest.getRateFromUSD() <= 0){
			throw new IllegalArgumentException(
					"Rates must be valid postive values");
		}
		double fromToRate = dest.getRateFromUSD()/sourceUSD;
		return fromToRate * amount;
	}
	
}
