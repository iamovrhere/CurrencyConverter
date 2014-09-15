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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
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
import com.ovrhere.android.currencyconverter.prefs.PreferenceUtils;
import com.ovrhere.android.currencyconverter.ui.adapters.CurrencyDataFilterListAdapter;
import com.ovrhere.android.currencyconverter.ui.adapters.CurrencyDataSpinnerAdapter;
import com.ovrhere.android.currencyconverter.utils.CompatClipboard;
import com.ovrhere.android.currencyconverter.utils.CurrencyCalculator;
import com.ovrhere.android.currencyconverter.utils.DateFormatter;
import com.ovrhere.android.currencyconverter.utils.KeyboardUtil;

/**
 * The main fragment where values are inputed and results shown.
 * @author Jason J.
 * @version 0.4.4-20140914
 */
public class MainFragment extends Fragment 
implements Handler.Callback, OnItemLongClickListener {
	/** The class name used for bundles. */
	final static private String CLASS_NAME = MainFragment.class.getSimpleName();
	/** The log tag for errors. */
	final static private String LOGTAG = CLASS_NAME;
	/** Whether or not to debug. */
	final static private boolean DEBUG = true;
	
	/** Bundle key: List<CurrencyData>/List<Parcellable>. The currently parsed list. */
	final static private String KEY_CURRENCY_LIST = 
			CLASS_NAME + ".KEY_CURRENCY_LIST";
	
	/** Bundle key: String. The input to convert. */
	final static private String KEY_CURRENCY_VALUE_INPUT = 
			CLASS_NAME+".KEY_CURRENCY_VALUE_INPUT";
	/** Bundle key. Boolean. The value of {@link #currentlyUpdating} */
	final static private String KEY_CURRENTLY_UPDATING = 
			CLASS_NAME + ".KEY_CURRENTLY_UPDATED";
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// End constants
	////////////////////////////////////////////////////////////////////////////////////////////////
	/** The model for fetching currency info. */
	private CurrencyExchangeRateAsyncModel asyncModel = null;
	/** The handler for the model. */
	private Handler asyncHandler = new Handler(this);
	
	/** Lists of resources used with {@link DateFormatter}.*/ 
	private HashMap<String, Integer> dateResUnits = new HashMap<String, Integer>();
	
	/** The currency list to use. Should be synchronized. */
	private List<CurrencyData> currencyList = new ArrayList<CurrencyData>();
	
	/** The from adapter. */
	private CurrencyDataSpinnerAdapter sourceCurrAdapter = null;
	/** The to adapter. */
	private CurrencyDataSpinnerAdapter destCurrAdapter = null;
	/** The output list adapter. */
	private CurrencyDataFilterListAdapter outputListAdapter = null;
	/** <code>true</code> if updating, <code>false</code> otherwise. */
	private boolean currentlyUpdating = false;
	
	/** The shared preference handle. */
	private SharedPreferences prefs = null;
	
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
	/** The View for the spinny-progress bar for updates. */
	private View updateProgressSpin = null;
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// End members 
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	public MainFragment() {	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {	
		super.onSaveInstanceState(outState);
		synchronized (currencyList) {
			outState.putParcelableArrayList(KEY_CURRENCY_LIST, 
					(ArrayList<? extends Parcelable>) currencyList);
		}
		outState.putString(KEY_CURRENCY_VALUE_INPUT, 
				et_currInput.getText().toString());
		outState.putBoolean(KEY_CURRENTLY_UPDATING, 
				currentlyUpdating);
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		asyncModel.dispose();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		asyncModel = new CurrencyExchangeRateAsyncModel(getActivity());
		asyncModel.addMessageHandler(asyncHandler);
		
		dateResUnits.put(DateFormatter.MINUTE_UNIT, 
					R.plurals.com_ovrhere_currConv_minutes);
		dateResUnits.put(DateFormatter.HOUR_UNIT, 
				R.plurals.com_ovrhere_currConv_hours);
		dateResUnits.put(DateFormatter.DAY_UNIT, 
				R.plurals.com_ovrhere_currConv_days);
		
		if (PreferenceUtils.isFirstRun(getActivity())){
			PreferenceUtils.setToDefault(getActivity());
		}
		prefs = PreferenceUtils.getPreferences(getActivity());		
		
		if (savedInstanceState == null){
			requestFreshExchangeRates(false);
		} else {
				currentlyUpdating = savedInstanceState.getBoolean(
						KEY_CURRENTLY_UPDATING);
			
			ArrayList<Parcelable> list = 
					savedInstanceState.getParcelableArrayList(KEY_CURRENCY_LIST);
			synchronized (currencyList) {
				if (list != null){
					currencyList.clear();
					try {
						currencyList.addAll((Collection<? extends CurrencyData>) list);
					} catch (ClassCastException e){
						Log.e(LOGTAG, "Current data invalid: "+e);
					}
				}
				if (currencyList.isEmpty()){
					requestFreshExchangeRates(false);
				}
			}
		}		
		
	}

	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_main, container,
				false);
		initAdapters();		
		initInputViews(rootView);
		initOutputViews(rootView);
		initKeyboardHide(rootView);
		processSavedState(savedInstanceState);
		
		updateCurrencyAdapters();
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
			    menu.add(Menu.CATEGORY_SECONDARY, 1, 1, 
			    		R.string.com_ovrhere_currConv_context_detailedCopy);
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
					int pos1 = info.position;
					copyConvertedValue(pos1, false);
					break;
				case 1:
					int pos2 = info.position;
					copyConvertedValue(pos2, true);
					break;
			}
		}
		return super.onContextItemSelected(item); 
	}

	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Initializer helper methods
	////////////////////////////////////////////////////////////////////////////////////////////////
	/** Processes the saved state, preferences and updates views accordingly.
	 * Assumes all views to be valid.
	 * @param savedInstanceState The current saved state.
	 * @see #initInputViews(View)
	 * @see #initOutputViews(View) 	 */
	private void processSavedState(Bundle savedInstanceState) {
		Resources r = getActivity().getResources();
		int sourceCurrSelect = 
				prefs.getInt(
						r.getString(R.string.com_ovrhere_currConv_pref_KEY_SOURCE_CURRENCY_INDEX), 
						0);
		int destCurrSelect = 
				prefs.getInt(
						r.getString(R.string.com_ovrhere_currConv_pref_KEY_DEST_CURRENCY_INDEX), 
						0);

		sp_sourceCurr.setSelection(sourceCurrSelect);
		sp_destCurr.setSelection(destCurrSelect);
		
		String input = "";
		if (savedInstanceState != null){
			input = 
				savedInstanceState.getString(KEY_CURRENCY_VALUE_INPUT) == null ?
				input : savedInstanceState.getString(KEY_CURRENCY_VALUE_INPUT);
			et_currInput.setText(input);
		}
		CurrencyData cdata = (CurrencyData) sp_sourceCurr.getSelectedItem();
		if (cdata != null){
			outputListAdapter.updateCurrentValue(cdata, convertToDouble(input));	
		}
	}
	
	/** Initializes the output views such as textviews, images & listview.
	 * Assumes adapters are valid.
	 * @param rootView The rootview to configure	
	 * @see #initAdapters()  */
	private void initOutputViews(View rootView) {
		ListView outputListView = (ListView) 
				rootView.findViewById(R.id.com_ovrhere_currConv_main_listView);
		outputListView.setAdapter(outputListAdapter);
		outputListView.setOnItemLongClickListener(this);
		registerForContextMenu(outputListView);
		
		updateProgressSpin =
				rootView.findViewById(R.id.com_ovrhere_currConv_main_progressSpin);
		checkProgressBar();
		
		tv_currSymbol = (TextView)
				rootView.findViewById(R.id.com_ovrhere_currConv_main_text_currSymbol);
		tv_warning = (TextView)
				rootView.findViewById(R.id.com_ovrhere_currConv_main_text_warning);

		img_currFlag = (ImageView)  
				rootView.findViewById(R.id.com_overhere_currConv_main_image_currFlag);
	}
	/** Initializes all input views. Assumes adapters are valid.
	 * @param rootView The rootview to configure	
	 * @see #initAdapters()	 */
	private void initInputViews(View rootView) {
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
	}
	/** Initializes adapters for output list and spinners. */
	private void initAdapters() {
		outputListAdapter = new CurrencyDataFilterListAdapter(getActivity());
		
		sourceCurrAdapter = 
				new CurrencyDataSpinnerAdapter(getActivity(), 
						android.R.layout.simple_list_item_1);
		destCurrAdapter = 
				new CurrencyDataSpinnerAdapter(getActivity(), 
						android.R.layout.simple_list_item_1, true);
		destCurrAdapter.setSelectAllText(
				getActivity().getResources()
					.getString(R.string.com_ovrhere_currConv_spinner_dest_selectAll));
	}
	/** Initializes the hiding of the keyboard for all non-edit-texts views.
	 * @param rootView The rootview to attach to.
	 */
	private void initKeyboardHide(View rootView){
		final FragmentActivity activity = getActivity();
		rootView.setOnTouchListener(new View.OnTouchListener() {	
				//this is will bubble until consumed (such as EditText)
				@SuppressLint("ClickableViewAccessibility")
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					KeyboardUtil.hideSoftKeyboard(activity);
					return false; //we aren't consume either
				}
			});
	}
	
	/** Updates all currency adapters the the current value of #currencyList 
	 * Assumes all adapters are set. */
	private void updateCurrencyAdapters() {
		synchronized (currencyList) {
			sourceCurrAdapter.setCurrencyData(currencyList);
			destCurrAdapter.setCurrencyData(currencyList);
			outputListAdapter.setCurrencyData(currencyList);
		}
	}
	
	/** Updates source views to match source currency. */
	private void updateSourceCurrency(){
		if (sp_sourceCurr == null || tv_warning == null || 
				tv_currSymbol == null || img_currFlag == null){
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
		checkTimestampWarning(r, data);
		
		tv_currSymbol.setText(data.getCurrencySymbol());
		int flagId = data.getFlagResource(); 
		if (flagId >= 0){
			img_currFlag.setImageDrawable(r.getDrawable(flagId));
		}
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Helper Methods
	////////////////////////////////////////////////////////////////////////////////////////////////

	/** Copies the converted currency at the given position. 
	 * @param position The position the item was selected from.
	 * @param detailed <code>true</code> to copy with multiple decimals places,
	 * <code>false</code> for the basic currency decimals.	 */
	private void copyConvertedValue(int position, boolean detailed) {
		Resources r = getActivity().getResources();
		CurrencyData sourceCurrency = (CurrencyData) sp_sourceCurr.getSelectedItem();
		CurrencyData destCurrency = outputListAdapter.getItem(position);
		if (destCurrency != null && sourceCurrency != null){
			double amount = convertToDouble(et_currInput.getText().toString());
			String value = destCurrency.getCurrencySymbol() +
					CurrencyCalculator.format(
							destCurrency, 
							CurrencyCalculator.convert(
									sourceCurrency, 
									destCurrency, 
									amount),
							detailed
							)
					+" "+destCurrency.getCurrencyCode();
			String label = 
					r.getString(R.string.com_ovrhere_currConv_clipboard_label_copiedCurrency);
			CompatClipboard.copyToClipboard(getActivity(), label, value);
		}
	}
	/** Hides/Shows the progress bar based on the value of {@link #currentlyUpdating}. */
	private void checkProgressBar(){
		updateProgressSpin.setVisibility(
				currentlyUpdating ? View.VISIBLE : View.GONE );
	}
	
	/** Takes the currency time stamp and checks if request for a new update.
	 * and update views. 
	 * @param r The resources to access strings with.
	 * @param currencyData The current data to parse. 
	 * @return The readable timestamp.	 */
	private void checkTimestampToUpdate(Resources r, CurrencyData currencyData) {
		long updateInterval = prefs.getInt(
							r.getString(
									R.string.com_ovrhere_currConv_pref_KEY_UPDATE_CURRENCY_INTERVAL),
							0);
		long interval = 
				new Date().getTime() - currencyData.getModifiedDate().getTime();
		if (updateInterval < interval ){
			requestFreshExchangeRates(true);
			checkTimestampWarning(r, currencyData);
		}
	}
	

	/** Takes the currency time stamp and checks if to display message. 
	 * @param r The resources to access strings with.
	 * @param currencyData The current data to parse. 
	 * @return The readable timestamp.	 */
	private void checkTimestampWarning(Resources r, CurrencyData currencyData) {
		long updateInterval = prefs.getInt(
							r.getString(
									R.string.com_ovrhere_currConv_pref_KEY_UPDATE_CURRENCY_INTERVAL),
							0);
		long interval = 
				new Date().getTime() - currencyData.getModifiedDate().getTime();
		if (updateInterval < interval){			
			String timestamp = DateFormatter.dateToRelativeDate(
					getActivity(), dateResUnits,
					currencyData.getModifiedDate());
			tv_warning.setText(
					r.getString(R.string.com_ovrhere_currConv_cachedRate_warning, 
							timestamp)
							);
			tv_warning.setVisibility(View.VISIBLE);
			requestFreshExchangeRates(true);
		} else {
			tv_warning.setVisibility(View.GONE);
		}
	}
	
	
	/** Requests a fresh list of exchange rates from the model. 
	 * @param forceUpdate <code>true</code> to force online update, 
	 * <code>false</code> to forgo it. */
	private void requestFreshExchangeRates(boolean forceUpdate) {
		asyncModel.sendMessage(
				CurrencyExchangeRateAsyncModel.REQUEST_GET_ALL_RECORDS, 
				forceUpdate);
	}
	
	
	/** Parses input and sends it to adapter for calculation(s).
	 * @param input The input to strip & parse.		 */
	private void calculateOutput(String input) {
		double value = convertToDouble(input);
		outputListAdapter.updateCurrentValue(value);
	}	
	
	
	/** Sets an integer preference 
	 * @param stringRes The preference string resource
	 * @param value The value to insert.	 */
	private void putIntPref(int stringRes, int value){
		SharedPreferences.Editor editor = prefs.edit();
		editor.putInt(getActivity().getResources().getString(stringRes), value);
		editor.commit();
	}
	
	
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Utility function
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	
	/** Converts input to pure double. Only 0-9 and "." allowed
	 * @param input The string input. If empty, returns 0.
	 * @return The parsed double. 	 */
	static private double convertToDouble(String input) {
		if (input.isEmpty()){ return 0.0d; }
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
			calculateOutput(s.toString().trim());
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
			putIntPref(
					R.string.com_ovrhere_currConv_pref_KEY_SOURCE_CURRENCY_INDEX, 
					position);
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
			putIntPref(
					R.string.com_ovrhere_currConv_pref_KEY_DEST_CURRENCY_INDEX, 
					position);
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
		if (DEBUG){
			Log.d(LOGTAG, "Message received: "+msg.what );
		}
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
				synchronized (currencyList) {
					currencyList.clear();
					currencyList.addAll(list);
				}
				updateCurrencyAdapters();
				updateSourceCurrency();
				if (!currentlyUpdating){
					checkTimestampToUpdate(getResources(), currencyList.get(0));
				} else {
					currentlyUpdating = false;
					checkProgressBar();
				}
			} catch (ClassCastException e){
				Log.e(LOGTAG, "Current data invalid: "+e);
			}
			return true;

		case CurrencyExchangeRateAsyncModel.NOTIFY_INITIALIZING_DATABASE:			
			return true;
		case CurrencyExchangeRateAsyncModel.NOTIFY_UPDATING_RATES:
			currentlyUpdating = true;
			checkProgressBar();
			return true;
		case CurrencyExchangeRateAsyncModel.ERROR_REQUEST_FAILED:
		case CurrencyExchangeRateAsyncModel.ERROR_REQUEST_TIMEOUT:
			currentlyUpdating = false;
			checkProgressBar();
			return true;
		}
		
		return false;
	}

	
}
