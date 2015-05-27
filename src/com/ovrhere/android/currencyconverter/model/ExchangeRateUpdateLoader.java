package com.ovrhere.android.currencyconverter.model;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.preference.PreferenceManager;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import com.ovrhere.android.currencyconverter.R;
import com.ovrhere.android.currencyconverter.model.currencyrequest.YahooApiExchangeRatesUpdate;
import com.ovrhere.android.currencyconverter.model.data.CurrencyConverterContract;
import com.ovrhere.android.currencyconverter.model.data.CurrencyConverterContract.DisplayOrderEntry;
import com.ovrhere.android.currencyconverter.model.data.CurrencyConverterContract.ExchangeRateEntry;
import com.ovrhere.android.currencyconverter.prefs.PreferenceUtils;
import com.ovrhere.android.currencyconverter.utils.Timestamp;

/**
 * Simple loader to provide abstraction from the back end.
 * 
 * @author Jason J. 
 * @version 0.3.0-20150527
 */
public class ExchangeRateUpdateLoader extends AsyncTaskLoader<Void> {
	/** Class name for debugging purposes. */
	final static private String LOGTAG = ExchangeRateUpdateLoader.class
			.getSimpleName();
	
	private final YahooApiExchangeRatesUpdate mUpdate;
	
	public ExchangeRateUpdateLoader(Context context) {
		super(context);
		String[] currencyList = context.getResources().getStringArray(R.array.currConv_rateOrder);
		boolean json = PreferenceManager.getDefaultSharedPreferences(context).getBoolean(
				context.getString(R.string.currConv_pref_KEY_USE_JSON_REQUEST),
				true);
		mUpdate = new YahooApiExchangeRatesUpdate(context.getContentResolver(), currencyList, json);
	}
	
	public ExchangeRateUpdateLoader(Context context, String[] currencyList) {
		super(context);
		boolean json = PreferenceManager.getDefaultSharedPreferences(context).getBoolean(
							context.getString(R.string.currConv_pref_KEY_USE_JSON_REQUEST),
							true);
		
		mUpdate = new YahooApiExchangeRatesUpdate(context.getContentResolver(), currencyList, json);
	}

    @Override
    protected void onStartLoading() {
        forceLoad(); //required for compat library
    }
	
	@Override
	public Void loadInBackground() {
		final long lastUpdate = PreferenceUtils.getLastUpdateTime(getContext());
		
		checkAndConfigFirstRun(lastUpdate);
		
		mUpdate.run();
		if (mUpdate.isUpdateSuccessful()) {
			PreferenceUtils.setLastUpdateTimeToNow(getContext());
		}
		
		
		return null;
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Helper methods
	////////////////////////////////////////////////////////////////////////////////////////////////

	
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Helper initializers
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Sets the database to be the value of the defaults, if there has never been any 
	 * updates.
	 * @param lastUpdate The time since epoch since last update.
	 */
	private void checkAndConfigFirstRun(long lastUpdate) {
		if (lastUpdate > 0) { //if greater than 0, we must have updated before
			return; //do not initialize database to defaults.
		}
		/*
		 * First run is to load the data
		 */
		Resources res = getContext().getResources();
		String[] currencyList = res.getStringArray(R.array.currConv_rateOrder);
		TypedArray arrayIdList = res.obtainTypedArray(R.array.currConv_rateArray_array);
		
		List<ContentValues> returnValues = new ArrayList<ContentValues>();
		
		buildDisplayOrder(currencyList);
		
		final int SIZE = currencyList.length;
		for (int index = 0; index < SIZE; index++) {
			String code = currencyList[index];
			
			int id = arrayIdList.getResourceId(index, 0); //get our array id
			TypedArray array = res.obtainTypedArray(id); //use id to get our actual rate array.
			
			createCurrencyData(code, currencyList, res.obtainTypedArray(id), returnValues);
			array.recycle();
		}
		
		arrayIdList.recycle();		
		
		ContentValues[] input = new ContentValues[returnValues.size()];
		
		getContext().getContentResolver()
					.bulkInsert(ExchangeRateEntry.CONTENT_URI, returnValues.toArray(input));
		
		long time = 0;
		try {
			time = Timestamp.parse(getContext().getString(R.string.currConv_defRate_updateTime)).getTime();
		} catch (ParseException e) {
			Log.w(LOGTAG, "Oops! Default timestamp did not parse.");
		}
		PreferenceUtils.setLastUpdateTime(getContext(), time);
	}

	/** Populates the display order table. 
	 * @param currencyCodeOrder An ordered set of currencies.
	 */
	private void buildDisplayOrder(String[] currencyCodeOrder){
		final int SIZE = currencyCodeOrder.length;
		ContentValues[] input = new ContentValues[SIZE];
		
		for (int index = 0; index < SIZE; index++) {
			input[index] = new ContentValues();
			input[index].put(DisplayOrderEntry.COLUMN_CURRENCY_CODE, currencyCodeOrder[index]);
			input[index].put(DisplayOrderEntry.COLUMN_DEF_DISPLAY_ORDER, index);
		}
		
		getContext().getContentResolver()
					.bulkInsert(DisplayOrderEntry.CONTENT_URI, input); 
	}
	
	/**
	 * 
	 * @param sourceCode The starting currency to convert from
	 * @param currencyList The list of all possible currencies (including itself)
	 * @param exchangeRates The list of matching exchange rates (parallel to currencyList) 
	 * @param returnValues The list to modified values in for return
	 */
	private static void createCurrencyData(String sourceCode, String[] currencyList, 
			TypedArray exchangeRates, final List<ContentValues> returnValues){
				
		if (currencyList.length != exchangeRates.length()){
			Log.w(LOGTAG, "Irregular behavior: Mismatched lists?");			
			throw new IndexOutOfBoundsException();
		}
		
		final int SIZE = currencyList.length;
		
		for (int index = 0; index < SIZE; index++) {
			//NOTE: this means we will include self conversions, e.g. USD -> USD
			returnValues.add(
						buildContentValues(
								sourceCode,
								currencyList[index], 
								exchangeRates.getFloat(index, 0))
					);
		}		
		
	}
	
	/**
	 * @param src The source code
	 * @param dst The destination code
	 * @param rate The rate
	 * @return  The content values populated using the keys in 
	 * {@link CurrencyConverterContract.ExchangeRateEntry}
	 */
	private static ContentValues buildContentValues(String src, String dst, double rate) {
		ContentValues cvPair = new ContentValues();
		cvPair.put(ExchangeRateEntry.COLUMN_SOURCE_CURRENCY_CODE, src);
		cvPair.put(ExchangeRateEntry.COLUMN_DEST_CURRENCY_CODE, dst);							
		cvPair.put(ExchangeRateEntry.COLUMN_EXCHANGE_RATE, rate);
		return cvPair;
	}
}
