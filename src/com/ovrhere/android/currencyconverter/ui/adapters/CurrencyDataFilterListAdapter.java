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
package com.ovrhere.android.currencyconverter.ui.adapters;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.ovrhere.android.currencyconverter.R;
import com.ovrhere.android.currencyconverter.dao.CurrencyData;
import com.ovrhere.android.currencyconverter.utils.CurrencyCalculator;

/** The currency data with a filter adapter. 
 * Additionally, performs the currency conversion calculations.
 * @author Jason J.
 * @version 0.2.1-20140908
 */
public class CurrencyDataFilterListAdapter extends BaseAdapter 
implements Filterable {
	/** LOGTAG for debugging purposes. */
	final static private String LOGTAG = CurrencyDataFilterListAdapter.class
			.getSimpleName();
	/** Should never be used. */
	final static private String EMPTY_LIST = "No matching currencies";
	
	/** List of all currencies. */
	volatile private List<CurrencyData> currencyList = new ArrayList<CurrencyData>();
	/** The list of displayed currencies. */
	private List<CurrencyData> displayList = new ArrayList<CurrencyData>();
	
	/** The current value to be calculated. */ 
	private double currentValue = 0.0f;
	/** The current currency selected. */
	private CurrencyData startCurrency = null;
	
	/** The inflater used to inflate the row layouts. */
	private LayoutInflater inflater = null;
	/** The selected resource. Default is <code>fragment_currency_value</code> */
	private int layoutResource = -1;
	
	/** The current filter being used. */
	private Filter mFilter = null;
	
	/** Initializes the list adapter.
	 * @param context The current context
	 * @param resource The layout resource to use. Expected to contain ids:
	 * <ul>
	 * <li><code>currConv_frag_currVal_text_symbol</code></li>
	 * <li><code>currConv_frag_currVal_text_currValue</code></li>
	 * <li><code>currConv_frag_currVal_img_currFlag</code></li>
	 * <li><code>currConv_frag_currVal_text_currCode</code></li>
	 * </ul>
	 */
	public CurrencyDataFilterListAdapter(Context context, int resource) {
		this.inflater = (LayoutInflater) 
				context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.layoutResource = resource;
		setFilter(new String[]{});
	}
	/**Intializes the list adapter with the layout of 
	 * <code>R.layout.fragment_currency_value</code>.
	 * @param context The current context	 */
	public CurrencyDataFilterListAdapter(Context context){
		this(context, R.layout.fragment_currency_value);
	}

	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Mutators 
	////////////////////////////////////////////////////////////////////////////////////////////////
	/** Updates the value and recalculates. 
	 * @param value The value to set to and calculate. 
	 */
	public void updateCurrentValue(double value){
		this.currentValue = value;
		if (startCurrency != null){
			notifyDataSetChanged();
		}
	}
	
	/** Updates the value and recalculates.
	 * @param startCurrency The starting currency 
	 * @param value The value to set to and calculate. 
	 */
	public void updateCurrentValue(CurrencyData startCurrency, double value){
		this.startCurrency = startCurrency;
		updateCurrentValue(value);
	}
	
	/** @param list The current data to populate the adapter with. Assumes there
	 * are unique values in this list. */
	public void setCurrencyData(List<CurrencyData> list){
		synchronized (currencyList) {
			this.currencyList.clear();
			this.currencyList.addAll(list);		
		}
		notifyFilterDataSetChanged();
	}
	
	
	
	/** Sets the constraints for the currency data to be displayed.
	 * @param constraints The list of constraints in the form of currency codes. 
	 * If empty, all elements are displayed. Assumes unique values.
	 */
	public void setContraints(String[] constraints){
		setFilter(constraints);
		notifyFilterDataSetChanged();
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Start over ridden methods
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** @return <code>true</code> if the list is empty, <code>false</code> if 
	 * there are contents	 */
	public boolean isListEmpty(){
		return displayList.size() < 1;
	}
	
	/**
     * {@inheritDoc}
     * @return Count of items (note: returns "1" when empty.
     * @see #isListEmpty()
     */
	@Override
	public int getCount() {
		return isListEmpty() ? 1 :  displayList.size();
	}

	/**
     *{@inheritDoc}
     * @return The data at the specified position or <code>null</code>.
     */
	@Override
	public CurrencyData getItem(int position) {
		return isListEmpty() ? null : displayList.get(position);
	}
	

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Holder holder = null;
		if (convertView == null){
			convertView = inflater.inflate(layoutResource, parent, false);
			holder = new Holder();
			
			holder.symbol = (TextView)
					convertView.findViewById(R.id.currConv_frag_currVal_text_symbol);
			holder.value = (TextView)
					convertView.findViewById(R.id.currConv_frag_currVal_text_currValue);
			holder.flag = (ImageView)
				convertView.findViewById(R.id.currConv_frag_currVal_img_currFlag);
			holder.code = (TextView)
				convertView.findViewById(R.id.currConv_frag_currVal_text_currCode);
			convertView.setTag(holder);
		} else {
			holder = (Holder) convertView.getTag();
		}
		populateView(position, convertView, holder);
		setRowColour(convertView, position);
		return convertView;
	}
	
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Holder pattern
	////////////////////////////////////////////////////////////////////////////////////////////////
	/** The holder pattern for optimization. */
	private class Holder {
		public TextView symbol = null;
		public TextView value = null;
		public ImageView flag = null;  
		public TextView code = null;
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Helper methods
	////////////////////////////////////////////////////////////////////////////////////////////////
	/** Sets the background colour of row based upon position. 
	 * @param convertView The view to set
	 * @param position The current position of row 	 */
	static void setRowColour(View convertView, int position){
		int color = convertView.getResources().getColor(
				position % 2 == 1 ? //zero-indexed 	
				R.color.currconv_outputEven :
				R.color.currconv_outputOdd);
		convertView.setBackgroundColor(color);
	}
	
	
	/** Populates view at given position in the list.
	 * @param position The position of item
	 * @param convertView The view for accessing resources
	 * @param holder The holder from the holder pattern
	 */
	private void populateView(int position, View convertView, Holder holder) {
		if (isListEmpty()){
			holder.symbol.setVisibility(View.GONE);
			holder.flag.setVisibility(View.GONE);
			holder.code.setVisibility(View.GONE);
			//hide unused views, set empty message
			holder.value.setText(EMPTY_LIST);			
			return; //escape early
		} else {
			holder.symbol.setVisibility(View.VISIBLE);
			holder.flag.setVisibility(View.VISIBLE);
			holder.code.setVisibility(View.VISIBLE);
		}
		
		CurrencyData currency = displayList.get(position);
		holder.symbol.setText(currency.getCurrencySymbol());
		holder.value.setText(calculatedValue(currency));
		holder.code.setText(currency.getCurrencyCode());
		int flagResource  = currency.getFlagResource();
		
		if (flagResource > 0){
			holder.flag.setImageDrawable(
					convertView.getResources().getDrawable(flagResource));
		}
		
		return;
	}
	/** Calculates values of currencies.
	 * @param destCurrency The final destination currency.
	 * @return The formatted currency string
	 */
	private String calculatedValue(CurrencyData destCurrency){
		if (startCurrency == null){
			Log.w(LOGTAG, "Unexpected behaviour, no code selected");
			return "0.00";			
		}
		double rate = startCurrency.getRate(destCurrency.getCurrencyCode());
		if (rate <= 0){
			Log.w(LOGTAG, "Unexpected behaviour, rate is \""+rate+"\"");
			rate = 0.0d;
		}
		return CurrencyCalculator.format(
						destCurrency, 
						rate * currentValue);
	}
	
	/** Calls {@link #notifyDataSetChanged()} and resets the filter. */
	private void notifyFilterDataSetChanged() {
		notifyDataSetChanged();
		getFilter().filter("");;
	}
	
	/** Sets filter according to the constraints passed.
	 * @param constraints Currency code list.
	 * If given an empty array or <code>null</code>, no
	 * filtering is done. If given a list of currency codes, 
	 * only those currencies are shown.
	 */
	private void setFilter(final String constraints[]){
		this.mFilter = new Filter() {
	        @Override
	        protected FilterResults performFiltering(CharSequence constraint) {
	            final FilterResults filteredResults = new FilterResults();
	            final ArrayList<CurrencyData> results = 
	            		new ArrayList<CurrencyData>();
	            synchronized (currencyList) {
				    final int SIZE = currencyList.size();
		            
		            if (constraints == null || constraints.length <= 0){
		            	//no constraints? All the results!
		            	results.addAll(currencyList);
		            } else if (SIZE > 0){
		            	final int SIZE2 = constraints.length;
		            	for (int index = 0; index < SIZE; index++) {
		            		CurrencyData currency = currencyList.get(index);
							for (int index2 = 0; index2 < SIZE2; index2++){
								if (constraints[index2].equals(
										currency.getCurrencyCode())){
									results.add(currency);
									break; //we can only match once
								}
							}
							//assuming unique values in currencyList, we exit
		            		if (results.size() >= SIZE2){
		            			break;
		            		}
						}
		            }
	            }
	                filteredResults.values = results;
	                filteredResults.count = results.size();
	            return filteredResults;
	        }

	        @SuppressWarnings("unchecked")
			@Override
	        protected void publishResults(CharSequence constraint,
	                FilterResults results) {
	        	if (results.count == 0){
	        		notifyDataSetInvalidated();
	        	} else {
	        		try{
	        			@SuppressWarnings("unused")
						CurrencyData castTest = 
						((List<CurrencyData>) results.values).get(0);

		        		displayList.clear();
		        		displayList.addAll((Collection<? extends CurrencyData>) 
		        				results.values);
		        		notifyDataSetChanged();
	        		} catch (ClassCastException e){
	        			Log.w(LOGTAG, "Irregular behaviour: "+e);
	        			e.printStackTrace();
	        		}
	        	}
	        }
	    };
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Implemented interfaces
	////////////////////////////////////////////////////////////////////////////////////////////////

	@Override
	public Filter getFilter() {
		return mFilter;
	}	
	
}
