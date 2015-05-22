package com.ovrhere.android.currencyconverter.model.data;

import java.util.HashSet;

import org.junit.Before;
import org.junit.Test;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import com.ovrhere.android.currencyconverter.model.data.CurrencyConverterContract.ExchangeRateEntry;
import com.ovrhere.android.currencyconverter.test.UtilityTestMethods;

public class TestCurrencyConvertDbHelper extends AndroidTestCase {

	@Before
	protected void setUp() throws Exception {
		super.setUp();
		deleteDatabase(); //always start test with clean slate.
	}

	@Test
	public void testCreateSQLiteDatabase() {
		deleteDatabase(); //ensure clean.
		SQLiteDatabase db = new CurrencyConverterDbHelper(this.mContext).getReadableDatabase();
		
        assertTrue("Database should be open", db.isOpen());
        
        //check to see if tables were in fact created
        Cursor cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);
        
        assertTrue("Our database has not been created correctly", cursor.moveToFirst());
        
        //checking our columns
        cursor = db.rawQuery("PRAGMA table_info(" + ExchangeRateEntry.TABLE_NAME + ")",
                null);

        assertTrue("Failed to query the database for table information.", cursor.moveToFirst());
        
        // Build a HashSet of all of the column names we want to look for
        final HashSet<String> exchangeRateCols = new HashSet<String>();
        exchangeRateCols.add(ExchangeRateEntry._ID);
        exchangeRateCols.add(ExchangeRateEntry.COLUMN_SOURCE_CURRENCY_CODE);
        exchangeRateCols.add(ExchangeRateEntry.COLUMN_DEST_CURRENCY_CODE);
        exchangeRateCols.add(ExchangeRateEntry.COLUMN_EXCHANGE_RATE);

        int colNameIndex = cursor.getColumnIndex("name");
        do {
            exchangeRateCols.remove(cursor.getString(colNameIndex));
        } while(cursor.moveToNext());

        assertTrue("Database does not contain all required columns", exchangeRateCols.isEmpty());
        
        db.close();
	}

	 public void testExchangeRateTable() {
        SQLiteDatabase wDb = new CurrencyConverterDbHelper(mContext).getWritableDatabase();

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
        inputs3.put(ExchangeRateEntry.COLUMN_DEST_CURRENCY_CODE, "bbd");
        inputs3.put(ExchangeRateEntry.COLUMN_EXCHANGE_RATE, 1.6050d); //old value

        // Insert ContentValues into database and get a row ID back
        
	    long rowId1 = wDb.insert(ExchangeRateEntry.TABLE_NAME, null, inputs1);
        
        assertFalse("Problem inserting record.", rowId1 == -1 );
        
        Cursor result = wDb.query(  ExchangeRateEntry.TABLE_NAME, null,
                null, null, null, null, null, null);

        UtilityTestMethods.validateCursor("Expected matching cursor and content", inputs1, result);
        result.close();
        
        wDb.beginTransaction();
        	rowId1 = wDb.insert(ExchangeRateEntry.TABLE_NAME, null, inputs1);
        	long rowId2 = wDb.insert(ExchangeRateEntry.TABLE_NAME, null, inputs2);
        	long rowId3 = wDb.insert(ExchangeRateEntry.TABLE_NAME, null, inputs3);
        wDb.setTransactionSuccessful();
        wDb.endTransaction();
        
        wDb.close();
        
        assertFalse("Problem inserting records", rowId2 == -1 || rowId3 == -1 );
        
        SQLiteDatabase rDb = new CurrencyConverterDbHelper(mContext).getReadableDatabase();
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
	 
	 /** Deletes the database, simply. */
	public void deleteDatabase(){
		mContext.deleteDatabase(CurrencyConverterDbHelper.DATABASE_NAME);
	}
	
}
