package com.ovrhere.android.currencyconverter.model.data;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;
import android.test.RenamingDelegatingContext;

import com.ovrhere.android.currencyconverter.model.data.CurrencyConverterContract.DisplayOrderEntry;
import com.ovrhere.android.currencyconverter.model.data.CurrencyConverterContract.ExchangeRateEntry;
import com.ovrhere.android.currencyconverter.test.UtilityTestMethods;

public class TestCurrencyConvertDbHelper extends AndroidTestCase {

	
			
	
	@Before
	protected void setUp() throws Exception {
		deleteDatabase(); //always start test with clean slate.
	}
	
	@After
	protected void tearDown() throws Exception {
		deleteDatabase();
		super.tearDown();
	}
		

	@Test
	public void testCreateSQLiteDatabase() {
		deleteDatabase(); //ensure clean.
		SQLiteDatabase db = new CurrencyConverterDbHelper(getTestContext()).getReadableDatabase();
		
        assertTrue("Database should be open", db.isOpen());
        
        //check to see if tables were in fact created
        Cursor cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);
        
        ArrayList<String> tables = new ArrayList<String>();
        tables.add(DisplayOrderEntry.TABLE_NAME);
        tables.add(ExchangeRateEntry.TABLE_NAME);
        
        validateColumnNames(
        		"Our database has not been created correctly", 
        		"One or more table not created.",
        		tables,
        		cursor);
        
        //checking our DisplayOrderEntry columns
        cursor = db.rawQuery("PRAGMA table_info(" + DisplayOrderEntry.TABLE_NAME + ")",
                null);

        final ArrayList<String> displayOrderCols = new ArrayList<String>();
        displayOrderCols.add(DisplayOrderEntry._ID);
        displayOrderCols.add(DisplayOrderEntry.COLUMN_CURRENCY_CODE);
        displayOrderCols.add(DisplayOrderEntry.COLUMN_DEF_DISPLAY_ORDER);

        validateColumnNames(
        		"Failed to query the database for table information.",
        		"DisplayOrderEntry Table does not contain all required columns", 
        		displayOrderCols, 
        		cursor);
        
        //checking our ExchangeRateEntry columns
        cursor = db.rawQuery("PRAGMA table_info(" + ExchangeRateEntry.TABLE_NAME + ")",
                null);        
        
        final ArrayList<String> exchangeRateCols = new ArrayList<String>();
        exchangeRateCols.add(ExchangeRateEntry._ID);
        exchangeRateCols.add(ExchangeRateEntry.COLUMN_SOURCE_CURRENCY_CODE);
        exchangeRateCols.add(ExchangeRateEntry.COLUMN_DEST_CURRENCY_CODE);
        exchangeRateCols.add(ExchangeRateEntry.COLUMN_EXCHANGE_RATE);

        validateColumnNames(
        		"Failed to query the database for table information.",
        		"ExchangeRateEntry Table does not contain all required columns", 
        		exchangeRateCols, 
        		cursor);
        
