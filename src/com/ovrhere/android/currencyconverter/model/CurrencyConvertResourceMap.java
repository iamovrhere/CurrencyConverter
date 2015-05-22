package com.ovrhere.android.currencyconverter.model;

import com.ovrhere.android.currencyconverter.R;

/**
 * <p>Produces a simple map to resources so that the database can easily map to
 * translatable resources.</p>
 * <p>Note:
 * <ul>
 * <li>Use {@link CurrencyConvertResourceMap#ordinal()} to get each index</li>
 * <li>Use {@link CurrencyConvertResourceMap#values()} to get an array of all enums</li>
 * <li>Use {@link CurrencyConvertResourceMap#valueOf(String)} (with uppercase) to get the 
 * value of a given currency code. </li>
 * </ul>
 * </p>
 * @author Jason J.
 * @version 0.1.0-20150521
 */
public enum CurrencyConvertResourceMap {
	//WARNING! If you change the order of these, update the Database version.
	CAD(R.string.currConv_CAD_name, R.drawable.ic_flag_cad), //0
	EUR(R.string.currConv_EUR_name, R.drawable.ic_flag_eur), //1
	GBP(R.string.currConv_GBP_name, R.drawable.ic_flag_gbp), //2
	JPY(R.string.currConv_JPY_name, R.drawable.ic_flag_jpy), //3
	USD(R.string.currConv_USD_name, R.drawable.ic_flag_usd); //4
	
	/** The resource for the flag drawable. */
	final public int mFlagResId;
	/** The resource for the currency name. */
	final public int mNameResId;

    private CurrencyConvertResourceMap(int currencyNameId, int flagId) {
        this.mFlagResId = flagId; 
        this.mNameResId = currencyNameId;   
    }    
}
