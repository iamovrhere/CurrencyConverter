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

import java.util.Currency;
import java.util.Locale;

import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ovrhere.android.currencyconverter.R;
import com.ovrhere.android.currencyconverter.model.CurrencyResourceMap;
import com.ovrhere.android.currencyconverter.model.data.CurrencyConverterContract.DisplayOrderEntry;
import com.ovrhere.android.currencyconverter.model.data.CurrencyConverterContract.ExchangeRateEntry;
import com.ovrhere.android.currencyconverter.utils.CurrencyCalculator;

/**
 * The adapter to show the list of currency exchanges, from the source currency
 * to the list given. 
 * Remember to use {@link #CURRENCY_LIST_COLUMNS} order.
 * 
 * @author Jason J.
 * @version 0.2.0-20150527
 */
public class CurrencyCursorAdapter extends CursorAdapter {
	/* Class name for debugging purposes. */
	//final static private String LOGTAG = CurrencyCursorAdapter.class.getSimpleName();
	
	/** The expected column order; anything else will throw an exception. */
	public static final String[] CURRENCY_LIST_COLUMNS = new String[]{
		ExchangeRateEntry.TABLE_NAME+"."+ExchangeRateEntry._ID,
		DisplayOrderEntry.COLUMN_DEF_DISPLAY_ORDER,
		//ExchangeRateEntry.COLUMN_SOURCE_CURRENCY_CODE,
		ExchangeRateEntry.COLUMN_DEST_CURRENCY_CODE,
		ExchangeRateEntry.COLUMN_EXCHANGE_RATE
	};
	
	public static final int COL_ID = 0;
	public static final int COL_DISPLAY_ORDER = 1;	
	public static final int COL_DESTINATION_CODE = 2;
	public static final int COL_EXCHANGE_RATE = 3;
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// End Constants
	////////////////////////////////////////////////////////////////////////////////////////////////

	
	/** The value to convert from. */
	private double mInputValue = 0.0d;
	
	/**
	 * {@inheritDoc}
	 */
	public CurrencyCursorAdapter(Context context, Cursor c, int flags) {
		super(context, c, flags);
	}

	
	/** Updates the value and recalculates. 
	 * @param value The value to set to and calculate. 
	 */
	public void updateCurrentValue(double value){
		this.mInputValue = value;
		notifyDataSetChanged();
	}
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Start Override methods
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		Holder holder = (Holder) view.getTag();
        if (holder == null){
            holder = new Holder(view);
            view.setTag(holder);
        }
        convertCursorRowToViewOutput(holder, cursor);
        setRowColour(view, cursor.getPosition());
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context)
        			.inflate(R.layout.list_item_currency_value, parent, false);        
        return view;
    }
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Helper & Utility methods
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** Sets the background colour of row based upon position. 
	 * @param view The view to set
	 * @param position The current position of row 	 */
	private static void setRowColour(View view, int position){
		Drawable background = view.getResources() 
				.getDrawable(//zero-indexed, and documentation has it that odd is first row, even is 2nd.
					position % 2 != 0 ? 
					R.drawable.list_item_background_even : R.drawable.list_item_background_odd);
		
		view.setBackgroundDrawable(background);
	}
	
	
	/** Populates view at given position in the list.
	 * @param holder The holder of views from the holder pattern
	 * @param cursor The cursor row 
	 */
	private void convertCursorRowToViewOutput(Holder holder,  Cursor cursor) {
		final String destCode = cursor.getString(COL_DESTINATION_CODE);
		final double rate = cursor.getDouble(COL_EXCHANGE_RATE);
		
		final CurrencyResourceMap map  = CurrencyResourceMap.valueOf(destCode.toUpperCase(Locale.US));
		
		final String fullCurrencyName = holder.value.getResources().getString(map.mNameResId);
		final String convertedValue = calculateAndFormat(destCode, rate);
		
		holder.symbol.setText(map.mSymbol);
		holder.value.setText(convertedValue);
		holder.code.setText(destCode);		
		
		int flagResource  = map.mFlagResId;
		
		if (flagResource > 0){
			holder.flag.setImageResource(flagResource);
		}
		
		holder.code.setContentDescription(fullCurrencyName);
		holder.flag.setContentDescription(fullCurrencyName);
		
		return;
	}
	
	/** Calculates values of currencies.
	 * @param destCode The final destination currency.
	 * @param rate The rate to convert at.
	 * @return The formatted currency string
	 */
	private String calculateAndFormat(String destCode, double rate){
		if (rate <= 0){
			//Log.w(LOGTAG, "Unexpected behaviour, rate is \""+rate+"\"");
			rate = 0.0d;
		}
		final Currency currency = Currency.getInstance(destCode);
		return CurrencyCalculator.format(currency, rate * mInputValue);
	}
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Holder pattern
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** The holder pattern for optimization. */
	private class Holder {
		final public TextView symbol;
		final public TextView value;
		final public ImageView flag;  
		final public TextView code;
		public Holder(View view) {
			this.symbol = (TextView) view.findViewById(R.id.currConv_frag_currVal_text_symbol);
			this.value = (TextView) view.findViewById(R.id.currConv_frag_currVal_text_currValue);
			this.flag = (ImageView) view.findViewById(R.id.currConv_frag_currVal_img_currFlag);
			this.code = (TextView) view.findViewById(R.id.currConv_frag_currVal_text_currCode);
		}
	}

}
