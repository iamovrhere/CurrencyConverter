package com.ovrhere.android.currencyconverter.model.currencyrequest;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.ovrhere.android.currencyconverter.model.data.CurrencyConverterContract.ExchangeRateEntry;
import com.ovrhere.android.currencyconverter.test.UtilityTestContentObserver;
import com.ovrhere.android.currencyconverter.test.UtilityTestMethods;

import android.content.ContentValues;
import android.database.Cursor;
import android.test.AndroidTestCase;

public class TestYahooApiExchangeRatesUpdate extends AndroidTestCase {

		//Source: http://query.yahooapis.com/v1/public/yql/?q=select * from yahoo.finance.xchange where pair in ("USDCAD", "USDJPY", "CADJPY")&env=store://datatables.org/alltableswithkeys&format=json
		//Retrieved: 2015-05-25

		private static final String TEST_JSON_RATES = "{\"query\":{\"count\":3,\"created\":\"2015-05-25T14:04:56Z\",\"lang\":\"en-US\",\"results\":{\"rate\":[{\"id\":\"USDCAD\",\"Name\":\"USD/CAD\",\"Rate\":\"1.2315\",\"Date\":\"5/25/2015\",\"Time\":\"3:04pm\",\"Ask\":\"1.2316\",\"Bid\":\"1.2315\"},{\"id\":\"USDJPY\",\"Name\":\"USD/JPY\",\"Rate\":\"121.5050\",\"Date\":\"5/25/2015\",\"Time\":\"3:04pm\",\"Ask\":\"121.5200\",\"Bid\":\"121.5050\"},{\"id\":\"CADJPY\",\"Name\":\"CAD/JPY\",\"Rate\":\"98.6602\",\"Date\":\"5/25/2015\",\"Time\":\"3:04pm\",\"Ask\":\"98.6844\",\"Bid\":\"98.6360\"}]}}}";

		/*
				...
	        {
	          "id": "USDCAD",
	          "Rate": "1.2315",
				...
	        },
	        {
	          "id": "USDJPY",
	          "Rate": "121.5050",
				...
	        },
	        {
	          "id": "CADJPY",
	          "Rate": "98.6602",
				...
	        }

			Src	Dst	Rate
			USD	CAD	1.2315
			USD	JPY	121.505
			CAD	JPY	98.6602
					
			CAD	USD	0.812018
			JPY	USD	0.008230
			JPY	CAD	0.010136


		*/
		
	private static final ContentValues[] TEST_EXPECTED_RATES = new ContentValues[] {
		new CodeRatePair("CADJPY",	98.6602d).toContentValues(),
		new CodeRatePair("CADUSD",	0.812018d).toContentValues(),
		
		new CodeRatePair("JPYCAD",	0.010136d).toContentValues(),
	    new CodeRatePair("JPYUSD",	0.008230d).toContentValues(),
	    
	    new CodeRatePair("USDCAD",	1.2315d).toContentValues(),
	    new CodeRatePair("USDJPY",	121.505d).toContentValues()
	};
	
	private static final String[] TEST_CURRENCIES = new String[]{"USD", "CAD", "JPY"};
		
	
	@Before
	public void setUp() throws Exception {
		super.setUp();
		mContext.getContentResolver().delete(
                ExchangeRateEntry.CONTENT_URI,
                null,
                null
        );
	}
	
	@After
	protected void tearDown() throws Exception {
		super.tearDown();
		mContext.getContentResolver().delete(
                ExchangeRateEntry.CONTENT_URI,
                null,
                null
        );
	}

	@Test
	public void testOnlineRun() {
		UtilityTestContentObserver observer = UtilityTestContentObserver.newTestContentObserver();
		mContext.getContentResolver()
   				.registerContentObserver(ExchangeRateEntry.CONTENT_URI, true, observer);
		
		YahooApiExchangeRatesUpdate update = 
				new YahooApiExchangeRatesUpdate(
						mContext.getContentResolver(),
						TEST_CURRENCIES,
						true);
		
		update.run(); //As a unit test, will fail if the internet connection is poor
		
		observer.waitForNotificationOrFail();
		mContext.getContentResolver().unregisterContentObserver(observer);
		
		Cursor cursor = mContext.getContentResolver()
				.query(ExchangeRateEntry.CONTENT_URI, 
						null, //all columns
						null, //select
						null, //select args
						null //sort by
						);
		
		final int EXPECTED_COUNT = TEST_CURRENCIES.length * 2;
		
		assertEquals("Did not get the number of records expected", EXPECTED_COUNT, cursor.getCount());
	}

	@Test
	public void testOnStart() {
		// convert String into InputStream
		final InputStream fakeJsonStream = new ByteArrayInputStream(TEST_JSON_RATES.getBytes());
		
		UtilityTestContentObserver observer = UtilityTestContentObserver.newTestContentObserver();
		mContext.getContentResolver()
   				.registerContentObserver(ExchangeRateEntry.CONTENT_URI, true, observer);

		YahooApiExchangeRatesUpdate update = 
				new YahooApiExchangeRatesUpdate(
						mContext.getContentResolver(),
						TEST_CURRENCIES,
						true);
		update.onStart(fakeJsonStream);
		
		observer.waitForNotificationOrFail();
		mContext.getContentResolver().unregisterContentObserver(observer);
		   
		Cursor cursor = mContext.getContentResolver()
				.query(ExchangeRateEntry.CONTENT_URI, 
						null, //all columns
						null, //select
						null, //select args
						ExchangeRateEntry.COLUMN_SOURCE_CURRENCY_CODE + " ASC, " + //sort by source code,
						ExchangeRateEntry.COLUMN_DEST_CURRENCY_CODE + " ASC " //then by dest code
						);
		
		final int EXPECTED_COUNT = TEST_EXPECTED_RATES.length;
		
		assertEquals("Did not get the number of records expected", EXPECTED_COUNT, cursor.getCount());
		
		cursor.moveToFirst();		
		 
		
		for ( int i = 0; i < EXPECTED_COUNT; i++, cursor.moveToNext() ) {
			UtilityTestMethods.validateSingleCursor("Error validating record number: " + i,
	    		   TEST_EXPECTED_RATES[i], cursor);
		}
		cursor.close();		
	}

}
