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
package com.ovrhere.android.currencyconverter.ui.fragments;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.ovrhere.android.currencyconverter.R;
import com.ovrhere.android.currencyconverter.dao.CurrencyData;
import com.ovrhere.android.currencyconverter.model.CurrencyExchangeRateAsyncModel;
import com.ovrhere.android.currencyconverter.ui.adapters.CurrencyDataFilterListAdapter;
import com.ovrhere.android.currencyconverter.ui.adapters.CurrencyDataSpinnerAdapter;
import com.ovrhere.android.currencyconverter.utils.CompatClipboard;
import com.ovrhere.android.currencyconverter.utils.CurrencyCalculator;

/**
 * The main fragment where values are inputed and results shown.
 * @author Jason J.
 * @version 0.2.0-20140902
 */
public class MainFragment extends Fragment 
implements Handler.Callback, OnItemLongClickListener {
	/** The class name used for bundles. */
	final static private String CLASS_NAME = MainFragment.class.getSimpleName();
	/** The log tag for errors. */
	final static private String LOGTAG = CLASS_NAME;
	
	/** Bundle key: List<CurrencyData>/List<Parcellable>. The currently parsed list. */
	final static private String KEY_CURRENCY_LIST = 
			CLASS_NAME + ".KEY_CURRENCY_LIST";
	/** Bundle key: Int. The "from" currency spinner position (see #sp_fromCurr ). */
	final static private String KEY_SOURCE_CURRENCY_POSITION = 
			CLASS_NAME + ".KEY_SOURCE_CURRENCY_POSITION";
	/** Bundle key: Int. The "to" currency spinner position (see #sp_toCurr ). */
	final static private String KEY_DEST_CURRENCY_POSITION = 
			CLASS_NAME + ".KEY_DEST_CURRENCY_POSITION";
	/** Bundle key: String. The input to convert. */
	final static private String KEY_CURRENCY_VALUE_INPUT = 
			CLASS_NAME+".KEY_CURRENCY_VALUE_INPUT";
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// End constants
	////////////////////////////////////////////////////////////////////////////////////////////////
	/** The model for fetching currency info. */
	private CurrencyExchangeRateAsyncModel asycModel = null;
	
	/** The currency list to use. */
	private List<CurrencyData> currencyList = new ArrayList<CurrencyData>();
	
	/** The from adapter. */
	private CurrencyDataSpinnerAdapter sourceCurrAdapter = null;
	/** The to adapter. */
	private CurrencyDataSpinnerAdapter destCurrAdapter = null;
	/** The output list adapter. */
	private CurrencyDataFilterListAdapter outputListAdapter = null;
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Views start here
	////////////////////////////////////////////////////////////////////////////////////////////////
	/** The spinner for the from Currency. */
	private Spinner sp_sourceCurr = null;
	/** The spinner for the to Currency. */
	private Spinner sp_destCurr = null;
	/** The current currency flag. */
	private ImageView img_currFlag = null;
	/** The current currency symbol. */
	private TextView tv_currSymbol = null;
	/** The current warning message. */
	private TextView tv_warning = null;
	/** The currency input. */
	private EditText et_currInput = null;
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// End members 
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	public MainFragment() {	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {	
		super.onSaveInstanceState(outState);
		
		outState.putInt(KEY_SOURCE_CURRENCY_POSITION, 
				sp_sourceCurr.getSelectedItemPosition());
		outState.putInt(KEY_DEST_CURRENCY_POSITION, 
				sp_destCurr.getSelectedItemPosition());
		outState.putParcelableArrayList(KEY_CURRENCY_LIST, 
				(ArrayList<? extends Parcelable>) currencyList);
		outState.putString(KEY_CURRENCY_VALUE_INPUT, 
				et_currInput.getText().toString());
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		asycModel = new CurrencyExchangeRateAsyncModel(getActivity());
		asycModel.addMessageHandler(new Handler(this));
		if (savedInstanceState == null){
			requestFreshExchangeRates();
		} else {
			ArrayList<Parcelable> list = 
					savedInstanceState.getParcelableArrayList(KEY_CURRENCY_LIST);
			if (list != null){
				currencyList.clear();
				try {
					currencyList.addAll((Collection<? extends CurrencyData>) list);
				} catch (ClassCastException e){
					Log.e(LOGTAG, "Current data invalid: "+e);
				}
			}
			if (currencyList.isEmpty()){
				requestFreshExchangeRates();
			}
		}
		
	}

	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_main, container,
				false);
		outputListAdapter = new CurrencyDataFilterListAdapter(getActivity());
		
		ListView outputListView = (ListView) 
				rootView.findViewById(R.id.com_ovrhere_currConv_main_listView);
		outputListView.setAdapter(outputListAdapter);
		outputListView.setOnItemLongClickListener(this);
		registerForContextMenu(outputListView);
		
		
		sourceCurrAdapter = 
				new CurrencyDataSpinnerAdapter(getActivity(), 
						android.R.layout.simple_list_item_1);

		destCurrAdapter = 
				new CurrencyDataSpinnerAdapter(getActivity(), 
						android.R.layout.simple_list_item_1, true);
		destCurrAdapter.setSelectAllText("View All");
		
		updateCurrencyAdapters();
		
		
		sp_sourceCurr = (Spinner) 
				rootView.findViewById(R.id.com_ovrhere_currConv_main_spinner_currencySource);
		sp_sourceCurr.setAdapter(sourceCurrAdapter);
		sp_sourceCurr.setOnItemSelectedListener(sourceItemSelectedListener);
		
		sp_destCurr = (Spinner) 
				rootView.findViewById(R.id.com_ovrhere_currConv_main_spinner_currencyDest);
		sp_destCurr.setAdapter(destCurrAdapter);
		sp_destCurr.setOnItemSelectedListener(destItemSelectListener);
		
		et_currInput = (EditText)
				rootView.findViewById(R.id.com_ovrhere_currConv_main_edittext_valueToConv);
		et_currInput.addTextChangedListener(valueInputListener);
		
		int fromCurrSelect = 0;
		int toCurrSelect = 0;
		if (savedInstanceState != null){
			fromCurrSelect = savedInstanceState.getInt(KEY_SOURCE_CURRENCY_POSITION);
			toCurrSelect = savedInstanceState.getInt(KEY_DEST_CURRENCY_POSITION);
			String input = savedInstanceState.getString(KEY_CURRENCY_VALUE_INPUT);
			et_currInput.setText(input != null ? input : "0");
		}
		sp_sourceCurr.setSelection(fromCurrSelect);
		sp_destCurr.setSelection(toCurrSelect);
		
		img_currFlag = (ImageView)  
				rootView.findViewById(R.id.com_overhere_currConv_main_image_currFlag);
		
		tv_currSymbol = (TextView)
				rootView.findViewById(R.id.com_ovrhere_currConv_main_text_currSymbol);
		tv_warning = (TextView)
				rootView.findViewById(R.id.com_ovrhere_currConv_main_text_warning);
		updateSourceCurrency();
		return rootView;
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
				ContextMenuInfo menuInfo) {
		  if (v.getId()==R.id.com_ovrhere_currConv_main_listView) {
		    AdapterView.AdapterContextMenuInfo info = 
		    		(AdapterView.AdapterContextMenuInfo)menuInfo;
		    
		    CurrencyData currency = outputListAdapter.getItem(info.position);
		    if (currency != null){
		    	String title = getActivity().getResources().getString(
		    			R.string.com_ovrhere_currConv_context_currencyAction,
		    			currency.getCurrencyCode());
		    	menu.setHeaderTitle(title);
			    menu.add(Menu.CATEGORY_SECONDARY, 0, 0, android.R.string.copy);
		    }
		  }
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		if (item.getGroupId() == Menu.CATEGORY_SECONDARY){
			AdapterView.AdapterContextMenuInfo info = 
					(AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
			switch (item.getItemId()){
				case 0:
					int position = info.position;
				copyConvertedValue(position);					
			}
		}
		return super.onContextItemSelected(item); 
	}


	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Helper Methods
	////////////////////////////////////////////////////////////////////////////////////////////////

	/** Copies the converted currency at the given position. 
	 * @param position The position the item was selected from.
	 */
	private void copyConvertedValue(int position) {
		Resources r = getActivity().getResources();
		CurrencyData sourceCurrency = (CurrencyData) sp_sourceCurr.getSelectedItem();
		CurrencyData destCurrency = outputListAdapter.getItem(position);
		if (destCurrency != null && sourceCurrency != null){
			double amount = convertToDouble(et_currInput.getText().toString());
			String value = destCurrency.getCurrencySymbol() +
					CurrencyCalculator.convert(sourceCurrency, destCurrency, amount)
					+" "+destCurrency.getCurrencyCode();
			String label = 
					r.getString(R.string.com_ovrhere_currConv_clipboard_label_copiedCurrency);
			CompatClipboard.copyToClipboard(getActivity(), label, value);
		}
	}
	
	/** Updates all currency adapters the the current value of #currencyList 
	 * Assumes all adapters are set. */
	private void updateCurrencyAdapters() {
		sourceCurrAdapter.setCurrencyData(currencyList);
		destCurrAdapter.setCurrencyData(currencyList);
		outputListAdapter.setCurrencyData(currencyList);
	}
	
	/** Requests a fresh list of exchange rates from the model. */
	private void requestFreshExchangeRates() {
		asycModel.sendMessage(
				CurrencyExchangeRateAsyncModel.REQUEST_GET_ALL_RECORDS, 
				true);
	}
	
	
	/** Parses input and sends it to adapter for calculation(s).
	 * @param input The input to strip & parse.		 */
	private void calculateOutput(String input) {
		double value = convertToDouble(input);
		outputListAdapter.updateCurrentValue(value);
	}
	
	
	/** Updates source views to match source currency. */
	private void updateSourceCurrency(){
		if (sp_sourceCurr == null || tv_currSymbol == null || img_currFlag == null){
			return; //nothing can be set.
		}
		int position = sp_sourceCurr.getSelectedItemPosition();
		
		if (position < 0 || sourceCurrAdapter.getCount() < 1){
			return; //nothing to show yet.
		}
		CurrencyData data =  null;
		try {
			data = sourceCurrAdapter.getItem(position);
		} catch (IndexOutOfBoundsException e){
			Log.w(LOGTAG, "Index out:"+position);
		}
		
		if (data == null){
			Log.w(LOGTAG, "Irregular behaviour skipping update.");
			return;
		}
		Resources r = getActivity().getResources();
		String date = r.getString(R.string.com_ovrhere_currConv_cachedRate_warning, 
									getReadableTimestamp(data));
		tv_warning.setText(date);
		tv_currSymbol.setText(data.getCurrencySymbol());
		int flagId = data.getFlagResource(); 
		if (flagId >= 0){
			img_currFlag.setImageDrawable(r.getDrawable(flagId));
		}
	}

	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Utility function
	////////////////////////////////////////////////////////////////////////////////////////////////
	/** Parses currency data's timestamp into more readable form. 
	 * @param currencyData The current data to parse.
	 * @param resources The resources to 
	 * @return
	 */
	static private String getReadableTimestamp(CurrencyData currencyData) {
		String original = currencyData.getModifiedTimestamp();
		//TODO more granular timestamp readablity
		int seconds = original.lastIndexOf(":");
		if (seconds > -1){
			return original.substring(0, seconds);
		}
		return original;
	}
	
	/** Converts input to pure double. Only 0-9 and "." allowed
	 * @param input The string input.
	 * @return The parsed double. 	 */
	static private double convertToDouble(String input) {
		//scrub input from non-valid input
		return Double.parseDouble(input.replaceAll("[^\\.0-9]", ""));
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Implemented interfaces
	////////////////////////////////////////////////////////////////////////////////////////////////
	/** The input listener for when entering values. */
	private TextWatcher valueInputListener = new TextWatcher() {		
		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			//nothing to do
		}
		
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
			//nothing to do
		}
		
		@Override
		public void afterTextChanged(Editable s) {
			String input = s.toString();
			calculateOutput(input);
		}
	};
	
	/** The selection listener for the "from" spinner. */
	private OnItemSelectedListener sourceItemSelectedListener = 
			new OnItemSelectedListener() {
		@Override
		public void onItemSelected(android.widget.AdapterView<?> parent, 
				View view, int position, long id) {
			updateSourceCurrency();
			CurrencyData currency = sourceCurrAdapter.getItem(position);
			if (currency != null){
				outputListAdapter.updateCurrentValue(currency, 0);
				calculateOutput(et_currInput.getText().toString());
			}
			//TODO change image, symbol, recalculate 
		};
		@Override
		public void onNothingSelected(android.widget.AdapterView<?> parent) {
			//do nothing for now
		};
	};
	/** The selection listener for the "to" spinner. */
	private OnItemSelectedListener destItemSelectListener =
			new OnItemSelectedListener() {
		@Override
		public void onItemSelected(android.widget.AdapterView<?> parent, 
				View view, int position, long id) {
			CurrencyData data = destCurrAdapter.getItem(position);
			if (data == null){
				outputListAdapter.setContraints(new String[]{});
			} else {
				outputListAdapter.setContraints(
						new String[]{data.getCurrencyCode()});
			}
			
			//TODO change list, recalculate 
		};
		@Override
		public void onNothingSelected(android.widget.AdapterView<?> parent) {
			//do nothing for now
		}
	};
	
	@Override
	public boolean onItemLongClick(android.widget.AdapterView<?> parent, 
			View view, int position, long id) {
		CurrencyData selectedCurrency = outputListAdapter.getItem(position);
		if (selectedCurrency != null){
			Log.d(LOGTAG, "Testing value: "+selectedCurrency.getCurrencyCode());
		}
		return false;
	};
	
	@Override
	public boolean handleMessage(Message msg) {
		switch (msg.what){
		case CurrencyExchangeRateAsyncModel.REPLY_RECORDS_RESULT:
			try {
				@SuppressWarnings("unchecked")
				List<CurrencyData> list = (List<CurrencyData>) msg.obj;
				if (!list.isEmpty()){
					@SuppressWarnings("unused")
					//cast type checking.
					CurrencyData data = (CurrencyData) list.get(0);
				}
				currencyList.clear();
				currencyList.addAll(list);
				updateCurrencyAdapters();
				updateSourceCurrency();
			} catch (ClassCastException e){
				Log.e(LOGTAG, "Current data invalid: "+e);
			}
			return true;
		}
		return false;
	}

	
}
