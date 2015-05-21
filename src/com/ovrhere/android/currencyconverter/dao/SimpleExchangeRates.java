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
package com.ovrhere.android.currencyconverter.dao;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map.Entry;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * <p>Provides convenient way to hold exchange rates, in <code>(n x n)</code> 
 * form. </p>
 *  <p>Additionally, provides a non-redundant way to provide exchange rates. 
 *  Using this method instead avoids having <code>(n x n)</code> data to 
 *  instead have <code>(n x (n - 1))/2</code> data; about half as much.</p>
 *  <p>In short: I was just having fun with this class.</p>
 * @author Jason J.
 * @version 0.1.0-20140907
 */
public class SimpleExchangeRates implements Parcelable {
	/** The exception message to give for invalid codes. */
	final static private String DETAILED_EXCEPTION_CURRENCY_CODE_SHORT = 
			"Currency codes incorrect length, cannot be valid; source(%s), target(%s)";
	/** The exception message to give for invalid rates. */
	final static private String DETAILED_EXCEPTION_RATE_INVALID =
			"Rates must be greater than 0, not %d";
	
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// End constants
	////////////////////////////////////////////////////////////////////////////////////////////////
		
	/** The internally stored rates, one rate per pair. */
	private HashMap<CurrencyPair, Double> rates = 
			new HashMap<SimpleExchangeRates.CurrencyPair, Double>();
	/** Keeps count on the number of rates added/removed. 
	 * This count is always divisible by 2. */
	private int rateCount = 0;
	/** Whether or not to use compact form. */
	private boolean compactForm = false;
	
	/** Intializes the exchange rates.
	 * @param compact <code>true</code> will use the compact form.
	 */
	public SimpleExchangeRates(boolean compact) {
		this.compactForm = compact;
	}
	
