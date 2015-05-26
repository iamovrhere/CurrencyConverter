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
package com.ovrhere.android.currencyconverter.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import com.ovrhere.android.currencyconverter.R;
import com.ovrhere.android.currencyconverter.prefs.PreferenceUtils;

/** The main activity.
 * @author Jason J.
 * @version 0.2.0-20150526
 */
public class MainActivity extends ActionBarActivity  {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		if (PreferenceUtils.isFirstRun(this)) { //TODO move to activity
			PreferenceUtils.resetToDefault(this);
		}
		
		if (getSupportActionBar() != null){ //make actionbar flat
			getSupportActionBar().setElevation(0.0f);
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);		
		return true;
	}
	

	@SuppressLint("NewApi")
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			Intent settings = new Intent(this, SettingsActivity.class);
			startActivity(settings);
			
			//TODO complete animations
			/*if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
				startActivity(settings, //only support JellyBean and up
		              ActivityOptionsCompat.makeSceneTransitionAnimation(this).toBundle());
			} else {
				startActivity(settings);
			}*/
			 
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
