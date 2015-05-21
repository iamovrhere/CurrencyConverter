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
package com.ovrhere.android.currencyconverter.oldmodel.dao;

import java.text.ParseException;
import java.util.Currency;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map.Entry;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import com.ovrhere.android.currencyconverter.utils.Timestamp;
/**
 * The data access object for currency data records. 
 * 
 * @author Jason J.
 * @version 0.4.0-20140907
 */ 
@Deprecated
public class CurrencyData implements Parcelable {
	/** The current ISO standard for this currency based upon the currency code. 
	 * Default is <code>null</code>. */
	protected Currency isoCurrency = null;
	
	/** The record id. */
	protected int _id = -1;	
	/** The 1 character currency symbol. Default is null. */
	protected String currencySymbol = null;
	/** The 3 letter currency code. Default is null. */
	protected String currencyCode = null;
	/** The full currency name. Default is null. */
	protected String currencyName = null;
	
	/** The exchange rate from this currency to the keys of this map. */ 
	protected HashMap<String, Double> exchangeRates = 
			new HashMap<String, Double>();	
		
	/** The flag image resource of currency. Default is -1. */
	protected int flagImageResourceId = -1;
	/** The warning that the currency may opt to carry. Default is null. */
	protected String warning = null;
	/** The last modified date of the currency. Default null. */
	protected Date modifiedDate = null;
	/** The last modified date of the currency. Default null. */
	protected String modifiedTimestamp = null;
	
	protected CurrencyData() {}
	
	@Override
	public String toString() {
		//useful in debugging.
		return super.toString() + 
				"[code: "+currencyCode+", name: "+currencyName+"]";
	}
	
	/** @param drawableId The flag image resource id for the currency. */
	public void setFlagResource(int drawableId) {
		this.flagImageResourceId = drawableId;
	}
	
