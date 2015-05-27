/*
 * Copyright 2015 Jason J. (iamovrhere)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.ovrhere.android.currencyconverter.model.data;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

/**
 * <p>Provides ContentResolver a means of querying and updating the database for 
 * currency exchange rates.
 * Remember to add: 
 * <code>&lt;provider
            android:name=".model.data.CurrencyConverterProvider"
            android:exported="false"
            /&gt;</code> to the manifest.</p>
 * 
 * Provider will only return precisely what is put in, that is to say the data store
 * is NOT compact (requires both A -> B and B -> A). 
 *
 * @author Jason J.
 * @version 0.2.0-20150527
 */
public class CurrencyConverterProvider extends ContentProvider {

    final public static int EXCHANGE_RATES = 100;
    final public static int EXCHANGE_RATES_WITH_SOURCE = 101;
    final public static int EXCHANGE_RATE_FROM_SOURCE_TO_DEST = 102;
    
    final public static int DISPLAY_ORDER = 200;
    
    //TODO enforce uppercase
    //TODO allow for compact contents of the database, while having verbose responses
    // for both EXCHANGE_RATES_WITH_SOURCE and EXCHANGE_RATE_FROM_SOURCE_TO_DEST.

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //// End Public Constants
    ////////////////////////////////////////////////////////////////////////////////////////////////
    /** Uri matcher for pairing uris to public ids. */
    final private static UriMatcher URI_MATCHER = buildUriMatcher();
    /** Builder, very helpful for repetitive queries. */
    final private static SQLiteQueryBuilder RATE_BY_SOURCE_BUILDER =
            new SQLiteQueryBuilder();

    static {
        //If we were to JOIN, we could do it here.
    	RATE_BY_SOURCE_BUILDER.setTables(
                CurrencyConverterContract.ExchangeRateEntry.TABLE_NAME + " INNER JOIN " +
                		CurrencyConverterContract.DisplayOrderEntry.TABLE_NAME +
                		" ON " + CurrencyConverterContract.ExchangeRateEntry.TABLE_NAME +
                		"." + CurrencyConverterContract.ExchangeRateEntry.COLUMN_DEST_CURRENCY_CODE +
                		" = " + CurrencyConverterContract.DisplayOrderEntry.TABLE_NAME +
                		"." + CurrencyConverterContract.DisplayOrderEntry.COLUMN_CURRENCY_CODE
        );
    	
    }
    

    /** Prepared statement. source_code = ?  (case insensitive) */
    final private static String SELECTION_SOURCE_CURRENCY =
            CurrencyConverterContract.ExchangeRateEntry.COLUMN_SOURCE_CURRENCY_CODE + " LIKE ? ";

    /** Prepared statement. source_code = ? AND dest_code = ?  (case insensitive) */
    final private static String SELECTION_SOURCE_TO_DEST =
    		CurrencyConverterContract.ExchangeRateEntry.COLUMN_SOURCE_CURRENCY_CODE + " LIKE ?  AND " +
			CurrencyConverterContract.ExchangeRateEntry.COLUMN_DEST_CURRENCY_CODE + " LIKE ? ";


    ////////////////////////////////////////////////////////////////////////////////////////////////
    //// End Constants
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private CurrencyConverterDbHelper mDbHelper = null;

    @Override
    public boolean onCreate() {
        this.mDbHelper = new CurrencyConverterDbHelper(getContext());
        return true;
    }

