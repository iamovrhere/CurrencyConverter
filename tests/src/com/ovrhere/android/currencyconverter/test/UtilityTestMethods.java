package com.ovrhere.android.currencyconverter.test;

import java.util.Map.Entry;

import com.ovrhere.android.currencyconverter.model.data.CurrencyConverterContract.DisplayOrderEntry;
import com.ovrhere.android.currencyconverter.model.data.CurrencyConverterContract.ExchangeRateEntry;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.test.AndroidTestCase;

/**
 * 
 * @version 0.2.0-20150527
 *
 */
public class UtilityTestMethods extends AndroidTestCase {
	
	
	/**
	 * Clears the database tables via the resolver.
	 * @param context
	 */
	public static void deleteDatabaseByContentResolver(Context context) {
		context.getContentResolver().delete(
                DisplayOrderEntry.CONTENT_URI,
                null,
                null
        );
		context.getContentResolver().delete(
                ExchangeRateEntry.CONTENT_URI,
                null,
                null
        );
	}

	/**
	 * Validates the content values against the expected values 
	 * @param message The additional message to give after the log message. 
	 * @param expectedValues The expected results. 
	 * @param actualValues The test results.
	 */
	static public void validateContentValues(String message,
			ContentValues[] expectedValues,
			ContentValues[] actualValues) {
				
		int count = 0;
		for (ContentValues expected : expectedValues) {
			boolean matchFound = false;
			
			for (ContentValues actual : actualValues) { //ignore order
				if (expected.equals(actual)) { 
					//easy comparison? easy result.
					matchFound = true;
					break;
				} 
				
				int requiredMatches = expected.keySet().size();
				for (String columnName: expected.keySet()) {
					//check every column for matches
					if (expected.getAsString(columnName).equals(actual.getAsString(columnName))) {
						break; //if there was a mismatch, break.
					}
					requiredMatches--;
				}
				
				if (requiredMatches == 0) { //we met all matches? 
					matchFound = true; //we're done!
					break;
				}
				
			}
			assertTrue("Did not find match at record number " + count++, matchFound);
		}
	}
	
	
	/**
	 * Tests the first row of a cursor to ensure the cursor contains values from the 
	 * given content values.
	 * @param message The error to append if either columns or values are not found. 
	 * @param actual The cursor under test 
	 * @param expected The values expected.
	 */
	public static void validateCursor(String message, ContentValues expected, 
			Cursor actual) {
        assertTrue("Empty cursor returned. " + message, actual.moveToFirst());
        validateSingleCursor(message, expected, actual);        
        actual.close();
    }
	
	/**
	 * Tests to ensure the cursor contains values from the given content values.
	 * @param message The error to append if either columns or values are not found. 
	 * @param actual The cursor under test 
	 * @param expected The values expected.
	 */
	public static void validateSingleCursor(String message, ContentValues expected, 
			Cursor actual) {
		for (Entry<String, Object> entry : expected.valueSet()) {
            String colName = entry.getKey();
            
            int idx = actual.getColumnIndex(colName);
            assertFalse("Column '" + colName + "' not found. " + message, idx == -1);
            
            String expectedValue = entry.getValue().toString();
            assertEquals("Cursor value at '" + colName +"' does not match expected value: " + 
            			message, expectedValue, actual.getString(idx));
        }
	}
	
}
