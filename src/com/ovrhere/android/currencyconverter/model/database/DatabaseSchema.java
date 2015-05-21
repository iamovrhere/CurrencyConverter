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

/**
 * The basic outline for a database schema for use with 
 * {@link DatabaseOpenHelper}.
 * @author Jason J.
 * @version 0.1.0-20140416
 */
abstract public class DatabaseSchema {
	/** @return The name of the database.	 */
	abstract public String getName();
	/** String used to generate the sql-lite tables. 
	 * @return An array of the table creation strings. */
	abstract public String[] getTableCreates();
	/** @return The database schema version number. Default is 1. */
	public int getVersion(){
		return 1;
	}	
}
