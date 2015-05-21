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
package com.ovrhere.android.currencyconverter.model.database;

import java.util.HashMap;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * <p>Creates a database open helper for sqlite. Provides thread safe factory
 * methods for opening writable databases and read-only databases. 
 * For reference see:
 * <ul>
 * <li><a href="http://touchlabblog.tumblr.com/post/24474750219/single-sqlite-connection"
 *  target="_blank">Single SQLite connection | Touch Lab Blog</a> </li>
 * <li><a href="http://stackoverflow.com/questions/2493331/what-are-the-best-practices-for-sqlite-on-android"
 *  target="_blank">database - What are the best practices for SQLite on Android? - Stack Overflow</a></li>
 * </ul></p>
 * <p>It is suggested but not required to implement the 
 * {@link #onUpgrade(SQLiteDatabase, int, int)} function. </p>
 * @author Jason J.
 * @version 0.2.1-20140929
 * @see DatabaseSchema
 */
public class DatabaseOpenHelper extends SQLiteOpenHelper {
	/** Error message for read-only helpers trying to perform write work. */ 
	final private static String DETAILED_EXCEPTION_READ_ONLY = 
		"Helper is read-only. Cannot open a writable database.";
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Start static factory methods.
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** A map of database scheme names -> writerHelperInstances (such as with inheritance). */
	final protected static HashMap<String, DatabaseOpenHelper> writeHelperInstances 
		= new HashMap<String, DatabaseOpenHelper>();
	
	//http://stackoverflow.com/questions/10371233/where-to-close-the-sqliteopenhelper-if-i-am-using-a-singelton-one
    /**
     * <h3>Factory method</h3>
     * <p>Creates and maintains a single instance of a {@link DatabaseOpenHelper}
     * per database schema to ensure thread safety. 
     * When using a single helper for writing
     * requests are serialized; if not, multiple helpers can lead to lost writes.</p>
     * 
     * <p>To close, please call {@link #softClose()}. 
     * Alternatively, closing all open writable databases will allow 
     * the helper to close at application closing. </p>
     * 
     * @param context The context used to create the database helper.
     * @param databaseSchema The {@link DatabaseSchema} used to create the
     * helper and used to identify the instance.
     * 
     * @return The only instance of a DatabaseOpenHelper with this schema.
     */
	public static DatabaseOpenHelper getWriteHelper(Context context, 
			DatabaseSchema databaseSchema){
		synchronized (DatabaseOpenHelper.class) {
			String name = databaseSchema.getName();
			if (!writeHelperInstances.containsKey(name)){
				if (!writeHelperInstances.containsKey(name)){
					writeHelperInstances.put(name, 
							new DatabaseOpenHelper(
									context.getApplicationContext(),
									databaseSchema
									)
								);
						
				}
			}
			//synchronize the return as we don't know if someone is trying to 
			//close between the above if and return
			
			//increments references on this instance
			writeHelperInstances.get(name).incrementReferenceCount();
			return writeHelperInstances.get(name);
		}		
    }
	
    
    /**
     * <h3>Factory method</h3>
     * Creates a read-only database helper. 
     * These reads can be done in parallel as there are no dangerous 
     * race conditions.
     * 
     * @param context The context used to create the database helper.
     * @param databaseSchema The {@link DatabaseSchema} used to create the
     * helper.
     * 
     * @return A {@link DatabaseOpenHelper} for reading 
     * (will throw UnsupportedOperationException for {@link #getWritableDatabaseOrThrow()}).
     */
    public static DatabaseOpenHelper getReadHelper(Context context, 
    		DatabaseSchema databaseSchema){
    	DatabaseOpenHelper value = new DatabaseOpenHelper(	
    									context.getApplicationContext(), 
    									databaseSchema, 
										true);
    	//we do not sync, because we are not sharing.
    	value.incrementReferenceCount();
    	return value;
    }
  
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// End static factory methods.
	////////////////////////////////////////////////////////////////////////////////////////////////
	
    /** The schema used to define the tables creates, columns and types. */
    private DatabaseSchema dbSchema = null;
    /** USed to determine if the helper created is read only or not. 
     * Default is false. */
    private boolean readOnlyHelper = false; 
    
    /** A count of open references. */
	private int openReferenceCount = 0;
    
    /**
     * Create a helper object to create, open, and/or manage a database. 
     * Uses the default cursor factory.
     * @param context The context to get the database with.
     * @param databaseSchema The schema to create the database from.
     */
    protected DatabaseOpenHelper(Context context, DatabaseSchema databaseSchema) {
		super(context, databaseSchema.getName(), null, databaseSchema.getVersion());
		this.dbSchema = databaseSchema;		
	}
    
    /**
     * Create a helper object to create, open, and/or manage a database. 
     * Uses the default cursor factory.
     * @param context The context to get the database with.
     * @param databaseSchema The schema to create the database from.
     * @param readOnly (Optional) If <code>true</code>, the helper created cannot call
     * {@link #getWritableDatabaseOrThrow()}. Default value is <code>false</code>.
     */
    protected DatabaseOpenHelper(Context context, DatabaseSchema databaseSchema, 
			boolean readOnly) {
		this(context, databaseSchema);
		this.readOnlyHelper = readOnly;
	}
    
    /** Used to increment the reference count. */
    private void incrementReferenceCount(){
    	openReferenceCount++;
    }
    
    /** Attempts to close the helper. If there are no lingering open references
     * the helper is closed and any internal references removed. Otherwise,
     * the number of references is reduced. 
     * @return <code>true</code> if closed, <code>false</code> if still open.
     */
    public boolean softClose(){
    	synchronized (DatabaseOpenHelper.class) {
    		if (0 == openReferenceCount){
    			return true;
    		}
    		//remove a reference from this helper; if last, close.
			if(--openReferenceCount <= 0){
	    		this.close();
	    		DatabaseOpenHelper writeHelper = 
	    				writeHelperInstances.get(dbSchema.getName());
	    		
	    		//if this is currently the same instance of writeHelper within
	    		//the same schema
	    		if ( writeHelper != null && writeHelper.equals(this)){
	    			writeHelperInstances.remove(dbSchema.getName());
	    			//remove it.
	    		}
	    		return true;
	    	}
		}
    	return false;
    }

	/////////////////////////////////////////////////////////////////////////////////////////////////
	///  Override functions
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		String[] tableCreates = dbSchema.getTableCreates();
		for (String tableCreate : tableCreates){
			db.execSQL(tableCreate);
		}
	}
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		//not needed atm
	}
	
	/** Calls {@link #getWritableDatabase()} but throws if this database was 
	 * meant to be a read-only. 
	 * @return A writable database.	 
	 * @throws UnsupportedOperationException If this was intended to be a 
	 * read-only database. */
	public SQLiteDatabase getWritableDatabaseOrThrow() {
		if (readOnlyHelper){
			throw new UnsupportedOperationException(DETAILED_EXCEPTION_READ_ONLY);
		}
		
		return super.getWritableDatabase();
	}
}
