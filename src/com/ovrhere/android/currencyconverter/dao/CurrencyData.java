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
package com.ovrhere.android.currencyconverter.dao;

import java.text.ParseException;
import java.util.Currency;
import java.util.Date;

import android.os.Parcel;
import android.os.Parcelable;

import com.ovrhere.android.currencyconverter.utils.Timestamp;
/**
 * The data access object for currency data records. 
 * 
 * @author Jason J.
 * @version 0.3.0-20140903
 */
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
	/** The exchange rate from currency from usd to currency. */ 
	protected float rateFromUSD = 0.0f;
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
	/** @return The exchange rate from USD -> this currency. 
	 *  Default is 0.0. */
	public float getRateFromUSD() {
		return rateFromUSD;
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
	 * @version 0.1.1-20140903
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
		final static private String DETAILED_EXCEPTION_CODE_NONEMPTY = 
				"Currency code cannot be empty";
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
		private Builder setCurrency(String code, String name, float rate){
			if (code.isEmpty()){
				throw new IllegalArgumentException(DETAILED_EXCEPTION_CODE_NONEMPTY);
			}
			if (name.isEmpty()){
				throw new IllegalArgumentException(DETAILED_EXCEPTION_NAME_NONEMPTY);
			}
			if (rate < 0){
				throw new IllegalArgumentException(DETAILED_EXCEPTION_RATE_GREATER_THAN_ZERO);
			}
			currencyData.isoCurrency = Currency.getInstance(code);
			//all valid data.
			currencyData.currencyCode = code.trim();
			currencyData.currencyName = name.trim();			
			currencyData.rateFromUSD =  rate;
			readyToBuild = true;
			return this;
		}
		/** Required. Sets currency information. 
		 * @param symbol Currently. Not used.
		 * @param code The 3 letter currency code (e.g. GBP)
		 * Cannot be blank.
		 * @param name The proper name of the currency (e.g. Great British Pound).
		 * Cannot be blank.
		 * @param rate The exchange rate from USD to this currency. 
		 * Must be >= 0.
		 * @return {@link Builder} for chaining. */
		public Builder setCurrency(String symbol, String code, String name, float rate){
			if (symbol.isEmpty()){
				throw new IllegalArgumentException(DETAILED_EXCEPTION_SYMBOL_NONEMPTY);
			}
			currencyData.currencySymbol = symbol;
			return setCurrency(code, name, rate);
		}
		/*public Builder setRateFromUSD(float rate){
			currencyData.rateToUSD =  rate;
			return this;
		}*/
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
		//all floats
		out.writeFloat(rateFromUSD);
		//all strings
		out.writeStringArray(new String[]{
				currencyCode,
				currencyName,
				currencySymbol,
				modifiedTimestamp
		});
		//modifiedDate will be rebuilt.
    }
	
	/** Constructor for use with parcellable #CREATOR. 
	 * Coupled to {@link #writeToParcel(Parcel, int)}. */
	private CurrencyData(Parcel in){
		int[] ints = new int[2];
		String[] strings = new String[4];
		
		//reset ints
		in.readIntArray(ints);
		_id = ints[0];
		flagImageResourceId = ints[1];
		
		//reset floats
		rateFromUSD = in.readFloat();
		
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
