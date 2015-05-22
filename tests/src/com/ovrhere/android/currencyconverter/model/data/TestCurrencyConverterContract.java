package com.ovrhere.android.currencyconverter.model.data;

import java.util.Locale;

import org.junit.Test;

import com.ovrhere.android.currencyconverter.model.data.CurrencyConverterContract.ExchangeRateEntry;

import android.net.Uri;
import android.test.AndroidTestCase;

public class TestCurrencyConverterContract extends AndroidTestCase {

	public static final String TEST_CURRENCY1 = "USD";
	
	public static final String TEST_CURRENCY2 = "bbd";
	
	public static final String TEST_CURRENCY3 = "eUr";

	@Test 
    public void testBuildExchangeRateWithSourceCurrency() {
    	Uri srcUri = ExchangeRateEntry.buildExchangeRateWithSourceCurrency(TEST_CURRENCY1);
    	
    	assertNotNull("Error: Null Uri returned. ", srcUri);
    	 
    	assertEquals("Error: Source currency not properly appended to the end of the Uri",
    			TEST_CURRENCY1.toLowerCase(Locale.US), srcUri.getLastPathSegment());
    	
    	assertEquals("Error: Cannot extract the matching currency code from the uri for uppercase",
    			TEST_CURRENCY1.toLowerCase(Locale.US), 
    			ExchangeRateEntry.getSourceCurrencyFromUri(srcUri));
    	
    	Uri srcUri2 = ExchangeRateEntry.buildExchangeRateWithSourceCurrency(TEST_CURRENCY2);
    	assertEquals("Error: Cannot extract the matching currency code from the uri for lowercase",
    			TEST_CURRENCY2.toLowerCase(Locale.US), 
    			ExchangeRateEntry.getSourceCurrencyFromUri(srcUri2));
    }
    
    @Test
    public void testBuildExchangeRateFromSourceToDest() {
    	Uri exchangeUri = 
    			ExchangeRateEntry.buildExchangeRateFromSourceToDest(TEST_CURRENCY2, TEST_CURRENCY3);
    	
    	assertNotNull("Error: Null Uri returned. ", exchangeUri);
    	 
    	assertEquals("Error: Destination currency not properly appended to the end of the Uri",
    			TEST_CURRENCY3.toLowerCase(Locale.US), exchangeUri.getLastPathSegment());
    	
    	assertEquals("Error: Cannot extract the destination currency code from the uri for mix-case",
    			TEST_CURRENCY3.toLowerCase(Locale.US), 
    			ExchangeRateEntry.getDestCurrencyFromUri(exchangeUri));
    	
    	assertEquals("Error: Cannot extract the source currency code from the uri for lower case",
    			TEST_CURRENCY2.toLowerCase(Locale.US), 
    			ExchangeRateEntry.getSourceCurrencyFromUri(exchangeUri));    	
    }

}
