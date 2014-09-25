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
package com.ovrhere.android.currencyconverter.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentManager.BackStackEntry;
import android.support.v4.app.FragmentManager.OnBackStackChangedListener;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.ovrhere.android.currencyconverter.R;
import com.ovrhere.android.currencyconverter.ui.fragments.MainFragment;
import com.ovrhere.android.currencyconverter.ui.fragments.SettingsFragment;

/** The main activity that manages it all.
 * @author Jason J.
 * @version 0.1.0-20140925
 */
public class MainActivity extends ActionBarActivity 
 implements OnBackStackChangedListener {
	/** Class name for debugging purposes. */
	final static private String CLASS_NAME = MainActivity.class.getSimpleName();
	
	/** Bundle Key. The last fragment to be attached and so re-attached. String. */ 
	final static private String KEY_LAST_FRAG = 
			CLASS_NAME + ".KEY_LAST_FRAG";
	
	/** Bundle Key. The current action bar title. String. */
	final static private String KEY_ACTIONBAR_TITLE = 
			CLASS_NAME + ".KEY_ACTIONBAR_TITLE";
	
	
	/** The settings tag. */
	final static private String TAG_SETTINGS_FRAG = 
			SettingsFragment.class.getName();
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// End contstants
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** The current fragment to attached/re-attach. */
	private String currFragTag = null;
	/** The current actionbar title. */
	private String actionBarTitle = "";
	
	/** The last built menu. */
	private Menu menu = null;
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(KEY_ACTIONBAR_TITLE, actionBarTitle);
		outState.putString(KEY_LAST_FRAG, currFragTag);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		if (savedInstanceState == null) {
			loadFragment(new MainFragment(), MainFragment.class.getName(), false);
			actionBarTitle = getString(R.string.app_name);
		} else {
			currFragTag = savedInstanceState.getString(KEY_LAST_FRAG);
			actionBarTitle = savedInstanceState.getString(KEY_ACTIONBAR_TITLE);
			reattachLastFragment();
		}
		getSupportActionBar().setTitle(actionBarTitle);
		
		getSupportFragmentManager().addOnBackStackChangedListener(this);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		getSupportFragmentManager().removeOnBackStackChangedListener(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		this.menu = menu;
		checkSettings();
		return true;
	}
	

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			loadFragment(new SettingsFragment(), 
					TAG_SETTINGS_FRAG, 
					true);
			setActionBarTitle(getString(R.string.action_settings));
			checkSettings(); //hide menu
			return true;
		}
		return super.onOptionsItemSelected(item);
	}	
	
	@Override
	public boolean onSupportNavigateUp() {
	    //This method is called when the up button is pressed. Just the pop back stack.
		setActionBarTitle(getString(R.string.app_name));
		FragmentManager fm = getSupportFragmentManager();
	    fm.popBackStack();
	    int index = fm.getBackStackEntryCount()-1;
	    if (index >= 0 ){
		    BackStackEntry entry  = getSupportFragmentManager()
		    					.getBackStackEntryAt(index);
		    currFragTag = entry.getName();
	    }
	    checkSettings(); //show menu
	    return true;
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Misc. Helpers
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** Checks to see if the current fragment is the settings fragment.
	 * If so deactivate menu, if not, re-enable it. Must be called after
	 * {@link #onCreateOptionsMenu(Menu)} 
	 */
	private void checkSettings(){
		if (menu == null){
			return;
		}
		if (TAG_SETTINGS_FRAG.equals(currFragTag)){
			menu.setGroupVisible(0, false);
		} else {
			menu.setGroupVisible(0, true);
		}
	}
	
	/** Sets actionbar title in {@link #actionBarTitle} & sets title to it. */
	private void setActionBarTitle(String title) {
		actionBarTitle = title;
		getSupportActionBar().setTitle(actionBarTitle);
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Fragment method
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** Re-attaches the last fragment. And resets back button. 
	 * If no tag is found, main is attached. */
	private void reattachLastFragment() {
		if (currFragTag == null){
			currFragTag = MainFragment.class.getName();
		}
		Fragment frag = getSupportFragmentManager()
				.findFragmentByTag(currFragTag);
		getSupportFragmentManager().beginTransaction()
				.attach(frag).commit();
		
		checkHomeButtonBack();
	}
	
	/** Returns whether or not there is a backstack. */
	private boolean canBack(){
		return getSupportFragmentManager().getBackStackEntryCount() > 0;
	}

	/** Checks to see whether to enable the action bar back. */
	private void checkHomeButtonBack() {
		boolean canback = canBack();
		getSupportActionBar().setDisplayHomeAsUpEnabled(canback);
	}
	
	/** Loads a fragment either by adding or replacing and then adds it to
	 * the #currFragTag.
	 * @param fragment The fragment to add
	 * @param tag The tag to give the fragment
	 * @param backStack <code>true</code> to allow a backstack, 
	 * <code>false</code> to clear it.
	 */
	private void loadFragment(Fragment fragment, String tag, 
			boolean backStack){
		FragmentManager fragManager = getSupportFragmentManager();
		if (backStack && currFragTag != null){
			fragManager.beginTransaction()
				.addToBackStack(currFragTag)
				.replace(R.id.container, fragment, tag)
				.commit();
		} else {
			//clear backstack
			fragManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
			fragManager.beginTransaction()
					.replace(R.id.container, fragment, tag)
					.commit();
		}
		checkHomeButtonBack();
		currFragTag = tag; //if we intent multiple fragments, we could use a stack
	}

	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Implemented listeners
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	@Override
	public void onBackStackChanged() {
		checkHomeButtonBack(); //update the back button
	}

}
