/*
 * Copyright 2014 Jason J.
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
package com.ovrhere.android.currencyconverter.model.localmodel;

import java.sql.SQLException;

/**
 * Defines structure for models that perform writes.
 *   
 * @author Jason J.
 * @version 0.2.0-201400616
 *
 * @param <R> The container/type for single records
 */
public interface WriteModel<R> {
	/**
	 * Removes a record from storage.
	 * @param id The id that identifies the record to delete.
	 * @return <code>true</code> on successful deletion, <code>false</code> 
	 * otherwise.
	 */
	public boolean deleteRecord(int id);
	/**
	 * Removes a record from storage.
	 * @param record The record to delete from storage.
	 * @return <code>true</code> on successful deletion, <code>false</code> 
	 * otherwise.
	 */
	public boolean deleteRecord(R record);
	/**
	 * Inserts a new record.
	 * @param record The record to add to storage.
	 * @throws SQLException If an error occurs during insertion.
	 */
	public void insertRecord(R record) throws SQLException;
	/**
	 * Modifies an existing record.
	 * @param id The id of the record to modify
	 * @param record The new information to add to the old record.
	 * @throws SQLException If an error occurs during modification.
	 */
	public void modifyRecord(int id, R record) throws SQLException;
}