	/** @return <code>true</code> if it is compact form, <code>false</code> 
	 * if not.	 */
	public boolean isCompactForm() {
		return compactForm;
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// End constructors
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** Adds or updates a rate based upon the source currency code & target
	 * currency code. If a counter example is found, this the value updated.
	 * Same as calling with <code>force == false</code>.
	 * @param sourceCode The source currency code (ISO 4217, 3 letters)
	 * @param targetCode The target/destination currency code (ISO 4217, 3 letters)
	 * @param rate The rate to give from <code>source</code> to <code>targetCode</code.
	 * Must be &gt; 0.
	 * @throws IllegalArgumentException If the currency codes supplied are
	 * of inappropriate length or the rate is <= 0.
	 */
	public void addRate(String sourceCode, String targetCode, double rate){
		addRate(sourceCode, targetCode, rate, false);
	}
	
	/** Adds or updates a rate based upon the source currency code & target
	 * currency code. If a counter example is found, this the value updated.
	 * @param sourceCode The source currency code (ISO 4217, 3 letters)
	 * @param targetCode The target/destination currency code (ISO 4217, 3 letters)
	 * @param rate The rate to give from <code>source</code> to <code>targetCode</code.
	 * Must be &gt; 0.	 * 
	 * @param force <code>true</code> to force adding it for
	 * source -> target ONLY, <code>false</code> to automatically add complement
	 * (target->source 1/rate)
	 * @throws IllegalArgumentException If the currency codes supplied are
	 * of inappropriate length or the rate is <= 0.
	 */
	public void addRate(String sourceCode, String targetCode, double rate, 
			boolean force){
		validateCurrencyCodes(sourceCode, targetCode);
		if (rate <= 0){
			throw new IllegalArgumentException(
					String.format(DETAILED_EXCEPTION_RATE_INVALID, 
					rate)
				);
		}		
		if (compactForm && !force ){
			compactAdd(sourceCode, targetCode, rate);
		} else {	
			nonCompactAdd(sourceCode, targetCode, rate, force);
			
		}
	}


	
	/**
	 * Removes the suggested rate from the list. Note that in compact mode
	 * this will also remove its complement (target -> source) rate.
	 * @param sourceCode The source currency code (ISO 4217, 3 letters)
	 * @param targetCode The target/destination currency code (ISO 4217, 3 letters)
	 * @return The removed rate or -1.0 if no rate is found. 
	 * @throws IllegalArgumentException If the currency codes supplied are
	 * of inappropriate length  */
	public double removeRate(String sourceCode, String targetCode){
		validateCurrencyCodes(sourceCode, targetCode);
		if (compactForm){
			return compactRemove(sourceCode, targetCode);
		}
		return nonCompactRemove(sourceCode, targetCode);
	}

	/** Removes rate for non-compact form. 
	 * @param sourceCode
	 * @param targetCode
	 * @return The removed rate or -1.0 if no rate is found.
	 */
	private double nonCompactRemove(String sourceCode, String targetCode) {
		Double value = rates.remove(new CurrencyPair(sourceCode, targetCode));
		if (value != null){
			decrementCount();
			return value.doubleValue();
		}
		return -1.0;
	}

	/** Removes the form or it's complement in non-compact form.
	 * @param sourceCode
	 * @param targetCode
	 * @return The removed rate or -1.0 if no rate is found.
	 */
	private double compactRemove(String sourceCode, String targetCode) {
		Double value = rates.remove(new CurrencyPair(sourceCode, targetCode));
		if (value != null){
			decrementCount();
			return value.doubleValue();
		}
		//if we are still here, why not try the counter pair?
		value = rates.remove(new CurrencyPair(targetCode, sourceCode));
		if (value != null){
			decrementCount();
			return 1.0/value.doubleValue();
		}
		return -1.0; //return the in invalid value.		
	}
	
	/** Gets the exchange rate from source -> target. 
	 * @param sourceCode The source currency code (ISO 4217, 3 letters)
	 * @param targetCode The target/destination currency code (ISO 4217, 3 letters)
	 * @return The exchange rate from source -> target or 0.0 if not found.
	 * @throws IllegalArgumentException If the currency codes supplied are
	 * of inappropriate length
	 */
	public double getRate(String sourceCode, String targetCode){
		validateCurrencyCodes(sourceCode, targetCode);
		
		if (compactForm){
			return compactGet(sourceCode, targetCode);
		}
			
		return nonCompactGet(sourceCode, targetCode);
		
	}

	
	/** Returns the size of the hashmap */
	protected int getSize(){
		return rates.size();
	}
	/** Returns count of all possible rates.
	 * @return The number of possible exchanges rates found.	 */
	public int getRateCount(){
		/* To get the number of rates, i.e. n x n, 
		 * one must deduce n from: (n x (n - 1))/2 = size
		 * OR count them. So we count them.
		 */
		return rateCount;
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Helper methods
	////////////////////////////////////////////////////////////////////////////////////////////////
	/** Adds records in duplicate pairs unless <code>force</code> 
	 * is set to <code>true</code>
	 * @param sourceCode The source currency code
	 * @param targetCode The target currency code
	 * @param rate The already validated rate
	 * @param force <code>true</code> to force adding it for
	 * source -> target ONLY, <code>false</code> to automatically add complement
	 * (target->source 1/rate)
	 */
	private void nonCompactAdd(String sourceCode, String targetCode,
			double rate, boolean force) {
		rates.put(
				new CurrencyPair(sourceCode, targetCode), 
				Double.valueOf(rate));
		if (!force){
			rates.put( 
					new CurrencyPair(targetCode, sourceCode), 
					Double.valueOf(1.0/rate));
		}
		incrementCount(force);
	}
	/** Adds a single record and ensures only the single record is added.
	 * Complements will be calculated if they exist.
	 * @param sourceCode The source currency code
	 * @param targetCode The target currency code
	 * @param rate The already validated rate
	 */
	private void compactAdd(String sourceCode, String targetCode, double rate) {
		CurrencyPair pair = new CurrencyPair(sourceCode, targetCode);
		if (rates.containsKey(pair)){ //update
			rates.put(pair, Double.valueOf(rate));
			incrementCount();
			return;
		} //do not create pair2 unless required.
		CurrencyPair counterPair = new CurrencyPair(targetCode, sourceCode);
		if (rates.containsKey(counterPair)){ //update
			rates.put(counterPair, Double.valueOf(1.0/rate));
		} else { //new pair
			rates.put(pair, Double.valueOf(rate));
		}
		incrementCount();
	}
	
	/** Get rate if not using compact form. Pretty straight forward
	 * @param sourceCode 
	 * @param targetCode
	 * @return The exchange rate from source -> target or 0.0 if not found.
	 */
	private double nonCompactGet(String sourceCode, String targetCode) {
		Double value = rates.get(new CurrencyPair(sourceCode, targetCode));
		if (value != null){
			return value.doubleValue();
		} 
		return 0.0; //no value found.
	}

	/** Gets rate if using compact form. This involves calculating.
	 * @param sourceCode
	 * @param targetCode
	 * @return The exchange rate from source -> target or 0.0 if not found.
	 */
	private double compactGet(String sourceCode, String targetCode) {
		Double value = rates.get(new CurrencyPair(sourceCode, targetCode));
		if (value != null){ 
			return value.doubleValue();
		} 
		//if we are still here, why not try the counter pair?
		value = rates.get(new CurrencyPair(targetCode, sourceCode));
		if (value != null){ 
			return 1.0/value.doubleValue();
		} 
		return 0.0; //give up and return nothing.
	}
	
	
	/** Increments count by 2. */
	private void incrementCount(){
		incrementCount(false);
	}
	/** Increments count by 1 or 2.
	 * @param single increment by 1 if true, by 2 if false.	 */
	private void incrementCount(boolean single){
		rateCount += single ? 1 : 2;		
	}
	/** Decrements count until the minimum point of 0. */
	private void decrementCount(){
		rateCount -= 2;
		if (rateCount < 0){
			rateCount = 0;
		}
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Utility functions
	////////////////////////////////////////////////////////////////////////////////////////////////
	/** Validates the currency codes. If incorrect length, throws exception. 
	 * @throws IllegalArgumentException If the currency codes are not length 3. */
	static private void validateCurrencyCodes(String sourceCode, String targetCode) {
		if (sourceCode.length() != 3 || targetCode.length() != 3){
			throw new IllegalArgumentException(
					String.format(DETAILED_EXCEPTION_CURRENCY_CODE_SHORT, 
							sourceCode, targetCode)
					);
		}
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Internal classes
	////////////////////////////////////////////////////////////////////////////////////////////////
			
	/** Doa for the purposes of indexing the internal hashmap. 
	 * @author Jason J.
	 * @version 0.1.0-20140907 */		
	static private class CurrencyPair {
		final protected String sourceCode;
		final protected String targetCode;
		
		public CurrencyPair(String source, String target) {
			this.sourceCode = source.toUpperCase(Locale.US);
			this.targetCode = target.toUpperCase(Locale.US);
		}
		
		@Override
		public String toString() {
			return super.toString() + 
					String.format("[source:%s,target:%s)", 
									sourceCode, targetCode);
		}

	    @Override
	    public int hashCode() {
	    	return Arrays.hashCode(
	    			new Object[]{
	    					this.sourceCode, this.targetCode.hashCode()});
	    }

	    @Override
	    public boolean equals(Object obj) {
	        if (this == obj){
	            return true;
	        }
	        if (obj == null){
	            return false;
	        }
	        if (getClass() != obj.getClass()){
	            return false;
	        }
	        CurrencyPair other = (CurrencyPair) obj;
	        //if both are equal, true. Otherwise false.
	        return sourceCode.equals(other.sourceCode) && 
	        		targetCode.equals(other.targetCode);
	    }
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Parcelable details
	////////////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public int describeContents() {
		return 0;
	}
	
	@Override
	public void writeToParcel(Parcel out, int flags) {
		//all booleans
		out.writeBooleanArray(new boolean[]{compactForm});
		
		//all hashaps, i.e. strings + doubles
		final int doublesCount = rates.size();
		String[] strings = new String[doublesCount*2];
		double[] doubles = new double[doublesCount];
		int sIndex = 0;
		int dIndex = 0;
		for (Entry<CurrencyPair, Double> rate : rates.entrySet()) {
			strings[sIndex++] = rate.getKey().sourceCode; //alternate: evens
			strings[sIndex++] = rate.getKey().targetCode; //odds
			doubles[dIndex++] = rate.getValue().doubleValue();
		}
		//hash-map convertered to arrays
		//storing
		out.writeStringArray(strings);
		out.writeDoubleArray(doubles);
		
		//all ints; rate count and the size of doubles.
		out.writeIntArray(new int[]{rateCount, doublesCount});
		
		//TODO redo hashmap using bundle
	}
	
	/** Constructor for use with parcellable #CREATOR. 
	* Coupled to {@link #writeToParcel(Parcel, int)}. */
	private SimpleExchangeRates(Parcel in){
		//reset ints
		int ints[] = new int[2];
		in.readIntArray(ints);
		rateCount = ints[0];
		final int doublesCount = ints[1];
		
		//reset booleans
		boolean bools[] = new boolean[1];
		in.readBooleanArray(bools);
		compactForm = bools[0];
		
		//reset hashmap: doubles, strings, etc
		double doubles[] = new double[doublesCount];
		String strings[] = new String[doublesCount*2];
		int dIndex = 0;
		for (int index = 0; index < strings.length; index+=2) {
			rates.put(new CurrencyPair (strings[index], strings[index+1]),
					Double.valueOf(doubles[dIndex++]));
		}
		
	}
	
	/** Creator used with parcellable interface. */
	public static final Parcelable.Creator<SimpleExchangeRates> CREATOR
			= new Parcelable.Creator<SimpleExchangeRates>() {
		@Override
		public SimpleExchangeRates createFromParcel(Parcel in) {
			return new SimpleExchangeRates(in);
		}
		@Override
		public SimpleExchangeRates[] newArray(int size) {
			return new SimpleExchangeRates[size];
		}
	};
}
