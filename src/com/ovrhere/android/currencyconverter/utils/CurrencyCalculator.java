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
package com.ovrhere.android.currencyconverter.utils;

import com.ovrhere.android.currencyconverter.dao.CurrencyData;

/**
 * The calculations regarding currency. 
 * @author Jason J.
 * @version 0.2.0-20140902
 */
public class CurrencyCalculator {

	/**
	 * Converts an amount of currency <code>source</code> 
	 * from currency <code>source</code> to currency <code>dest</code>
	 * @param source The currency to convert from (must have valid positive rate).
	 * @param dest The currency to convert to (must have valid positive rate)
	 * @param amount The amount of currency <code>source</code> to convert
	 * @return The amount of currency <code>dest</code>.
	 */
	static public double convert(CurrencyData source, CurrencyData dest, 
			double amount){
		if (source.getRateFromUSD() <= 0 || dest.getRateFromUSD() <= 0){
			throw new IllegalArgumentException(
					"Currency Data must contain valid rates.");
		}
		double fromToRate = dest.getRateFromUSD()/source.getRateFromUSD();
		return fromToRate * amount;		
	}
	
	/**
	 * Converts an amount of currency <code>source</code> 
	 * from currency <code>source</code> to currency <code>dest</code>
	 * @param sourceRateFromUSD The uSD rate for source currency (must be positive)
	 * @param dest The currency to convert to (must have valid rate)
	 * @param amount The amount of currency <code>source</code> to convert
	 * @return The amount of currency <code>dest</code>.
	 */
	static public double convert(double sourceRateFromUSD, 
			CurrencyData dest, double amount){
		if (sourceRateFromUSD <= 0 || dest.getRateFromUSD() <= 0){
			throw new IllegalArgumentException(
					"Rates must be valid postive values");
		}
		double fromToRate = dest.getRateFromUSD()/sourceRateFromUSD;
		return fromToRate * amount;
	}
	
}
