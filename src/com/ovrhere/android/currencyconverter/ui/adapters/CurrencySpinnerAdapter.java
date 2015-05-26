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
package com.ovrhere.android.currencyconverter.ui.adapters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.ovrhere.android.currencyconverter.R;
import com.ovrhere.android.currencyconverter.model.CurrencyResourceMap;

/** The spinner adapter for currencies.
 * Use cases:
 * <ul>
 * <li>In spinner only the currency code is shown</li>
 * <li>During selection popup: Full currency name, currency code & Flags are shown.</li>
 * </ul>
 * @author Jason J.
 * @version 0.1.0-20150525
 */
public class CurrencySpinnerAdapter extends ArrayAdapter<String> {
	
	/** The text for select all. */ 
	final private String mSelectAllText;
	
	/** The inflater used to inflate the row layouts. */
	final private LayoutInflater mInflater;
	/** The selected resource. */
	final private int mLayoutResource;
	
	/** List of possible currencies */
	final private List<String> mCurrencyList = new ArrayList<String>();

	
	/** Convenience method; calls {@link #CurrencySpinnerAdapter(Context, int, String)} 
	 * with string set to <code>null</code>.
	 * @param context The current context.
	 * @param resource A layout resource containing the id:
	 * <code>android.R.id.text1</code>
	 * currencyCodes The current data to populate the adapter with.
	 */	
	public CurrencySpinnerAdapter(Context context, int resource, String[] currencyCodes){
		this(context, resource, currencyCodes, null);			
	}
	
	/** Initializes the adapter for use.
	 * @param context The current context.
	 * @param resource A layout resource containing the id:
	 * <code>android.R.id.text1</code>
	 * @param selectAllString The string to set the selectAllOption to; <code>null</code>
	 * skips omits it and removes the option.
	 */	
	public CurrencySpinnerAdapter(Context context, int resource, String[] currencyCodes,
			@Nullable String selectAllString){
		super(context, resource);
		this.mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.mLayoutResource = resource;
		this.mSelectAllText = selectAllString;	
		List<String> list = Arrays.asList(currencyCodes);
		this.mCurrencyList.addAll(list);
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Mutators
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** @param currencyCodes The current data to populate the adapter with. */
	public void updateCurrencyData(String[] currencyCodes){
		this.mCurrencyList.clear();		
		List<String> list = Arrays.asList(currencyCodes);
		this.mCurrencyList.addAll(list);
		notifyDataSetChanged();
	}
	
	
	/** @param currencyCodes The current data to populate the adapter with. */
	public void updateCurrencyData(List<String> currencyCodes){
		this.mCurrencyList.clear();
		this.mCurrencyList.addAll(currencyCodes);
		notifyDataSetChanged();
	}
	
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Start methods
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	@Override
	public int getItemViewType(int position) {
		return 1;
	}
	
	@Override
	public long getItemId(int position) {
		return position;
	}
	
	/**
     * {@inheritDoc}
     * @return The data at this position, note: can be <code>null</code> if 
     * there is a selection all option.
     */
	@Override
	public String getItem(int position) {
		if (selectAllOptionExists()){
			position -= 1;
		}
		if (position < 0){
			return null;
		}
		return mCurrencyList.get(position);
	}
	
	@Override
	public int getCount() {
		if (selectAllOptionExists()){
			return mCurrencyList.size() + 1;
		}
		return mCurrencyList.size();
	}

	
	
	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		return getPopulatedView(position, convertView, parent, true);
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		return getPopulatedView(position, convertView, parent, false);
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Helper methods
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** 
	 * Simple method for readability.
	 * @return <code>true</code> when #mSelectAllText != <code>null</code>
	 */
	private boolean selectAllOptionExists() {
		return mSelectAllText != null;
	}

	/** Populates view(s) from the data set. 
	 * @param position The postion of the item being shown
	 * @param convertView The convertView
	 * @param parent The parent to attach to
	 * @param verbose if <code>true</code> use the long form, <code>false</code>
	 * to use currency code only.
	 * @return The view corresponding to the given position.
	 */
	private View getPopulatedView(int position, View convertView, ViewGroup parent, 
			boolean verbose) {
		
		if(selectAllOptionExists()){
			position -= 1; //decrement the additional item.			
		}
		
		Holder holder = null;
		if (convertView == null){
			convertView = mInflater.inflate(mLayoutResource, parent, false);
			holder =  new Holder(convertView);						
			convertView.setTag(holder); 
		} else { //if created, getTag
			holder = (Holder) convertView.getTag();
		}
		
		int flagRes = 0;
		int drawablePadding = 0;
		
		if (position < 0) {
			holder.text.setText(mSelectAllText);
			
		} else {
			final String currencyCode = mCurrencyList.get(position);
			final Resources res = convertView.getResources();
			
			CurrencyResourceMap map  = CurrencyResourceMap.valueOf(currencyCode);
			String currencyName = res.getString(map.mNameResId);
			
			if (verbose){
				holder.text.setText(currencyName);
				flagRes = map.mFlagResId;
				drawablePadding = res.getDimensionPixelSize(
									R.dimen.currConv_main_spinner_imgPadding);
				
			} else {
				holder.text.setText(currencyCode);
				holder.text.setContentDescription(currencyName);
			}			
		}		
		
		
		holder.text.setCompoundDrawablePadding(drawablePadding);
		holder.text.setCompoundDrawablesWithIntrinsicBounds(flagRes, 0, 0, 0);
		return convertView;
	}	
	
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Holder pattern
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** Internal holder for optimizing adapter. */
	private class Holder {
		final public TextView text;
		public Holder(View convertView) {
			this.text = (TextView) convertView.findViewById(android.R.id.text1);
		}
	}
	
}