	/** Updates the exchange rates with additonal rates. Note that this method
	 * CANNOT clear rates, but only add/overwrite existing ones, such that:
	 * <p><i>Before:</i>
	 * <ul><li>USD -> 2.0</li><li>CAD -> 2.1</li></ul>
	 * <i>After:</i>
	 * <ul><li>USD -> 1.9</li><li>CAD -> 2.1</li><li>EUR -> 1.2</li></ul> 
	 * </ul>
	 * </p>
	 * @param rates The rates to append to the existing list. Cannot be null.
	 * Cannot contain zero values.
	 */
	public void updateRates(HashMap<String, Double> rates){
		if (rates == null){
			throw new NullPointerException();
		}
		Builder.validateRates(rates);
		this.exchangeRates.putAll(rates);
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Start accessors
	////////////////////////////////////////////////////////////////////////////////////////////////
	/** @return The record id. Default is -1. */ 
	public int getId() {
		return _id;
	}
	/** @return The 3 letter currency code for this currency (e.g. "CAD). */ 
	public String getCurrencyCode() {
		return currencyCode;
	}
	/** @return The currency symbol (e.g. $) */
	public String getCurrencySymbol() {
		return currencySymbol;
	}
	
	/** Gets the Android currency object for this currency.
	 * @return The ISO 4217 {@link Currency} object.	 */
	public Currency getCurrency(){
		return isoCurrency;
	}
	/** @return The full name of the currency (e.g. "Euro").  */
	public String getCurrencyName() {
		return currencyName;
	}
	/** @returns The flag drawable resource id for the currency. 
	 *  Default is -1. */ 
	public int getFlagResource() {
		return flagImageResourceId;
	}
	/** @deprecated Use {@link #getRate(String)} and, if necessary, 
	 * <code>1.0f / getRate("USD") </code>.
	 * @return The exchange rate from USD -> this currency. 
	 *  Default is 0.0. */
	@Deprecated
	public float getRateFromUSD() {
		Double dRate = exchangeRates.get("USD");
		if (dRate != null){
			return 1.0f/dRate.floatValue();
		}
		return 0.0f;
	}
	/** Returns the rate from this currency -> targetCurrency
	 * @param targetCurrencyCode The targeted currency. Should be of
	 * ISO 4217 form.
	 * @return The exchange rate from this currency -> target, or 0.0d
	 * if not found.	 */
	public double getRate(String targetCurrencyCode){
		Double dRate = exchangeRates.get(targetCurrencyCode.toUpperCase(Locale.US));
		if (dRate != null){
			return dRate.doubleValue();
		} 
		if (currencyCode.equalsIgnoreCase(targetCurrencyCode)){
			return 1.0d; //if we are the same, but not set, assume the same.
		}
		return 0.0d;
	}
	/** Returns the map of all rates.
	 * @return The hashmap of all target currency rates (i.e. targets are keys)
	 */
	public HashMap<String, Double> getRates(){
		return exchangeRates;
	}

	/** @return A warning associate with this currency.
	 *  Default is <code>null</code>. */ 
	public String getWarning() {
		return warning;
	}
	/** @return The last time this record was modified
	 *  Default is <code>null</code>. */
	public Date getModifiedDate() {
		return modifiedDate;
	}
	/** @return The timestamp when this record was modified. 
	 * Default is <code>null</code>. */
	public String getModifiedTimestamp(){
		return modifiedTimestamp;
	}
		
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// End accessors
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * The builder for the currency data records. 
	 * @author Jason J.
	 * @version 0.2.0-20140908
	 */
	public static class Builder {
		final static private String DETAILED_EXCEPTION_RATE_GREATER_THAN_ZERO = 
				"Exchange rate must be >= 0";
		final static private String DETAILED_EXCEPTION_BUILDER_IS_NOT_PREPARED = 
				"Insufficient details to build currency.";
		final static private String DETAILED_EXCEPTION_SYMBOL_NONEMPTY = 
				"Currency symbol cannot be empty";		
		final static private String DETAILED_EXCEPTION_NAME_NONEMPTY = 
				"Currency name cannot be empty";
		final static private String DETAILED_EXCEPTION_CODE_INVALID = 
				"Currency code must be 3 chars long.";
		
		/** If ready to build. */
		private boolean readyToBuild = false;		
		private CurrencyData currencyData = new CurrencyData();
		public Builder() {}
		/** @param id The record id. 
		 * @return {@link Builder} for chaining. */
		public Builder setId(int id){
			currencyData._id = id;
			return this;
		}
		
		/** Required. Sets currency information.
		 * @deprecated  Use {@link #setCurrency(String, String, String, HashMap)} 
		 * instead.
		 * @param symbol Cannot be empty.
		 * @param code The 3 letter currency code (e.g. GBP)
		 * Cannot be blank.
		 * @param name The proper name of the currency (e.g. Great British Pound).
		 * Cannot be blank.
		 * @param rate The exchange rate from USD to this currency. 
		 * Must be >= 0.
		 * @return {@link Builder} for chaining. */
		@Deprecated
		public Builder setCurrency(String symbol, String code, String name, float rate){
			validateCurrencyStrings(symbol, code, name);
			currencyData.currencySymbol = symbol;
			return setCurrency(code, name, rate);
		}
		
		/** Required. Sets currency information.
		 * instead.
		 * @param symbol Cannot be empty.
		 * @param code The 3 letter currency code (e.g. GBP) according to ISO 4217.
		 * @param name The proper name of the currency (e.g. Great British Pound).
		 * Cannot be blank.
		 * @return {@link Builder} for chaining. */
		public Builder setCurrency(String symbol, String code, String name){
			validateCurrencyStrings(symbol, code, name);
			currencyData.isoCurrency = Currency.getInstance(code);
			//all valid data.
			currencyData.currencyCode = code.trim().toUpperCase(Locale.US);
			currencyData.currencyName = name.trim();
			currencyData.currencySymbol = symbol.trim();
			this.readyToBuild = true;
			return this;
		}
		/** Sets the exchange rates.
		 * @param rates Non-null list of rates.
		 * @return {@link Builder} for chaining.  */
		public Builder setExchangeRates(HashMap<String, Double> rates){
			if (rates == null){
				throw new NullPointerException();
			}
			validateRates(rates);
			currencyData.exchangeRates.putAll(rates);
			return this;
		}
		
		/** @param resId The drawable resource id for the flag
		 * @return {@link Builder} for chaining. */
		public Builder setFlagResource(int resId){
			currencyData.flagImageResourceId = resId;			
			return this;
		}
		/** @param warning A warning for the currency.
	 	 * @return {@link Builder} for chaining. */
		public Builder setWarning(String warning){
			currencyData.warning = warning.trim();
			return this;
		}
		/** @param timestamp The timestamp for last modified.  
		 * @return {@link Builder} for chaining. 
		 * @throws ParseException If the timestamp is invalid. */
		public Builder setModifiedTimestamp(String timestamp) throws ParseException{
			currencyData.modifiedDate = Timestamp.parse(timestamp);
			currencyData.modifiedTimestamp = timestamp;
			return this;
		}
		/** Creates the currency data object. 
		 * @throws IllegalStateException If the builder is not ready to build. */
		public CurrencyData create(){
			if (readyToBuild){
				return currencyData;
			} else {
				throw new IllegalStateException(DETAILED_EXCEPTION_BUILDER_IS_NOT_PREPARED);
			}
		}
		
		/////////////////////////////////////////////////////////////////////////////////////////////////
		/// Helper + Utility methods
		////////////////////////////////////////////////////////////////////////////////////////////////
		
		/** Validates the currency strings and ensures them to be within the 
		 * restricted params.
		 * @param symbol Symbol must be non-empty
		 * @param code Code must be length 3
		 * @param name Name must be non-empty.
		 * @throws IllegalArgumentException If any of the currency data is invalid
		 */
		static private void validateCurrencyStrings(String symbol, String code,
				String name) {
			if (code.length() != 3){
				throw new IllegalArgumentException(DETAILED_EXCEPTION_CODE_INVALID);
			}
			if (name.isEmpty()){
				throw new IllegalArgumentException(DETAILED_EXCEPTION_NAME_NONEMPTY);
			}
			if (symbol.isEmpty()){
				throw new IllegalArgumentException(DETAILED_EXCEPTION_SYMBOL_NONEMPTY);
			}
		}
		/** Validates the rates and ensures non are less than zero. */
		static private void validateRates(HashMap<String, Double> rates){
			for (Entry<String, Double> entry : rates.entrySet()) {
				if (entry.getValue() <= 0){
					throw new IllegalArgumentException(
							DETAILED_EXCEPTION_RATE_GREATER_THAN_ZERO
							+"[Code: " + entry.getKey()+","+
							entry.getValue()+"]");
				}
			}
		}
		
		/** Sets currency information. 
		 * @param symbol The symbol related to currency (e.g. $)
		 * Cannot be blank.
		 * @param code The 3 letter currency code (e.g. GBP)
		 * Cannot be blank.
		 * @param name The proper name of the currency (e.g. Great British Pound).
		 * Cannot be blank.
		 * @param rate The exchange rate from USD to this currency. 
		 * Must be >= 0.
		 * @return {@link Builder} for chaining. 
		 * @throws IllegalArgumentException If the arguments passed are empty
		 * or if the currency code is not a supported ISO 4217 currency  . */
		@Deprecated
		private Builder setCurrency(String code, String name, float rate){
			if (rate < 0){
				throw new IllegalArgumentException(DETAILED_EXCEPTION_RATE_GREATER_THAN_ZERO);
			}
			currencyData.isoCurrency = Currency.getInstance(code);
			//all valid data.
			currencyData.currencyCode = code.trim().toUpperCase(Locale.US);
			currencyData.currencyName = name.trim();
			currencyData.exchangeRates.put("USD", Double.valueOf(1.0f/rate));
			readyToBuild = true;
			return this;
		}
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Parcellable details
	////////////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public int describeContents() {
        return 0;
    }

	@Override
    public void writeToParcel(Parcel out, int flags) {
        //all ints
		out.writeIntArray(new int[]{_id, flagImageResourceId});
		
		//all strings
		out.writeStringArray(new String[]{
				currencyCode,
				currencyName,
				currencySymbol,
				modifiedTimestamp
		});
		//modifiedDate will be rebuilt.
		
		Bundle extras = new Bundle();  
			extras.putSerializable(
				CurrencyData.class.getName()+".exchangeRates", exchangeRates);
		out.writeBundle(extras);	
    }
	
	/** Constructor for use with parcellable #CREATOR. 
	 * Coupled to {@link #writeToParcel(Parcel, int)}. */
	@SuppressWarnings("unchecked")
	private CurrencyData(Parcel in){
		int[] ints = new int[2];
		String[] strings = new String[4];
		
		//reset ints
		in.readIntArray(ints);
		_id = ints[0];
		flagImageResourceId = ints[1];
				
		//reset strings, etc
		in.readStringArray(strings);
		currencyCode = strings[0];
		currencyName = strings[1];
		currencySymbol = strings[2];
		try {
			if (strings[3] != null){
				modifiedDate = Timestamp.parse(strings[3]);
				modifiedTimestamp = strings[3];
			}
		} catch (ParseException e) {
			//should never throw.
		}
		
		//reset hashmap rates
		Bundle extras = in.readBundle();  
		exchangeRates.putAll(
				(HashMap<String, Double>) extras.getSerializable(
						CurrencyData.class.getName()+".exchangeRates")
						);
		
		//reintialize
		isoCurrency = Currency.getInstance(currencyCode);
	}

    /** Creator used with parcellable interface. */
	public static final Parcelable.Creator<CurrencyData> CREATOR
		    	= new Parcelable.Creator<CurrencyData>() {
		@Override
		public CurrencyData createFromParcel(Parcel in) {
		    return new CurrencyData(in);
		}
		@Override
		public CurrencyData[] newArray(int size) {
		    	return new CurrencyData[size];
			}
		};
}
