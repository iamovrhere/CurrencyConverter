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

import android.app.Activity;
import android.support.v4.app.FragmentActivity;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;

/** Simple keyboard utility for common keyboard methods.
 * @author Jason J.
 * @version 0.1.0-20140911
 */
public class KeyboardUtil {
	/** Class name for debugging purposes. */
	final static public String CLASS_NAME = KeyboardUtil.class.getSimpleName();
	
	/** Hides the keyboard. Suggested with use with either:
	 * <ul><li> {@link OnFocusChangeListener}</li>
	 * <li> {@link OnTouchListener}</li></ul>
	 * Such that: 
	 * <code>
	 * <br/>rootView.setOnTouchListener(
	 * <br/>@ Override
	 * <br/>public boolean onTouch(View v, MotionEvent event) {
	 * <br/>&nbsp;&nbsp;&nbsp; KeyboardUtil.hideSoftKeyboard(activity);
	 * <br/>&nbsp;&nbsp;&nbsp; return false;
	 * <br/>}
	 * <br/>});
	 * </code>
	 * @param activity The activity to get system services with.
	 */
	static public void hideSoftKeyboard(FragmentActivity activity){
		InputMethodManager inputMethodManager = (InputMethodManager)  
				activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
		if (null != activity.getCurrentFocus()){
			inputMethodManager.hideSoftInputFromWindow(
					activity.getCurrentFocus().getWindowToken(), 0);
		}
	}
}
