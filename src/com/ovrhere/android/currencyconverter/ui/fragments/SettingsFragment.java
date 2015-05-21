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
package com.ovrhere.android.currencyconverter.ui.fragments;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.support.v4.preference.PreferenceFragment;
import android.util.Log;

import com.ovrhere.android.currencyconverter.R;
import com.ovrhere.android.currencyconverter.prefs.PreferenceUtils;
import com.ovrhere.android.currencyconverter.ui.fragments.dialogs.ConfirmationDialogFragment;
import com.ovrhere.android.currencyconverter.utils.MarketIntentUtil;
import com.ovrhere.android.currencyconverter.utils.ToastManager;

/** 
 * Fragment for displaying settings and version number.
 * 
 * Requires:
 * <a href="https://github.com/kolavar/android-support-v4-preferencefragment" 
 * target="_blank">android-support-v4-preferencefragment</a>
 * @author Jason J.
 * @version 0.2.0-20140929
 */
public class SettingsFragment extends PreferenceFragment 
 implements OnPreferenceClickListener, OnPreferenceChangeListener {
	/** Class name for debugging purposes. */
	final static private String CLASS_NAME = SettingsFragment.class
			.getSimpleName();
	/** Basic debugging bool. */
	final static private boolean DEBUG = true;
	/** The developer name for launching intent. */
	final static private String DEVELOPER_NAME = "iamovrhere";
	
	/** Clear all request. */
	final static private int REQUEST_CLEAR_ALL = 0x101;
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// End constants
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** The toast manager for this frag. */
	private ToastManager tm = null;
	/** The shared preferences to get. */
	private SharedPreferences prefs = null;
	
	@Override
	public void onCreate(Bundle paramBundle) {
		super.onCreate(paramBundle);
		getPreferenceManager().setSharedPreferencesName(
				getString(R.string.preferenceutil_PREFERENCE_FILE_KEY));
		
		prefs = getPreferenceManager().getSharedPreferences();
		tm = new ToastManager(getActivity());
		
		refreshPreferences();
	}
	
	
	@Override
	public void onDestroyView() {	
		super.onDestroyView();
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		switch (requestCode) {
		case REQUEST_CLEAR_ALL:
			if (resultCode == Activity.RESULT_OK){
				resetSettings();
			}
			break;

		default:
			break;
		}
	}

	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Initialization helpers
	////////////////////////////////////////////////////////////////////////////////////////////////
	

	/** Sets preference to null before attaching settings. */
	private void refreshPreferences(){
		setPreferenceScreen(null);
		addPreferencesFromResource(R.xml.settings);
		initUpdateInterval();
		initNonSettings();
	}
	
	
	/** Initializes the update interval, including label. */
	private void initUpdateInterval(){
		final String prefKey = getString(
				R.string.currConv_pref_KEY_UPDATE_CURRENCY_INTERVAL);
		final String settingKey = 
				getString(R.string.currConv_settings_KEY_UPDATE_INTERVAL);
		
		Preference updateInt = getPreferenceManager().findPreference(settingKey);
		updateInt.setOnPreferenceChangeListener(this);
		
		int value = prefs.getInt(prefKey, 0);
		findAndSetUpdateSummary(value, (ListPreference) updateInt);
	}

	
	/** Initializes software version & reset settings. */
	private void initNonSettings() {
		getPreferenceManager().findPreference(
				getString(R.string.currConv_settings_KEY_CLEAR_SETTINGS)
				).setOnPreferenceClickListener(this);
		
		getPreferenceManager().findPreference(
				getString(R.string.currConv_settings_KEY_MORE_APPS)
				).setOnPreferenceClickListener(this);
		
		Preference  softwareVersion =
				getPreferenceManager().findPreference(
						getString(R.string.currConv_settings_KEY_SOFTWARE_VERSION)
						);
		softwareVersion.setSummary(softwareVersionName());
	}
	
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Misc. Helper methods
	////////////////////////////////////////////////////////////////////////////////////////////////
	

	/** Finds and sets the update summary. Additionally sets default
	 * value based on interval 
	 * @param interval The interval to show 
	 * @param updateList The preference to update the summary */ 
	private void findAndSetUpdateSummary(final int interval, 
			ListPreference updateList) {
		CharSequence[] labels = updateList.getEntries(); 
		CharSequence[] values = updateList.getEntryValues();
		
		if (labels.length != values.length){
			Log.w(CLASS_NAME, "Labels and values mismatch!");
			//if there is a mismatch, we know nothing.
			return;
		}
		final int SIZE = labels.length;
		for (int index = 0; index < SIZE; index++) {
			if (values[index].equals(String.valueOf(interval))){
				//same value, so label it as such.
				updateList.setSummary(labels[index]);
				updateList.setValueIndex(index);
				return;
			}
		}
	}
	
	/** Resets all settings then toasts it. */
	private void resetSettings() {
		PreferenceUtils.getPreferences(getActivity())
						.edit()
						.clear()
						.commit(); //empty all settings
		PreferenceUtils.setToDefault(getActivity()); //reset
		refreshPreferences();
		tm.toastLong(getString(R.string.currConv_toast_clearedSettings));
	}
	
	/** Creates and shows dialog. */
	private void showClearSettingsDialog(){
		new ConfirmationDialogFragment.Builder()
			.setTargetFragment(this, REQUEST_CLEAR_ALL)
			.setTitle(R.string.currConv_settings_clearSettings_title)
			.setMessage(R.string.currConv_settings_clearSettings_confirmMsg)
			.setPositive(android.R.string.ok)
			.setNegative(android.R.string.cancel)
			.create()
			.show(getFragmentManager(), 
				ConfirmationDialogFragment.class.getName()
					+SettingsFragment.class.getSimpleName());
	}
	
	/** Launches more apps by this developer. */
	private void launchMoreApps(){
		MarketIntentUtil.launchDeveloper(getActivity(), DEVELOPER_NAME); 
	}
	
	/** Returns the software build version. */
	private String softwareVersionName(){
		try {
			return getActivity()	.getPackageManager()
									.getPackageInfo(getActivity()
									.getPackageName(), 0)
									.versionName;
		} catch (NameNotFoundException e) {
			if (DEBUG){
				e.printStackTrace();
			}
			return "Unavailable"; //show never happen
		}
	}
	
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Implemented listeners
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	@Override
	public boolean onPreferenceClick(Preference preference) {
		String clearSettings =  
				getString(R.string.currConv_settings_KEY_CLEAR_SETTINGS);
		String moreApps =
				getString(R.string.currConv_settings_KEY_MORE_APPS);
		
		if (clearSettings.equals(preference.getKey())){
			showClearSettingsDialog();
			return true;
		} else if (moreApps.equals(preference.getKey())){
			launchMoreApps();
			return true;
		}
		return false;
	}
	
	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		String updateKey = 
				getString(R.string.currConv_settings_KEY_UPDATE_INTERVAL);
		if (updateKey.equals(preference.getKey())){
			int value = Integer.parseInt((String)newValue);
			prefs.edit()
				.putInt(
						getString(R.string.currConv_pref_KEY_UPDATE_CURRENCY_INTERVAL), 
						value)
				.commit();
			findAndSetUpdateSummary(value, (ListPreference) preference);
			return false;
		}
		return true;
	}
	
}
