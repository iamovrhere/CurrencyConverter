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
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceManager;
import android.support.v4.preference.PreferenceFragment;

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
 * @version 0.3.0-20150526
 */
public class SettingsFragment extends PreferenceFragment implements 
	OnPreferenceClickListener, OnPreferenceChangeListener {
	
	/* Class name for debugging purposes. */
	//final static private String CLASS_NAME = SettingsFragment.class .getSimpleName();
	
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
	
	@Override
	public void onCreate(Bundle paramBundle) {
		super.onCreate(paramBundle);
				
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
		final String prefKey = getString(R.string.currConv_pref_KEY_UPDATE_CURRENCY_INTERVAL);
		
		Preference updateInt = getPreferenceManager().findPreference(prefKey);		
		
		bindPreferenceSummaryToValue((ListPreference) updateInt, this);
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
	
	
	/** Resets all settings then toasts it. */
	private void resetSettings() {
		PreferenceUtils.resetToDefault(getActivity()); //reset
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
	
    /**
     * Binds a preference's summary to its value, such that  when the
     * preference's value is changed, its summary is updated to reflect the value.
     */
    private static void bindPreferenceSummaryToValue(Preference preference, 
    		OnPreferenceChangeListener listener) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(listener);

        // Trigger the listener immediately with the preference's
        // current value.
        listener.onPreferenceChange(preference,
                PreferenceManager.getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
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
		String stringValue = newValue.toString();

        if (preference instanceof ListPreference) {
            // For list preferences, look up the correct display value in
            // the preference's 'entries' list.
            ListPreference listPreference = (ListPreference) preference;
            
            //note: this line may not work for integer-arrays
            int index = listPreference.findIndexOfValue(stringValue); 

            // Set the summary to reflect the new value.
            preference.setSummary(index >= 0 ? listPreference.getEntries()[index] : null);

        } else {
            // For all other preferences, set the summary to the value's
            // simple string representation.
            preference.setSummary(stringValue);
        }
        return true;
	}
	
}
