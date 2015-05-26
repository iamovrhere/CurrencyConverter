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


/**
 * Performs calculations & formatting regarding currency. 
 * @author Jason J.
 * @version 0.4.0-20150524
 */
public class CurrencyCalculator {
	/** The minimal format precision. */
	final static private int FORMAT_MIN_PRECISION = 4;	
	/** The detailed format precision. */
	final static private int FORMAT_DETAILED_PRECISION = 6;

	/** Convenience function for {@link #format(CurrencyData, double, boolean)}.
	 * Same as calling with <code>detailed</code> <code>false</code>.
	 * @param currency The currency type we are formating
	 * @param amount The amount to format
	 * @return The formatted currency string.
	 */
	static public String format(Currency currency, 
			double amount){
		return format(currency, amount, false);
	}
	
	/** 
	 * Formats money amounts according to locale.
	 * @param currency The currency type we are formating
	 * @param amount The amount to format
	 * @param detailed <code>true</code> to give more decimal points, 
	 * <code>false</code> to give the default currency digits. 
	 * @return The formatted currency string.	 
	 * */
	static public String format(Currency currency, double amount, boolean detailed){
		final NumberFormat numFormat = NumberFormat.getInstance();
		
		numFormat.setCurrency(currency);
		
		int fracDigits =  currency.getDefaultFractionDigits() > FORMAT_MIN_PRECISION ?
							currency.getDefaultFractionDigits() : FORMAT_MIN_PRECISION;
		if (detailed){
			// most currencies supported will have at least 2 decimals, 
			// so we can say our calculation should be within 6 decimals accurate enough.
			fracDigits = FORMAT_DETAILED_PRECISION;
		}
		numFormat.setMinimumFractionDigits(fracDigits);
		numFormat.setMaximumFractionDigits(fracDigits);
		
		return numFormat.format(amount);
	}
	
}
