package com.ovrhere.android.currencyconverter.model.data;

import android.content.ComponentName;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.test.AndroidTestCase;

import com.ovrhere.android.currencyconverter.model.data.CurrencyConverterContract.ExchangeRateEntry;
import com.ovrhere.android.currencyconverter.test.UtilityTestContentObserver;
import com.ovrhere.android.currencyconverter.test.UtilityTestMethods;

public class TestCurrencyConverterProvider extends AndroidTestCase {

	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		mContext.getContentResolver().delete(
                ExchangeRateEntry.CONTENT_URI,
                null,
                null
        );
	}
	
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		mContext.getContentResolver().delete(
                ExchangeRateEntry.CONTENT_URI,
                null,
                null
        );
	}


	
	public void testProviderRegistry() {
        PackageManager pm = mContext.getPackageManager();

        ComponentName componentName = new ComponentName(mContext.getPackageName(),
                CurrencyConverterProvider.class.getName());
        try {
            // This will throw an exception if the provider isn't registered in manifest.
            ProviderInfo providerInfo = pm.getProviderInfo(componentName, 0);

            assertEquals("Mismatched authorities.", providerInfo.authority, 
            		CurrencyConverterContract.CONTENT_AUTHORITY);
            
        } catch (PackageManager.NameNotFoundException e) {
            fail("Error: Provider not registered in manifest");
        }
    }
	
    public void testUriMatcher() {
        UriMatcher testMatcher = CurrencyConverterProvider.buildUriMatcher();
        
        final Uri TEST_EXCHANGE_RATE_DIR = 
        		CurrencyConverterContract.ExchangeRateEntry.CONTENT_URI;
        final Uri TEST_EXCHANGE_RATES_WITH_SOURCE_DIR = 
        		CurrencyConverterContract.ExchangeRateEntry.buildExchangeRateWithSourceCurrency("usd");
        final Uri TEST_EXCHANGE_RATES_WITH_SOURCE =  
        		CurrencyConverterContract.ExchangeRateEntry.buildExchangeRateFromSourceToDest("usd", "bbd");
        
        assertEquals("The EXCHANGE_RATE URI does not match.",
                testMatcher.match(TEST_EXCHANGE_RATE_DIR), 
                	CurrencyConverterProvider.EXCHANGE_RATES);
        
        assertEquals("The EXCHANGE_RATES_WITH_SOURCE URI does not match.",
                testMatcher.match(TEST_EXCHANGE_RATES_WITH_SOURCE_DIR), 
                	CurrencyConverterProvider.EXCHANGE_RATES_WITH_SOURCE);
        
        assertEquals("The XCHANGE_RATE_FROM_SOURCE_TO_DEST URI does not match.",
                testMatcher.match(TEST_EXCHANGE_RATES_WITH_SOURCE), 
                	CurrencyConverterProvider.EXCHANGE_RATE_FROM_SOURCE_TO_DEST);
    }
        
	
	public void testGetTypeUri() {		
		 // content://com.ovrhere.android.currencyconverter/exchange_rate/
        String mimeType = mContext.getContentResolver().getType(ExchangeRateEntry.CONTENT_URI);
        // vnd.android.cursor.dir/com.ovrhere.android.currencyconverter/exchange_rate
        assertEquals("Error: Should return ExchangeRateEntry.CONTENT_TYPE",
        		ExchangeRateEntry.CONTENT_TYPE, mimeType);

		 // content://com.ovrhere.android.currencyconverter/exchange_rate/usd
        mimeType = mContext.getContentResolver().getType(
        		ExchangeRateEntry.buildExchangeRateWithSourceCurrency("usd")
        		);
        // vnd.android.cursor.dir/com.ovrhere.android.currencyconverter/exchange_rate/usd
        assertEquals("Error: Should return ExchangeRateEntry.CONTENT_TYPE",
        		ExchangeRateEntry.CONTENT_TYPE, mimeType);
        
		 // content://com.ovrhere.android.currencyconverter/exchange_rate/cad/bbd
        mimeType = mContext.getContentResolver().getType(
        		ExchangeRateEntry.buildExchangeRateFromSourceToDest("cad", "bbd")
        		);
        // vnd.android.cursor.dir/com.ovrhere.android.currencyconverter/exchange_rate/cad/bbd
        assertEquals("Error: should return ExchangeRateEntry.CONTENT_TYPE",
        		ExchangeRateEntry.CONTENT_ITEM_TYPE, mimeType);
                
	}

	
	public void testBasicCurrencyQuery() {
		
        mContext.getContentResolver()
        		.insert(ExchangeRateEntry.CONTENT_URI, getTestInputs_CAD_BBD());
        
        // Test the basic content provider query
        Cursor resultCursor = mContext.getContentResolver().query(
                ExchangeRateEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        UtilityTestMethods.validateCursor("Content Resolver failed to match", 
        		getTestInputs_CAD_BBD(), resultCursor);
        
        // Level 19 or greater has getNotificationUri, which is useful in testing.
        if ( Build.VERSION.SDK_INT >= 19 ) {
            //assertEquals("Error: Basic Curreny query did not correctly set NotificationUri",
            //        resultCursor.getNotificationUri(), ExchangeRateEntry.CONTENT_URI);
        }
        
        resultCursor.close();
	}
	
	public void testComplexCurrencyQuery() {
		mContext.getContentResolver()
        		.insert(ExchangeRateEntry.CONTENT_URI, getTestInputs_CAD_BBD());
		mContext.getContentResolver()
				.insert(ExchangeRateEntry.CONTENT_URI, getTestInputs_CAD_GBP());
        
        // Test dir query
        Cursor resultCursor = mContext.getContentResolver().query(
                ExchangeRateEntry.buildExchangeRateWithSourceCurrency("CAD"),
                null,
                null,
                null,
                null
        );
        
        assertEquals("Expected different count for dir query", 2, resultCursor.getCount());
        
        resultCursor.moveToFirst();
        UtilityTestMethods.validateSingleCursor("CAD -> BBD cursor failed to match", 
        		getTestInputs_CAD_BBD(), resultCursor);
        resultCursor.moveToNext();
        UtilityTestMethods.validateSingleCursor("CAD -> GBP cursor failed to match", 
        		getTestInputs_CAD_GBP(), resultCursor);
        resultCursor.close();
        
        //testing item query
        resultCursor = mContext.getContentResolver().query(
                ExchangeRateEntry.buildExchangeRateFromSourceToDest("CAD", "GBP"),
                null,
                null,
                null,
                null
        );
        
        assertEquals("Expected single record for item query", 1, resultCursor.getCount());
        
        UtilityTestMethods.validateCursor("CAD -> GBP cursor failed to match", 
        		getTestInputs_CAD_GBP(), resultCursor);
        
	}

	
	public void testInsertReadContentProvider() {
		ContentValues testValues = getTestInputs_USD_CAD();
		
        UtilityTestContentObserver observer = UtilityTestContentObserver.newTestContentObserver();
        mContext.getContentResolver()
        		.registerContentObserver(ExchangeRateEntry.CONTENT_URI, true, observer);
        Uri resultUri = mContext.getContentResolver()
        		.insert(ExchangeRateEntry.CONTENT_URI, testValues);


        observer.waitForNotificationOrFail();
        mContext.getContentResolver().unregisterContentObserver(observer);

        long rowId = ContentUris.parseId(resultUri);

        assertTrue("Row failed to insert", rowId != -1);

        // data in
        
        // data out?
        Cursor cursor = mContext.getContentResolver().query(
        		ExchangeRateEntry.CONTENT_URI,
                null, // all columns
                null, // select
                null, // selectArgs
                null  // sort order
        );

        UtilityTestMethods.validateCursor("Results do not match input", testValues, cursor);
        
        cursor.close();
	}

	
	public void testUpdateContentProvider() {
		testBulkInsertContent();
        
        final double economicBoom = 0.3021d;        
        final ContentValues updateValues = new ContentValues(); 
        updateValues.put(ExchangeRateEntry.COLUMN_EXCHANGE_RATE, economicBoom); 
        
        final String selectCurrency = ExchangeRateEntry.COLUMN_SOURCE_CURRENCY_CODE + " = ?";
        
        int changed = mContext.getContentResolver()
        		.update(ExchangeRateEntry.CONTENT_URI, updateValues, 
        				selectCurrency, new String[]{ "usd" } );
        
        assertEquals("Expected 2 records to change", 2, changed);
        
        Cursor results = mContext.getContentResolver().query(
        		ExchangeRateEntry.CONTENT_URI, 
        		new String [] {ExchangeRateEntry.COLUMN_EXCHANGE_RATE}, 
        		selectCurrency, 
        		new String[]{ "usd" },
        		null, 
        		null);
        
        while (results.moveToNext()) {
        	assertEquals("Expected to have " + economicBoom, economicBoom, results.getDouble(0));
        }
        
        results.close();
        //if we made it here, it works!
	}

	
	public void testDeleteContentProvider() {
		//insert manually in case our insert provider is broken
		Uri uri1 = mContext.getContentResolver()
					.insert(ExchangeRateEntry.CONTENT_URI, getTestInputs_CAD_GBP());
        Uri uri2 = mContext.getContentResolver()
					.insert(ExchangeRateEntry.CONTENT_URI, getTestInputs_CAD_BBD());
		
        long id1 = ContentUris.parseId(uri1);
        long id2 = ContentUris.parseId(uri2);

		assertFalse("One or more records did not insert correctly", id1 == -1 || id2 == -1);
		
        UtilityTestContentObserver deleteObserver = UtilityTestContentObserver.newTestContentObserver();
        mContext.getContentResolver()
        		.registerContentObserver(ExchangeRateEntry.CONTENT_URI, true, deleteObserver);

        //delete everything
        mContext.getContentResolver().delete(
        		ExchangeRateEntry.CONTENT_URI,
                null,
                null
        );
        
        //we wait or fail.
        deleteObserver.waitForNotificationOrFail();
        mContext.getContentResolver().unregisterContentObserver(deleteObserver);
	}

	
	public void testBulkInsertContent() {
		ContentValues[] bulkCV = new ContentValues[]{ 
			getTestInputs_USD_EUR(),
			getTestInputs_USD_CAD(),
			getTestInputs_CAD_GBP(),
			getTestInputs_CAD_BBD()
		};
		final int RECORD_COUNT = bulkCV.length;
		
		UtilityTestContentObserver observer = UtilityTestContentObserver.newTestContentObserver();
        mContext.getContentResolver()
        		.registerContentObserver(ExchangeRateEntry.CONTENT_URI, true, observer);

		int insertCount = mContext.getContentResolver()
				.bulkInsert(ExchangeRateEntry.CONTENT_URI, bulkCV);		
		
        observer.waitForNotificationOrFail();
        mContext.getContentResolver().unregisterContentObserver(observer);

        
		assertEquals("Did not insert all records as expected", RECORD_COUNT, insertCount);
		
		Cursor cursor = mContext.getContentResolver()
				.query(ExchangeRateEntry.CONTENT_URI, null, null, null, null);
		
		cursor.moveToFirst();
        for ( int i = 0; i < RECORD_COUNT; i++, cursor.moveToNext() ) {
            UtilityTestMethods.validateSingleCursor("Error validating record number: " + i,
                    bulkCV[i], cursor);
        }
        cursor.close();		
		
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Utility methods
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	private static ContentValues getTestInputs_USD_EUR() {
		ContentValues cv = new ContentValues();
		cv.put(ExchangeRateEntry.COLUMN_SOURCE_CURRENCY_CODE, "usd");
		cv.put(ExchangeRateEntry.COLUMN_DEST_CURRENCY_CODE, "eur");
    	cv.put(ExchangeRateEntry.COLUMN_EXCHANGE_RATE, 0.7750d);
		return cv;
	}
	private static ContentValues getTestInputs_USD_CAD() {
		ContentValues cv = new ContentValues();
		cv.put(ExchangeRateEntry.COLUMN_SOURCE_CURRENCY_CODE, "usd");
		cv.put(ExchangeRateEntry.COLUMN_DEST_CURRENCY_CODE, "cad");
    	cv.put(ExchangeRateEntry.COLUMN_EXCHANGE_RATE, 0.8050d); //old value
		return cv;
	}
	private static ContentValues getTestInputs_CAD_BBD() {
		ContentValues cv = new ContentValues();
		cv.put(ExchangeRateEntry.COLUMN_SOURCE_CURRENCY_CODE, "cad");
		cv.put(ExchangeRateEntry.COLUMN_DEST_CURRENCY_CODE, "bbd");
    	cv.put(ExchangeRateEntry.COLUMN_EXCHANGE_RATE, 1.6050d); //old value
		return cv;
	}
	private static ContentValues getTestInputs_CAD_GBP() {
		ContentValues cv = new ContentValues();
		cv.put(ExchangeRateEntry.COLUMN_SOURCE_CURRENCY_CODE, "cad");
		cv.put(ExchangeRateEntry.COLUMN_DEST_CURRENCY_CODE, "gbp");
    	cv.put(ExchangeRateEntry.COLUMN_EXCHANGE_RATE, 0.9050d); //old value
		return cv;
	}
	

	
	
}