        cursor.close();
        db.close();
	}
	
	@Test
	public void testDisplayOrderTable() {
		SQLiteDatabase wDb = new CurrencyConverterDbHelper(getTestContext()).getWritableDatabase();
		
		long rowId1 = insertDisplayOrderEntry(wDb, "USD", 0);
		insertDisplayOrderEntry(wDb, "CAD", 1);
		insertDisplayOrderEntry(wDb, "BBD", 2);
		
		assertFalse("Problem inserting record", rowId1 == -1 );

		ContentValues inputs = new ContentValues();
        inputs.put(DisplayOrderEntry.COLUMN_CURRENCY_CODE, "USD" );
        inputs.put(DisplayOrderEntry.COLUMN_DEF_DISPLAY_ORDER, 3);
        
        wDb.insert(DisplayOrderEntry.TABLE_NAME, null, inputs);
        Cursor result = wDb.query(DisplayOrderEntry.TABLE_NAME, 
        		null, null, null, null, null, null, null);
        
        assertEquals("Record did not replaced as expected.", 3, result.getColumnCount() );
        
		wDb.close();
	}

	
	@Test
	public void testExchangeRateTable() {
		CurrencyConverterDbHelper dbHelper = new CurrencyConverterDbHelper(getTestContext());
        SQLiteDatabase wDb = dbHelper.getWritableDatabase();        
        		
        insertDisplayOrderEntry(wDb, "usd", 0);
		insertDisplayOrderEntry(wDb, "cad", 1);
		insertDisplayOrderEntry(wDb, "jpy", 2);
		
        ContentValues inputs1 = new ContentValues();
        inputs1.put(ExchangeRateEntry.COLUMN_SOURCE_CURRENCY_CODE, "usd");
        inputs1.put(ExchangeRateEntry.COLUMN_DEST_CURRENCY_CODE, "eur");
        inputs1.put(ExchangeRateEntry.COLUMN_EXCHANGE_RATE, 0.7750d); //old value
        
        ContentValues inputs2 = new ContentValues();
        inputs2.put(ExchangeRateEntry.COLUMN_SOURCE_CURRENCY_CODE, "usd");
        inputs2.put(ExchangeRateEntry.COLUMN_DEST_CURRENCY_CODE, "cad");
        inputs2.put(ExchangeRateEntry.COLUMN_EXCHANGE_RATE, 0.8050d); //old value
        
        ContentValues inputs3 = new ContentValues();
        inputs3.put(ExchangeRateEntry.COLUMN_SOURCE_CURRENCY_CODE, "cad");
        inputs3.put(ExchangeRateEntry.COLUMN_DEST_CURRENCY_CODE, "jpy");
        inputs3.put(ExchangeRateEntry.COLUMN_EXCHANGE_RATE, 1.6050d); //old value
        
        ContentValues badInput = new ContentValues(); //no matching foreign key
        inputs3.put(ExchangeRateEntry.COLUMN_SOURCE_CURRENCY_CODE, "bbd");
        inputs3.put(ExchangeRateEntry.COLUMN_DEST_CURRENCY_CODE, "bbd");
        inputs3.put(ExchangeRateEntry.COLUMN_EXCHANGE_RATE, 1d); //old value

        
        // Insert ContentValues into database and get a row ID back        
	    long rowId1 = wDb.insert(ExchangeRateEntry.TABLE_NAME, null, inputs1);        
        assertFalse("Problem inserting record.", rowId1 == -1 );
        
        Cursor result = wDb.query(  ExchangeRateEntry.TABLE_NAME, null,
                null, null, null, null, null, null);

        UtilityTestMethods.validateCursor("Expected matching cursor and content", inputs1, result);
        result.close();
        
        // Insert ContentValues into database and get -1        
	    long badRow = wDb.insert(ExchangeRateEntry.TABLE_NAME, null, badInput);        
        assertTrue("Record should fail; no foreign key in DisplayOrderEntry table", badRow == -1 );
        
        wDb.beginTransaction();
        	badRow = wDb.insert(ExchangeRateEntry.TABLE_NAME, null, inputs1);
        	long rowId2 = wDb.insert(ExchangeRateEntry.TABLE_NAME, null, inputs2);
        	long rowId3 = wDb.insert(ExchangeRateEntry.TABLE_NAME, null, inputs3);
        wDb.setTransactionSuccessful();
        wDb.endTransaction();
        
        wDb.close();
        
        assertFalse("Problem inserting records", rowId2 == -1 || rowId3 == -1 );
        
        SQLiteDatabase rDb = dbHelper.getReadableDatabase();
        result = rDb.query(  ExchangeRateEntry.TABLE_NAME, null,
                null, null, null, null, null, null);
        
        assertEquals("Inserted redundant record, instead of replacing", 3, result.getCount());        

        // Finally, close the cursor and database
        result.close();
        rDb.close();
        
    }
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Helper methods
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	private RenamingDelegatingContext getTestContext() {
		return new RenamingDelegatingContext(getContext(), "test_");
	}
	
	
	 /** Deletes the database, simply. */
	private void deleteDatabase(){
		getTestContext().deleteDatabase(CurrencyConverterDbHelper.DATABASE_NAME);
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Utility methods
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Validates the columns and ensures the actual matches the expected.
	 * @param messageEmpty 
	 * @param messageColumns
	 * @param expectedColumnNames
	 * @param actualCursor
	 */
	private static void validateColumnNames(String messageEmpty, String messageColumns, 
			List<String> expectedColumnNames, Cursor actualCursor) {
		
		assertTrue(messageEmpty, actualCursor.moveToFirst());
		
        final int colNameIndex = actualCursor.getColumnIndex("name");
        do {
        	expectedColumnNames.remove(actualCursor.getString(colNameIndex));
        } while(actualCursor.moveToNext());

        assertTrue(messageColumns, expectedColumnNames.isEmpty());
	}
	
	/**
	 * Inserts and tests each display order entry.
	 * @param wDb
	 * @param code
	 * @param order
	 */
	private static long insertDisplayOrderEntry(SQLiteDatabase wDb, String code,
			int order) {
		ContentValues inputs = new ContentValues();
        inputs.put(DisplayOrderEntry.COLUMN_CURRENCY_CODE, code);
        inputs.put(DisplayOrderEntry.COLUMN_DEF_DISPLAY_ORDER, order);
        
        long rowId = wDb.insert(DisplayOrderEntry.TABLE_NAME, null, inputs);
        
        assertFalse("Problem inserting record.", rowId == -1 );
        
        Cursor result = wDb.query(  
        		DisplayOrderEntry.TABLE_NAME, 
        		null, //columns
        		DisplayOrderEntry.COLUMN_CURRENCY_CODE + " = ? ", //selection 
                new String[]{code}, //selection args
                null, //group by
                null, //having 
                null, //order by
                null);//limit

        UtilityTestMethods.validateCursor("Expected matching cursor and content", inputs, result);
        result.close();
        
        return rowId;
	}

	
}
