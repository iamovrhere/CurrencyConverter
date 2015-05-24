package com.ovrhere.android.currencyconverter.model.currencyrequest;

import org.junit.Test;

import com.ovrhere.android.currencyconverter.model.data.CurrencyConverterContract.ExchangeRateEntry;

import android.content.ContentValues;
import junit.framework.TestCase;

public class TestCodeRatePair extends TestCase {

	@Test
	public void testCodeRatePair() {
		final double rate = 0.8123d;
		final String source = "USD";
		final String dest = "CAD";
		CodeRatePair validPair = new CodeRatePair(source+dest, rate);
		
		assertEquals("Expected a USD source", source, validPair.srcCode);
		assertEquals("Expected a CAD destination", dest, validPair.destCode);
		assertEquals("Expected a different rate", rate, validPair.rate);
		
		try {
			CodeRatePair invalidPair = new CodeRatePair("USDnope", 3.14159d);
			assertFalse("Did not expect pair to parse:" + invalidPair.toString(), true);
		} catch (IllegalArgumentException expected){
			//nothing to do.
		}		
	}
	

	@Test
	public void testToContentValues() {
		final double rate = 0.5d;
		final String source = "USD";
		final String dest = "BBD";
		
		CodeRatePair usdBbdPair = new CodeRatePair(source+dest, rate);
		ContentValues cv = usdBbdPair.toContentValues();
		
		assertEquals("Expected source column to be set to source", 
				source, cv.getAsString(ExchangeRateEntry.COLUMN_SOURCE_CURRENCY_CODE));
		assertEquals("Expected dest column to be set to dest", 
				dest, cv.getAsString(ExchangeRateEntry.COLUMN_DEST_CURRENCY_CODE));
		assertEquals("Expected the exchange rate to stay the same", 
				rate, cv.getAsDouble(ExchangeRateEntry.COLUMN_EXCHANGE_RATE));
	}

	@Test
	public void testToReverseContentValues() {
		final double rate = 0.81d;
		final String source = "USD";
		final String dest = "CAD";
		
		CodeRatePair usdCadPair = new CodeRatePair(source+dest, rate);
		ContentValues cv = usdCadPair.toReverseContentValues();
		
		assertEquals("Expected source column to be set to dest", 
				dest, cv.getAsString(ExchangeRateEntry.COLUMN_SOURCE_CURRENCY_CODE));
		assertEquals("Expected dest column to be set to source", 
				source, cv.getAsString(ExchangeRateEntry.COLUMN_DEST_CURRENCY_CODE));
		// CAD -> USD == 1.234567901d == 1/0.81d
		assertEquals("Expected the exchange rate to change", 
				1.234568d, cv.getAsDouble(ExchangeRateEntry.COLUMN_EXCHANGE_RATE));
	}

}