    @Override
    public String getType(Uri uri) { 
        switch (URI_MATCHER.match(uri)) {
	        case EXCHANGE_RATE_FROM_SOURCE_TO_DEST:
	            return CurrencyConverterContract.ExchangeRateEntry.CONTENT_ITEM_TYPE;
	            
            case EXCHANGE_RATES_WITH_SOURCE:
                return CurrencyConverterContract.ExchangeRateEntry.CONTENT_TYPE;
                
            case EXCHANGE_RATES:
                return CurrencyConverterContract.ExchangeRateEntry.CONTENT_TYPE;
                
            case DISPLAY_ORDER:
            	return CurrencyConverterContract.DisplayOrderEntry.CONTENT_TYPE;
                            	
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }


    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor result = null;
        switch (URI_MATCHER.match(uri)) {
            case EXCHANGE_RATE_FROM_SOURCE_TO_DEST:
                result = getRowFromSourceToDest(uri, projection, sortOrder);
                break;

            case EXCHANGE_RATES_WITH_SOURCE:
                result = getRowFromSource(uri, projection, sortOrder);
                break;

            case EXCHANGE_RATES:
                result = queryTableByArgs(CurrencyConverterContract.ExchangeRateEntry.TABLE_NAME,
                        projection, selection, selectionArgs, sortOrder);
                break;
                
            case DISPLAY_ORDER:
            	 result = queryTableByArgs(CurrencyConverterContract.DisplayOrderEntry.TABLE_NAME,
                         projection, selection, selectionArgs, sortOrder);
                 break;
                 
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        result.setNotificationUri(getContext().getContentResolver(), uri);
        
        return result;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) throws SQLException {
        final SQLiteDatabase wDb = mDbHelper.getWritableDatabase();
        Uri resultUri  = null;

        switch (URI_MATCHER.match(uri)) {
            case EXCHANGE_RATES: {
                final String table = CurrencyConverterContract.ExchangeRateEntry.TABLE_NAME;
                
                long id = wDb.insert(table, null, values);
                if ( id >= 0 ) {
                    resultUri = CurrencyConverterContract.ExchangeRateEntry.buildExchangeRateUri(id);
                } else {
                    throw new SQLException("Failed to insert row into " + uri);
                }
                break;
            }
            
            case DISPLAY_ORDER: {
                final String table = CurrencyConverterContract.DisplayOrderEntry.TABLE_NAME;
                
                long id = wDb.insert(table, null, values);
                if ( id >= 0 ) {
                    resultUri = CurrencyConverterContract.DisplayOrderEntry.buildDisplayOrderUri(id);
                } else {
                    throw new SQLException("Failed to insert row into " + uri);
                }
                break;
            }
                
            case EXCHANGE_RATE_FROM_SOURCE_TO_DEST:
            case EXCHANGE_RATES_WITH_SOURCE:
                //not supported
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        
        return resultUri;
    }


    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int updateCount = 0;

        switch (URI_MATCHER.match(uri)) {
            case EXCHANGE_RATES: {
            	updateCount = updateTransaction(
            			CurrencyConverterContract.ExchangeRateEntry.TABLE_NAME, 
            			values, selection, selectionArgs);
                break;
            }
                
            case DISPLAY_ORDER: 
            	updateCount = updateTransaction(
            			CurrencyConverterContract.DisplayOrderEntry.TABLE_NAME, 
            			values, selection, selectionArgs);
                break;	
            
            case EXCHANGE_RATE_FROM_SOURCE_TO_DEST:
            case EXCHANGE_RATES_WITH_SOURCE:
                //unsupported updates
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        
        if (updateCount != 0) { //do not notify if nothing happens
            getContext().getContentResolver().notifyChange(uri, null);
        }
        
        return updateCount;
    }

	
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase wDb = mDbHelper.getWritableDatabase();
        int deleteCount = 0;

        if (selection == null){
            selection = "1"; //delete all rows.
        }
        
        switch (URI_MATCHER.match(uri)) {
        	case EXCHANGE_RATES:
                deleteCount = wDb.delete(CurrencyConverterContract.ExchangeRateEntry.TABLE_NAME,
                                selection, selectionArgs);
                break;
                
        	case DISPLAY_ORDER:
        		deleteCount = wDb.delete(CurrencyConverterContract.DisplayOrderEntry.TABLE_NAME,
                        selection, selectionArgs);
        		break;
        		
            case EXCHANGE_RATE_FROM_SOURCE_TO_DEST:
            case EXCHANGE_RATES_WITH_SOURCE:
                //unsupported deletes
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        
        if (deleteCount != 0) { //do not notify if nothing happens.
            getContext().getContentResolver().notifyChange(uri, null);
        }
        
        return deleteCount;
    }



    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final int match = URI_MATCHER.match(uri);
        
        switch (match) {
        	case EXCHANGE_RATES:
        		return bulkTransactionInsert(
        				CurrencyConverterContract.ExchangeRateEntry.TABLE_NAME, uri, values);
        		
        	case DISPLAY_ORDER:
        		return bulkTransactionInsert(
        				CurrencyConverterContract.DisplayOrderEntry.TABLE_NAME, uri, values);
             
        	case EXCHANGE_RATE_FROM_SOURCE_TO_DEST:
            case EXCHANGE_RATES_WITH_SOURCE:
                //unsupported bulk inserts
            default:
                return super.bulkInsert(uri, values);
        }
    }

	

    /*  Used for in testing framework to run smoothly. See:
     * http://developer.android.com/reference/android/content/ContentProvider.html#shutdown() */
    @Override
    @TargetApi(11)
    public void shutdown() {
        mDbHelper.close();
        super.shutdown();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //// Helper methods
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Bulk inserts given values in a transaction block and notifies the 
     * {@link ContentResolver} via the uri, that the work is complete. 
     * @param table
     * @param uri
     * @param values
     * @return The count of records inserted.
     */
    private int bulkTransactionInsert(String table, Uri uri, ContentValues[] values) {
		final SQLiteDatabase wDb = mDbHelper.getWritableDatabase();
		wDb.beginTransaction();
		int insertCount = 0;
		try {
		        for (ContentValues value : values) {
		        	long id = wDb.insert(table, null, value);
		            
		            if (id != -1) {
		                insertCount++;
		            }
		        }
		        wDb.setTransactionSuccessful();
		} finally {
		    wDb.endTransaction();
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return insertCount;
	}
    
    /**
     * Updates database in a transaction block, using the given values.
     * @param table
     * @param values
     * @param selection
     * @param selectionArgs
     * @return The number of records updated.
     */
    private int updateTransaction(final String table, ContentValues values,
			String selection, String[] selectionArgs) {
		int updateCount = 0;
		final SQLiteDatabase wDb = mDbHelper.getWritableDatabase();
		wDb.beginTransaction();
		try {
			updateCount = wDb.update(table, values, selection, selectionArgs);
			 wDb.setTransactionSuccessful();
		} finally {
		    wDb.endTransaction();
		}
		return updateCount;
	}
    
    /**
     * Get the currency Cursor based on source codes.
     * @param uri 
     * @param projection
     * @param sortOrder
     * @return The queried cursor.
     */
    private Cursor getRowFromSource(Uri uri, String[] projection, String sortOrder) {
        String sourceCode = CurrencyConverterContract.ExchangeRateEntry.getSourceCurrencyFromUri(uri);
            	
        return RATE_BY_SOURCE_BUILDER.query(mDbHelper.getReadableDatabase(),
                projection,
                SELECTION_SOURCE_CURRENCY,
                new String[]{sourceCode},
                null,
                null,
                sortOrder
        );
    }

    /**
     * Get the currency Cursor based on source -> dest.
     * @param uri 
     * @param projection
     * @param sortOrder
     * @return The queried cursor.
     */
    private Cursor getRowFromSourceToDest(Uri uri, String[] projection, String sortOrder) {
        String sourceCode = CurrencyConverterContract.ExchangeRateEntry.getSourceCurrencyFromUri(uri);
        String destCode = CurrencyConverterContract.ExchangeRateEntry.getDestCurrencyFromUri(uri);
    	
        return RATE_BY_SOURCE_BUILDER.query(mDbHelper.getReadableDatabase(),
                projection,
                SELECTION_SOURCE_TO_DEST,
                new String[]{sourceCode, destCode},
                null,
                null,
                sortOrder
        );
    }


    /**
     * Gets <code>table</code>Cursor based on args.
     * @param table
     * @param projection
     * @param selection
     * @param selectionArgs
     * @param sortOrder
     * @return
     */
    private Cursor queryTableByArgs(String table, String[] projection, String selection,
                                     String[] selectionArgs, String sortOrder) {
        SQLiteDatabase rDb = mDbHelper.getReadableDatabase();
        Cursor results = rDb.query(
                            table,
                            projection,
                            selection,
                            selectionArgs,
                            null,
                            null,
                            sortOrder
                    );
        return results;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    //// Utility methods
    ////////////////////////////////////////////////////////////////////////////////////////////////


    /**
     * Builds the UriMatcher to match each Uri to their respective integer constants.
     * @return The matcher capable of matching uris too their respective constants.
     */
    protected static UriMatcher buildUriMatcher() {
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        //add types to the UriMatcher
        //exchange_rate/
        uriMatcher.addURI(
                CurrencyConverterContract.CONTENT_AUTHORITY,
                CurrencyConverterContract.ExchangeRateEntry.TABLE_NAME,
                EXCHANGE_RATES);
        //exchange_rate/[source_currency_code]
        uriMatcher.addURI(
        		CurrencyConverterContract.CONTENT_AUTHORITY,
                CurrencyConverterContract.ExchangeRateEntry.TABLE_NAME + "/*",
                EXCHANGE_RATES_WITH_SOURCE);
        //exchange_rate/[source_currency_code]/[dest_currency_code]
        uriMatcher.addURI(
        		CurrencyConverterContract.CONTENT_AUTHORITY,
                CurrencyConverterContract.ExchangeRateEntry.TABLE_NAME + "/*/*",
                EXCHANGE_RATE_FROM_SOURCE_TO_DEST);
        
        //display_order/
        uriMatcher.addURI(
        		CurrencyConverterContract.CONTENT_AUTHORITY,
                CurrencyConverterContract.DisplayOrderEntry.TABLE_NAME,  
                DISPLAY_ORDER);
        
        return uriMatcher;
    }
}
