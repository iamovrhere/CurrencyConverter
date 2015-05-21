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
package com.ovrhere.android.currencyconverter.oldmodel;

import java.util.HashMap;
import java.util.Locale;

import android.content.res.Resources;

import com.ovrhere.android.currencyconverter.R;
import com.ovrhere.android.currencyconverter.oldmodel.dao.CurrencyData;

/**
 * Maps flag resources onto currency data.
 * @author Jason J.
 * @version 0.1.0-20140620
 */
@Deprecated
class FlagResourceMap {
	/** Map of all currencies to their flags.
	 * Code->flag*/
	final static private HashMap<String, Integer> flagMap = 
			new HashMap<String, Integer>();;
	
	/**
	 * Sets the currency flag based upon
	 * @param res The resource to compare currency to string resources
	 * @param data The data to set the flag on. Relies on the currency code.
	 * @return <code>true</code> if there is a flag, 
	 * <code>false</code> if nothing was done.
	 */
	static public boolean setCurrencyFlagDrawable(Resources res, CurrencyData data){
		synchronized (flagMap) {
			if (flagMap.isEmpty()){
				initFlagMap(res);
			}
		}
		String currCode = data.getCurrencyCode();
		if (currCode != null){
			Integer flagRes = flagMap.get(currCode.toLowerCase(Locale.US)); 
			if (flagRes != null){
				data.setFlagResource(flagRes);
				return true;
			}
		}
		return false;
	}
	
	/** Initializes the flag resources. */
	static private void initFlagMap(Resources res){
		flagMap.put(
				res.getString(R.string.currConv_CAD_code)
					.toLowerCase(Locale.US), 
				R.drawable.ic_flag_cad);
		flagMap.put(
				res.getString(R.string.currConv_EUR_code)
					.toLowerCase(Locale.US), 
				R.drawable.ic_flag_eur);
		flagMap.put(
				res.getString(R.string.currConv_GBP_code)
					.toLowerCase(Locale.US), 
				R.drawable.ic_flag_gbp);
		flagMap.put(
				res.getString(R.string.currConv_JPY_code)
					.toLowerCase(Locale.US), 
				R.drawable.ic_flag_jpy);
		flagMap.put(
				res.getString(R.string.currConv_USD_code)
					.toLowerCase(Locale.US), 
				R.drawable.ic_flag_usd);
	}
}
