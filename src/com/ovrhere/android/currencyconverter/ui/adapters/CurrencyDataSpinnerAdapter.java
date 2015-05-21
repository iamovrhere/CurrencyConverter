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
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.ovrhere.android.currencyconverter.R;
import com.ovrhere.android.currencyconverter.dao.CurrencyData;

/** /** The spinner adapter for currencies.
 * <ul>
 * <li>In spinner only the currency code is shown</li>
 * <li>During selection popup: Full currency name, currency code & Flags are shown.</li>
 * </ul>
 * @author Jason J.
 * @version 0.2.0-20140903
 */
public class CurrencyDataSpinnerAdapter extends ArrayAdapter<CurrencyData> {
	
	/** List of possible currencies */
	private List<CurrencyData> currencyList = new ArrayList<CurrencyData>();
	/** Whether or not there is an "All" option. See {@link #setSelectAllOption(boolean)}. */
	private boolean selectAllOption = false;
	/** The text for select all. */ 
	private String allText = "Select All";
	
	/** The inflater used to inflate the row layouts. */
	private LayoutInflater inflater = null;
	/** The selected resource. */
	private int layoutResource = -1;
	
	/** Initializes the adapter for use.
	 * @param context The current context.
	 * @param resource A layout resource containing the id:
	 * <code>android.R.id.text1</code>
	 */
	public CurrencyDataSpinnerAdapter(Context context, int resource) {
		super(context, resource);
		init(context, resource, selectAllOption);
	}

	/** Initializes the adapter for use.
	 * @param context The current context.
	 * @param resource A layout resource containing the id:
	 * <code>android.R.id.text1</code>
	 * @param selectAllOption Whether or not to show a "select all" option.
	 */	
	public CurrencyDataSpinnerAdapter(Context context, int resource, 
			boolean selectAllOption){
		super(context, resource);
		init(context, resource, selectAllOption);
		
	}
	/** Simple init function.
	 * @param context The current context.
	 * @param resource A layout resource containing the id:
	 * <code>android.R.id.text1</code> 
	 * @param selectAllOption Whether or not to show a "select all" option. */
	private void init(Context context, int resource, boolean selectAllOption) {
		this.inflater = (LayoutInflater) 
				context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.layoutResource = resource;
		this.selectAllOption = selectAllOption;
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Mutators
	////////////////////////////////////////////////////////////////////////////////////////////////
	/** Sets the text that will be viewed when select all is set
	 * @param selectAllText The text to use
	 * @see #setSelectAllOption(boolean)	 */
	public void setSelectAllText(String selectAllText){
		this.allText = selectAllText;
		notifyDataSetChanged();
	}
	
	/** @param list The current data to populate the adapter with. */
	public void setCurrencyData(List<CurrencyData> list){
		this.currencyList.clear();
		this.currencyList.addAll(list);
		notifyDataSetChanged();
	}
	/** Sets whether or not there is an "All" option as the first time. 
	 * Default is <code>false</code>
	 * @param allOption <code>true</code> to set all option, 
	 * <code>false</code> to remove option.	 */
	public void setSelectAllOption(boolean allOption){
		this.selectAllOption = allOption;
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
     * {@link #setSelectAllOption(boolean)} is set <code>true</code>.
     */
	@Override
	public CurrencyData getItem(int position) {
		if (selectAllOption){
			position-=1;
		}
		if (position < 0){
			return null;
		}
		return currencyList.get(position);
	}
	
	@Override
	public int getCount() {
		if (selectAllOption){
			return currencyList.size() + 1;
		}
		return currencyList.size();
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

	/** Populates view(s) from the data set. 
	 * @param position The postion of the item being shown
	 * @param convertView The convertView
	 * @param parent The parent to attach to
	 * @param verbose if <code>true</code> use the long form, <code>false</code>
	 * to use currency code only.
	 * @return The view corresponding to the given position.
	 */
	private View getPopulatedView(int position, View convertView,
			ViewGroup parent, boolean verbose) {
		if(selectAllOption){
			position-=1; //decrement the additional item.			
		}
		Holder holder = null;
		
		if (convertView == null){
			convertView = inflater.inflate(layoutResource, parent, false);
			holder =  new Holder();
			holder.text = (TextView) convertView.findViewById(android.R.id.text1);			
			convertView.setTag(holder); 
		} else { //if created, getTag
			holder = (Holder) convertView.getTag();
		}
		
		String text = "";
		int flagRes = 0;
		int drawablePadding = 0;
		if (position < 0){
			text = allText;
		} else {
			CurrencyData data = currencyList.get(position);
			if (verbose){
				text =  data.getCurrencyName() + " ("+data.getCurrencyCode() +")";
				if (data.getFlagResource() > flagRes){
					flagRes = data.getFlagResource();
					drawablePadding = 
							convertView.getResources()
								.getDimensionPixelSize(
										R.dimen.currConv_main_spinner_imgPadding);
				}
			} else {
				text =  data.getCurrencyCode();
			}			
		}		
		
		holder.text.setText(text);
		holder.text.setCompoundDrawablePadding(drawablePadding);
		holder.text.setCompoundDrawablesWithIntrinsicBounds(flagRes, 0, 0, 0);
		return convertView;
	}	
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Holder pattern
	////////////////////////////////////////////////////////////////////////////////////////////////
	/** Internal holder for optimizing adapter. */
	private class Holder {
		public TextView text = null;
	}
	
}
