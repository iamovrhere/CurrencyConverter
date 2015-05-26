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
package com.ovrhere.android.currencyconverter.prefs;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;

import com.ovrhere.android.currencyconverter.R;

/**
 * Preference Utility for handling the preferences .
 * Has ability to set defaults. Requires <code>preference_info.xml</code> and
 * <code>preference_defaults.xml</code>.
 * @author Jason J.
 * @version 0.6.0-2010525
 */
public class PreferenceUtils {
	/* The class name. */
	//final static private String CLASS_NAME = PreferenceUtils.class.getSimpleName();	
	/** The key for the first run/preferences set pref. */
	final static protected String KEY_PREFERENCES_SET = "com.ovrhere.currConv.KEY_FIRST_RUN";
	/** The pref value for the first run/preferences set . 
	 * @see {@link #KEY_PREFERENCES_SET} */
	final static protected boolean VALUE_PREFERENCES_SET	 = true;
	
	/** Used to determine if {@link #resetToDefault(Context)} has been called before.
	 * @param context The current context.
	 * @return <code>true</code> only on the first run, <code>false</code> otherwise.
	 */
	static public boolean isFirstRun(Context context){
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		
		//set any missing preferences if not set, ignores the rest
		boolean isFirstRun = (prefs.getBoolean(KEY_PREFERENCES_SET, !VALUE_PREFERENCES_SET) 
				== !VALUE_PREFERENCES_SET);
		
		if (isFirstRun == false) {
			SharedPreferences oldPrefs = getOldPreferences(context);
			checkAndTransferOldToNew(oldPrefs, prefs, context);
		}
				
		//if the default value not set, then true.
		return isFirstRun;
	}
	
	/** 
	 * Returns the {@link SharedPreferences} file  using private mode. 
	 * @param context The current context to be used. 
	 * @deprecated Use {@link PreferenceManager#getDefaultSharedPreferences(Context)} instead */
	@Deprecated
	static public SharedPreferences getPreferences(Context context){
		context = context.getApplicationContext();
		
		return context.getSharedPreferences(
				context.getResources().getString(R.string.preferenceutil_OLDPREFERENCE_FILE_KEY), 
				Context.MODE_PRIVATE); 
	}
	
	/** <b>Clears</b> and resets application's preferences to the default values. 
	 * @param context The current context to be used. 
	 * @see res/values/preferences_info.xml */
	static public void resetToDefault(Context context){
		SharedPreferences.Editor edit = PreferenceManager.getDefaultSharedPreferences(context)
											.edit();
		edit.clear().commit();		
		PreferenceManager.setDefaultValues(context, R.xml.preference_defaults, true);	
		
		edit.putBoolean(KEY_PREFERENCES_SET, VALUE_PREFERENCES_SET).commit();
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Helper mutators and accessors
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** 
	 * Gets the update interval as a long.
	 * @param context The context of the value.
	 * @return The update interval parsed as a long.
	 */
	public static long getUpdateInterval(Context context) {
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
		return Long.parseLong(
				pref.getString(
					context.getString(R.string.currConv_pref_KEY_UPDATE_CURRENCY_INTERVAL), "0")
			);
	}
	
	/**
	 * Gets the last update time.
	 * @param context The activity context
	 * @return Returns the time in milliseconds (since epoch) the last updat was on.
	 */
	public static long getLastUpdateTime(Context context) {
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
		final long lastUpdate = pref.getLong(
				context.getString(R.string.currConv_pref_KEY_LAST_UPDATE), 0);
		return lastUpdate;
	}
	
	/**
	 * Convenience method for {@link #setLastUpdateTime(Context, long)} 
	 * with time set to now.
	 * @param context The activity context
	 */
	public static void setLastUpdateTimeToNow(Context context) {
		final long epochTime = System.currentTimeMillis();
		setLastUpdateTime(context, epochTime);
	}
	
	/**
	 * Sets the last update time to the value of time
	 * @param context The activity context
	 * @param time the GMT time in millis since epoch
	 */
	public static void setLastUpdateTime(Context context, long time) {
		SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
		editor.putLong( context.getString(R.string.currConv_pref_KEY_LAST_UPDATE), 
						time);	
		editor.commit();
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Utility functions
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	private static SharedPreferences getOldPreferences(Context context){
		/* This is safe as SharedPreferences is a shared instance for the application
		 * and thus will not leak.		 */
		context = context.getApplicationContext();
		
		return context.getSharedPreferences(
				context.getResources().getString(R.string.preferenceutil_OLDPREFERENCE_FILE_KEY), 
				Context.MODE_PRIVATE); 
	}
	
	/**
	 * Checks if old preferences exists and migrates them to the new one, if they do.
	 * @param oldPrefs
	 * @param newPrefs
	 * @param context
	 */
	private static void checkAndTransferOldToNew(SharedPreferences oldPrefs, 
			SharedPreferences newPrefs, Context context) {
		boolean oldPrefsExist = (oldPrefs.getBoolean(KEY_PREFERENCES_SET, !VALUE_PREFERENCES_SET) == VALUE_PREFERENCES_SET);
		
		if (!oldPrefsExist) {
			return; //if the older preferences do not exist, exit.
		} //otherwise, transfer them.
		
		Resources res = context.getResources();
		SharedPreferences.Editor prefs = newPrefs.edit();
		
		prefs.putInt(
				res.getString(R.string.currConv_pref_KEY_SOURCE_CURRENCY_INDEX),
				oldPrefs.getInt(res.getString(R.string.currConv_pref_OLDKEY_SOURCE_CURRENCY_INDEX), 0)
		);
		prefs.putInt(
				res.getString(R.string.currConv_pref_KEY_DEST_CURRENCY_INDEX),
				oldPrefs.getInt(res.getString(R.string.currConv_pref_OLDKEY_DEST_CURRENCY_INDEX), 0)
		);
		
		prefs.putString(
				res.getString(R.string.currConv_pref_KEY_UPDATE_CURRENCY_INTERVAL),
				""+oldPrefs.getInt(res.getString(R.string.currConv_pref_OLDKEY_UPDATE_CURRENCY_INTERVAL), 0)
		);
		
		oldPrefs.edit().clear().commit();		
		prefs.commit();
	}

}
