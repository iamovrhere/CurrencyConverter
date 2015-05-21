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
package com.ovrhere.android.currencyconverter.model.localmodel;

/**
 * Defines structure for models that perform reads & writes.
 *   
 * @author Jason J.
 * @version 0.1.0-20140425
 *
 * @param <R1> The container/type for single records
 * @param <R2> The container/type for multiple records.
 */
public interface ReadWriteModel<R1, R2>
	extends ReadModel<R1>, WriteModel<R1> {
	/** Returns all records from storage.
	 * @return The records in a type R2 container.	 */
	public R2 getAllRecords();
}
