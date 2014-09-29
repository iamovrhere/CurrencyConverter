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

import java.util.Locale;

import android.util.Log;

/** Simple internal dao for easy management. 
 * @version 0.3.0-20140929 */
class CodeRatePair {
	public double rate = 0.0d;
	public String destCode = "";
	public String srcCode = "";
	
	/** Extracts the currency code from id text, if possible. 
	 * @param idText The text to extract two strings from. Expects
	 * form "USDEUR", 
	 * where "USD" is {@link #srcCode} and "EUR" is {@link #destCode}
	 * @return <code>true</code> if successful, <code>false</code> otherwise
	 */
	public boolean extractCurrencyCodes(String idText) {
		if (idText == null || idText.length() < 6){
			Log.w("CodeRatePair", 
					"Skipping; Unable to extract currency code from id: "+idText);
			return false;
		}
		//extract the last 3 characters.
		destCode = idText.substring(3).toUpperCase(Locale.US);
		srcCode = idText.substring(0,3).toUpperCase(Locale.US);
		return true;
	}
	
	@Override
	public String toString() {
		return super.toString()+
				"[ sourceCode:"+srcCode+",destCode:"+destCode+
				" rate:"+rate+"]";
	}
}
