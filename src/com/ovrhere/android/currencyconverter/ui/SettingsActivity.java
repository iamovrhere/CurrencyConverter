package com.ovrhere.android.currencyconverter.ui;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import com.ovrhere.android.currencyconverter.R;
import com.ovrhere.android.currencyconverter.ui.fragments.SettingsFragment;

public class SettingsActivity extends ActionBarActivity {
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
    }   
	
	@Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

    	///handle two pane here, if needed.
        
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new SettingsFragment())
                    .commit();
        }
    }
}
