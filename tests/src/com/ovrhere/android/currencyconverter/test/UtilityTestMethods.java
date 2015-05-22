package com.ovrhere.android.currencyconverter.test;

import java.util.Map.Entry;

import android.content.ContentValues;
import android.database.Cursor;
import android.test.AndroidTestCase;

public class UtilityTestMethods extends AndroidTestCase {
	

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
            assertEquals("Cursor value does not match expected value: " + message, 
            			expectedValue, actual.getString(idx));
        }
	}
	
}
