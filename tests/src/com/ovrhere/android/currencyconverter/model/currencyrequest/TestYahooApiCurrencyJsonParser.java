package com.ovrhere.android.currencyconverter.model.currencyrequest;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

import com.ovrhere.android.currencyconverter.test.UtilityTestMethods;

import android.content.ContentValues;
import junit.framework.TestCase;

public class TestYahooApiCurrencyJsonParser extends TestCase {
	
	//Source: http://query.yahooapis.com/v1/public/yql/?q=select * from yahoo.finance.xchange where pair in ("USDBBD", "USDCAD", "USDJPY")&env=store://datatables.org/alltableswithkeys&format=json
	//Retrieved: 2015-05-24

	private static final String TEST_JSON_RATES = "{\"query\":{\"count\":3,\"created\":\"2015-05-24T05:17:00Z\",\"lang\":\"en-US\",\"results\":{\"rate\":[{\"id\":\"USDBBD\",\"Name\":\"USD/BBD\",\"Rate\":\"2.0000\",\"Date\":\"5/23/2015\",\"Time\":\"12:55pm\",\"Ask\":\"2.0000\",\"Bid\":\"2.0000\"},{\"id\":\"USDCAD\",\"Name\":\"USD/CAD\",\"Rate\":\"1.2283\",\"Date\":\"5/23/2015\",\"Time\":\"12:55pm\",\"Ask\":\"1.2292\",\"Bid\":\"1.2283\"},{\"id\":\"USDJPY\",\"Name\":\"USD/JPY\",\"Rate\":\"121.5350\",\"Date\":\"5/23/2015\",\"Time\":\"12:55pm\",\"Ask\":\"121.6000\",\"Bid\":\"121.5350\"}]}}}";

	/*
	        {
	          "id": "USDBBD",
	          "Rate": "2.0000",
		...
	        },
	        {
	          "id": "USDCAD",
	          "Rate": "1.2283",
		...
	        },
	        {
	          "id": "USDJPY",
	          "Rate": "121.5350",
		...

		Src	Dst	Rate
		USD	BBD	2.000000
		USD	CAD	1.228300
		USD	JPY	121.535000
				
		Flipped		
		BBD	USD	0.500000
		CAD	USD	0.814133
		JPY	USD	0.008228

	*/
	
	private static final ContentValues[] TEST_EXPECTED_RATES = new ContentValues[] {
		new CodeRatePair("USDBBD",	2.0d).toContentValues(),
		new CodeRatePair("USDCAD",	1.2283d).toContentValues(),
		new CodeRatePair("USDJPY",	121.535d).toContentValues(),

	    new CodeRatePair("BBDUSD",	0.5d).toContentValues(),
	    new CodeRatePair("CADUSD",	0.814133d).toContentValues(),
	    new CodeRatePair("JPYUSD",	0.008228d).toContentValues(),
	};
	

	@Test
	public void testParseJsonStreamInputStream() throws IOException {
		// convert String into InputStream
		final InputStream fakeJsonStream = new ByteArrayInputStream(TEST_JSON_RATES.getBytes());
		
		final int EXPECTED_SIZE = TEST_EXPECTED_RATES.length;
		ContentValues[] results = new YahooApiCurrencyJsonParser().parseJsonStream(fakeJsonStream);
		
		assertEquals("Did not get the expected number of results", EXPECTED_SIZE, results.length);
		
		UtilityTestMethods.validateContentValues("Mismatch found for xml", TEST_EXPECTED_RATES, results);		
	}

}
