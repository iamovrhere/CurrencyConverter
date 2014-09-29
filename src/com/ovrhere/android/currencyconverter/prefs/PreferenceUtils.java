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
package com.ovrhere.android.currencyconverter.prefs;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;

import com.ovrhere.android.currencyconverter.R;

/**
 * Preference Utility for handling the preferences and the preference container.
 * Has ability to set defaults.
 * @author Jason J.
 * @version 0.4.0-20140929
 */
public class PreferenceUtils {
	/* The class name. */
	//final static private String CLASS_NAME = PreferenceUtils.class.getSimpleName();	
	/** The key for the first run/preferences set pref. */
	final static protected String KEY_PREFERENCES_SET = "com.ovrhere.currConv.KEY_FIRST_RUN";
	/** The pref value for the first run/preferences set . 
	 * @see {@link #KEY_PREFERENCES_SET} */
	final static protected boolean VALUE_PREFERENCES_SET	 = true;
	
	/** Used to determine if the preferences have been set to default or if this
	 * the first run.
	 * @param context The current context.
	 * @return <code>true</code> if the first run, <code>false</code> otherwise.
	 */
	static public boolean isFirstRun(Context context){
		SharedPreferences prefs = getPreferences(context);
		//if the default value not set, then true.
		return (prefs.getBoolean(KEY_PREFERENCES_SET, !VALUE_PREFERENCES_SET) 
				== !VALUE_PREFERENCES_SET);
	}
	
	/** Returns the {@link SharedPreferences} file  using private mode. 
	 * @param context The current context to be used. */
	static public SharedPreferences getPreferences(Context context){
		/* This is safe as SharedPreferences is a shared instance for the application
		 * and thus will not leak.		 */
		context = context.getApplicationContext();
		return context.getSharedPreferences(
				context.getResources().getString(R.string.com_ovrhere_currConv_PREFERENCE_FILE_KEY), 
				Context.MODE_PRIVATE); 
	}
	
	/** Sets the application's preferences using the default values. 
	 * @param context The current context to be used. 
	 * @see res/values/preferences_info.xml */
	static public void setToDefault(Context context){
		SharedPreferences.Editor prefs = getPreferences(context).edit();
		Resources r = context.getResources();		
		_setDefaults(r, prefs);		
		prefs.commit();
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Utility functions
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** Sets defaults. Does not commit.
	 * @param r The {@link Resources} manager to use getting strings from 
	 * res/values/preferences_info.xml
	 * @param prefEdit The {@link SharedPreferences} editor to use to commit. */
	static private void _setDefaults(Resources r, SharedPreferences.Editor prefEdit){
		//Thought: Consider using parallel arrays in res to respect open-close?
		prefEdit.putInt(
				r.getString(R.string.com_ovrhere_currConv_pref_KEY_SOURCE_CURRENCY_INDEX),
			r.getInteger(R.integer.com_ovrhere_currConv_pref_DEF_VALUE_SOURCE_CURRENCY_INDEX)
		);
		prefEdit.putInt(
				r.getString(R.string.com_ovrhere_currConv_pref_KEY_DEST_CURRENCY_INDEX),
			r.getInteger(R.integer.com_ovrhere_currConv_pref_DEF_VALUE_DEST_CURRENCY_INDEX)
		);
		prefEdit.putInt(
				r.getString(R.string.com_ovrhere_currConv_pref_KEY_UPDATE_CURRENCY_INTERVAL),
			r.getInteger(R.integer.com_ovrhere_currConv_pref_DEF_VALUE_UPDATE_CURRENCY_INTERVAL)
		);
		
		prefEdit.putBoolean(
				r.getString(R.string.com_ovrhere_currConv_pref_KEY_USE_JSON_REQUEST),
			r.getBoolean(R.bool.com_ovrhere_currConv_pref_DEF_VALUE_USE_JSON_REQUEST)
		);
		
		//first run has completed.
		prefEdit.putBoolean(KEY_PREFERENCES_SET, VALUE_PREFERENCES_SET);
	}		
}
